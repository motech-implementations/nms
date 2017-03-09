package org.motechproject.nms.kilkari.service.impl;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.exception.InvalidReferenceDateException;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.MctsChildFixService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.jdo.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the {@link MctsChildFixService} interface.
 */
@Service("mctsChildFixService")
public class MctsChildFixServiceImpl implements MctsChildFixService {

    private MctsChildDataService mctsChildDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsChildFixServiceImpl.class);

    private SubscriberDataService subscriberDataService;
    private MctsMotherDataService mctsMotherDataService;
    private SubscriberService subscriberService;

    private int notFoundCount;
    private int subscriberPurgedCount;

    @Autowired
    public MctsChildFixServiceImpl(SubscriberDataService subscriberDataService,
                                   MctsMotherDataService mctsMotherDataService,
                                   MctsChildDataService mctsChildDataService,
                                   SubscriberService subscriberService) {
        this.subscriberDataService = subscriberDataService;
        this.mctsMotherDataService = mctsMotherDataService;
        this.mctsChildDataService = mctsChildDataService;
        this.subscriberService = subscriberService;
    }

    /**
     * Expected file format:
     * - any number of empty lines
     * - header line in the following format:  ID_No,Mother_ID,Birthdate
     * - one empty line
     * - CSV data (comma-separated)
     */
    @Override
    @Transactional
    public void updateMotherChild(Reader reader) throws IOException {

        List<MctsChild> childRecords = getAllIndependentChildRecords();

        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getCsvProcessorMapping())
                .setPreferences(CsvPreference.STANDARD_PREFERENCE)
                .createAndOpen(bufferedReader);

        notFoundCount = 0;
        subscriberPurgedCount = 0;
        int count = 0;

        Map<String, Object> record;
        Timer timer = new Timer("kid", "kids");
        LOGGER.debug("Started import");
        while (null != (record = csvImporter.read())) {
            try {
                updateSubscriber(record, childRecords);
                count++;
                if (count % KilkariConstants.PROGRESS_INTERVAL == 0) {
                    LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
                }
            } catch (RuntimeException e) {
                LOGGER.error("Error at beneficiary_id {}", record.get(KilkariConstants.BENEFICIARY_ID), e);
                throw new CsvImportDataException(String.format("MCTS child update import error, constraints violated"), e);
            }

        }

        LOGGER.debug("Count of records successfully read {}", count);
        LOGGER.debug("Count of Subscribers not found in Database {}", notFoundCount);
        LOGGER.debug("Count of Subscribers purged {}", subscriberPurgedCount);

        LOGGER.debug("updateMotherInChild() END ({})", count > 0 ? timer.frequency(count) : timer.time());
    }

    @Override
    @Transactional
    public void updateSubscriber(Map<String, Object> record, List<MctsChild> childRecords) {

        String beneficiaryId = (String) record.get(KilkariConstants.BENEFICIARY_ID);
        String motherBeneficiaryId = (String) record.get(KilkariConstants.MOTHER_ID);
        DateTime dob = (DateTime) record.get(KilkariConstants.DOB);
        MctsChild child = containsIn(beneficiaryId, childRecords);

        if (child != null) {

            Subscriber subscriber = subscriberService.getSubscriberByBeneficiary(child);

            // If mother is null then just update dob in child else update dob and mother
            if (motherBeneficiaryId != null) {
                if (subscriber != null) {

                    MctsMother mother = mctsMotherDataService.create(new MctsMother(motherBeneficiaryId));
                    child = subscriber.getChild();
                    child.setMother(mother);
                    child.setDateOfBirth(subscriber.getDateOfBirth());

                    if (subscriber.getMother() != null && subscriber.getMother().getBeneficiaryId() != motherBeneficiaryId) {
                        // create mother and new subscriber for child and update it in existing child subscription
                        Subscriber childSubscriber = new Subscriber(subscriber.getCallingNumber(), subscriber.getLanguage(), subscriber.getCircle());
                        childSubscriber.setMother(mother);
                        childSubscriber.setChild(child);
                        childSubscriber.setDateOfBirth(subscriber.getDateOfBirth());
                        childSubscriber = subscriberDataService.create(childSubscriber);
                        subscriber.setDateOfBirth(null);
                        subscriber.setChild(null);
                        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
                        for (Subscription subscription : subscriptions) {
                            if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.CHILD) {
                                subscription.setSubscriber(childSubscriber);
                            }
                        }
                    } else if (subscriber.getMother() == null) {
                        // Update mother in child and subscriber
                        subscriber.setMother(mother);
                    }


                } else {
                    // update mother in child
                    MctsMother mother = mctsMotherDataService.create(new MctsMother(motherBeneficiaryId));
                    child = mctsChildDataService.findByBeneficiaryId(beneficiaryId);
                    child.setMother(mother);
                    child.setDateOfBirth(dob);
                    subscriberPurgedCount++;
                    LOGGER.debug("No Subscriber found for BeneficiaryId {}", beneficiaryId);
                }
            } else {
                if (subscriber != null) {
                    child.setDateOfBirth(subscriber.getDateOfBirth());
                } else {
                    child.setDateOfBirth(dob);
                    subscriberPurgedCount++;
                    LOGGER.debug("No Subscriber found for BeneficiaryId {}", beneficiaryId);
                }
            }



        } else {
            notFoundCount++;
            LOGGER.debug("Child not found for BeneficiaryId {}", beneficiaryId);
        }

    }

    private MctsChild containsIn(String beneficiaryId, List<MctsChild> childRecords) {
        for (MctsChild childRecord : childRecords) {
            if (childRecord.getBeneficiaryId().equals(beneficiaryId)) {
                return childRecord;
            }
        }
        return null;
    }

    public List<MctsChild> getAllIndependentChildRecords() {

        SqlQueryExecution<List<MctsChild>> queryExecution = new SqlQueryExecution<List<MctsChild>>() {

            @Override
            public String getSqlQuery() {
                return "SELECT * FROM nms_mcts_children WHERE nms_mcts_children.mother_id_OID IS NULL";
            }

            @Override
            public List<MctsChild> execute(Query query) {
                query.setClass(MctsChild.class);
                return (List<MctsChild>) query.execute();
            }

        };
        return mctsChildDataService.executeSQLQuery(queryExecution);
    }

    private Map<String, CellProcessor> getCsvProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(KilkariConstants.BENEFICIARY_ID, new Optional(new GetString()));

        mapping.put(KilkariConstants.MOTHER_ID, new Optional(new GetString()));

        mapping.put(KilkariConstants.DOB, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return getDateByString(value);
            }
        }));
        return mapping;
    }

    public DateTime getDateByString(String value) {
        if (value == null) {
            return null;
        }

        DateTime referenceDate;

        try {
            DateTimeParser[] parsers = {
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser()};
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

            referenceDate = formatter.parseDateTime(value);

        } catch (IllegalArgumentException e) {
            throw new InvalidReferenceDateException(String.format("Reference date %s is invalid", value), e);
        }

        return referenceDate;
    }
}
