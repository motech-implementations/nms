package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.DataServiceCommentMatcher;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.prefs.CsvPreference.Builder;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the {@link MctsBeneficiaryImportService} interface.
 */
@Service("mctsBeneficiaryImportService")
public class MctsBeneficiaryImportServiceImpl implements MctsBeneficiaryImportService {

    private static final String STATE = "StateID";
    private static final String DISTRICT = "District_ID";
    private static final String TALUKA = "Taluka_ID";
    private static final String HEALTH_BLOCK = "HealthBlock_ID";
    private static final String PHC = "PHC";
    private static final String VILLAGE = "Village_ID";
    private static final String BENEFICIARY_ID = "ID_No";
    private static final String BENEFICIARY_NAME = "Name";
    private static final String MSISDN = "Whom_PhoneNo";
    private static final String LMP = "LMP_Date";
    private static final String DOB = "Birthdate";
    private static final String MOTHER_ID = "Mother_ID";

    private StateDataService stateDataService;
    private DistrictDataService districtDataService;
    private TalukaDataService talukaDataService;
    private HealthBlockDataService healthBlockDataService;
    private HealthFacilityDataService healthFacilityDataService;
    private VillageDataService villageDataService;
    private MctsMotherDataService mctsMotherDataService;
    private MctsChildDataService mctsChildDataService;
    private SubscriptionService subscriptionService;
    private SubscriberService subscriberService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private SubscriptionErrorDataService subscriptionErrorDataService;

    @Autowired
    MctsBeneficiaryImportServiceImpl(StateDataService stateDataService, DistrictDataService districtDataService,
                                     TalukaDataService talukaDataService, HealthBlockDataService healthBlockDataService,
                                     HealthFacilityDataService healthFacilityDataService,
                                     VillageDataService villageDataService, MctsMotherDataService mctsMotherDataService,
                                     MctsChildDataService mctsChildDataService, SubscriptionService subscriptionService,
                                     SubscriberService subscriberService,
                                     SubscriptionPackDataService subscriptionPackDataService,
                                     SubscriptionErrorDataService subscriptionErrorDataService) {
        this.stateDataService = stateDataService;
        this.districtDataService = districtDataService;
        this.talukaDataService = talukaDataService;
        this.healthBlockDataService = healthBlockDataService;
        this.healthFacilityDataService = healthFacilityDataService;
        this.villageDataService = villageDataService;
        this.mctsMotherDataService = mctsMotherDataService;
        this.mctsChildDataService = mctsChildDataService;
        this.subscriptionService = subscriptionService;
        this.subscriberService = subscriberService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
    }


    @Override
    @Transactional
    public void importMotherData(Reader reader) throws IOException {
        CsvMapImporter csvImporter = new CsvMapImporter();

        Builder preferenceBuilder = new Builder(CsvPreference.TAB_PREFERENCE);
        preferenceBuilder.skipComments(new DataServiceCommentMatcher());

        csvImporter.open(reader, getMotherProcessorMapping(), preferenceBuilder.build());
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            try {
                importMotherRecord(record);
            } catch (ConstraintViolationException e) {
                throw new CsvImportDataException(String.format("MCTS mother import error, constraints violated: %s",
                        ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
            }
        }
    }

    @Override
    @Transactional
    public void importChildData(Reader reader) throws IOException {
        CsvMapImporter csvImporter = new CsvMapImporter();

        Builder preferenceBuilder = new Builder(CsvPreference.TAB_PREFERENCE);
        preferenceBuilder.skipComments(new DataServiceCommentMatcher());

        csvImporter.open(reader, getChildProcessorMapping(), preferenceBuilder.build());
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            try {
                importChildRecord(record);
            } catch (ConstraintViolationException e) {
                throw new CsvImportDataException(String.format("MCTS child import error, constraints violated: %s",
                        ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
            }
        }
    }


    private void importMotherRecord(Map<String, Object> record) {
        MctsMother mother = (MctsMother) record.get(BENEFICIARY_ID);
        String name = (String) record.get(BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(MSISDN);
        DateTime lmp = (DateTime) record.get(LMP);

        String errors = setLocationFields(record, mother);
        if (errors != null) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.INVALID_LOCATION, SubscriptionPackType.PREGNANCY);
            return;
        }

        if (lmp == null) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.MISSING_LMP, SubscriptionPackType.PREGNANCY);
            return;
        }

        // TODO: more data validation specified in #111
        mother.setName(name);

        processSubscriptionForBeneficiary(mother, msisdn, lmp, SubscriptionPackType.PREGNANCY);
    }


    private void importChildRecord(Map<String, Object> record) {
        MctsChild child = (MctsChild) record.get(BENEFICIARY_ID);
        String name = (String) record.get(BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(MSISDN);
        MctsMother mother = (MctsMother) record.get(MOTHER_ID);
        DateTime dob = (DateTime) record.get(DOB);

        String errors = setLocationFields(record, child);
        if (errors != null) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.INVALID_LOCATION, SubscriptionPackType.CHILD);
            return;
        }

        if (dob == null) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.MISSING_DOB, SubscriptionPackType.CHILD);
            return;
        }
        // TODO: more data validation specified in #111

        child.setName(name);
        child.setMother(mother);

        processSubscriptionForBeneficiary(child, msisdn, dob, SubscriptionPackType.CHILD);
    }


    private void processSubscriptionForBeneficiary(MctsBeneficiary beneficiary, Long msisdn, DateTime referenceDate,
                                                  SubscriptionPackType type) {
        Language language = beneficiary.getDistrict().getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(type);
        Subscriber subscriber = subscriberService.getSubscriber(msisdn);

        if (subscriber == null) {
            // there's no subscriber with this MSISDN, create one

            subscriber = new Subscriber(msisdn, language);
            subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, type);
            subscriberService.create(subscriber);

        } else if (subscriptionService.subscriberHasActiveSubscription(subscriber, type)) {
            // subscriber already has an active subscription to this pack

            MctsBeneficiary existingBeneficiary = (type == SubscriptionPackType.PREGNANCY) ? subscriber.getMother() :
                    subscriber.getChild();

            if (existingBeneficiary == null) {
                // there's already an IVR-originated subscription for this MSISDN
                rejectBeneficiary(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, type);

                // TODO: Do we just reject the subscription request, or do we update the subscriber record with MCTS data?
                // TODO: Should we change the subscription start date based on the provided LMP/DOB?

            } else if (existingBeneficiary.getBeneficiaryId() != beneficiary.getBeneficiaryId()) {
                // if the MCTS ID doesn't match (i.e. there are two beneficiaries with the same phone number), reject the import
                rejectBeneficiary(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, type);
            } else {
                // it's the same beneficiary, treat this import as an update
                subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, type);
                subscriberService.update(subscriber);
            }

            return;
        } else {
            // subscriber exists, but doesn't have a subscription to this pack
            subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, type);
            subscriberService.update(subscriber);
        }

        subscriptionService.createSubscription(msisdn, language, pack, SubscriptionOrigin.MCTS_IMPORT);

    }


    private Subscriber updateSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, DateTime referenceDate,
                                        SubscriptionPackType type) {
        if (type == SubscriptionPackType.PREGNANCY) {
            subscriber.setLastMenstrualPeriod(referenceDate);
            subscriber.setMother((MctsMother) beneficiary);
        } else {
            subscriber.setDateOfBirth(referenceDate);
            subscriber.setChild((MctsChild) beneficiary);
        }
        return subscriber;
    }

    private String setLocationFields(Map<String, Object> record, MctsBeneficiary beneficiary) {
        String errors = null;

        State state = (State) record.get(STATE);
        District district = (District) record.get(DISTRICT);
        Taluka taluka = (Taluka) record.get(TALUKA);
        HealthBlock healthBlock = (HealthBlock) record.get(HEALTH_BLOCK);
        HealthFacility phc = (HealthFacility) record.get(PHC);
        Village village = (Village) record.get(VILLAGE);

        if (state == null || district == null) {
            errors = "District and state must both be set for an MCTS beneficiary.";
        }

        beneficiary.setState(state);
        beneficiary.setDistrict(district);
        beneficiary.setTaluka(taluka);
        beneficiary.setHealthBlock(healthBlock);
        beneficiary.setPrimaryHealthCenter(phc);
        beneficiary.setVillage(village);

        return errors;
    }

    private void rejectBeneficiary(Long msisdn, SubscriptionRejectionReason reason, SubscriptionPackType packType) {
        subscriptionErrorDataService.create(new SubscriptionError(msisdn, reason, packType));
    }

    private Map<String, CellProcessor> getBeneficiaryProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(STATE, new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                State state = stateDataService.findByCode(value);
                verify(null != state, "State does not exist");
                return state;
            }
        });
        mapping.put(DISTRICT, new GetInstanceByLong<District>() {
            @Override
            public District retrieve(Long value) {
                District district = districtDataService.findByCode(value);
                verify(null != district, "District does not exist");
                return district;
            }
        });
        mapping.put(TALUKA, new Optional(new GetInstanceByString<Taluka>() {
            @Override
            public Taluka retrieve(String value) {
                Taluka taluka = talukaDataService.findByCode(value);
                verify(null != taluka, "Taluka does not exist");
                return taluka;
            }
        }));
        mapping.put(HEALTH_BLOCK, new Optional(new GetInstanceByLong<HealthBlock>() {
            @Override
            public HealthBlock retrieve(Long value) {
                HealthBlock healthBlock = healthBlockDataService.findByCode(value);
                verify(null != healthBlock, "Health Block does not exist");
                return healthBlock;
            }
        }));
        mapping.put(PHC, new Optional(new GetInstanceByLong<HealthFacility>() {
            @Override
            public HealthFacility retrieve(Long value) {
                HealthFacility phc = healthFacilityDataService.findById(value);
                verify(null != phc, "Primary Health Center does not exist");
                return phc;
            }
        }));
        mapping.put(VILLAGE, new Optional(new GetInstanceByLong<Village>() {
            @Override
            public Village retrieve(Long value) {
                if (value == 0) { // the sample mother data file has village ID=0 and village name blank
                    return null;
                }

                Village village = villageDataService.findByVcodeAndSvid(value, null);
                if (village == null) {
                    village = villageDataService.findBySvid(value);
                }

                verify(null != village, "Village does not exist");
                return village;
            }
        }));

        return mapping;
    }

    private Map<String, CellProcessor> getMotherProcessorMapping() {
        Map<String, CellProcessor> mapping = getBeneficiaryProcessorMapping();

        mapping.put(BENEFICIARY_ID, new GetInstanceByString<MctsMother>() {
            @Override
            public MctsMother retrieve(String value) {
                MctsMother mother = mctsMotherDataService.findByBeneficiaryId(value);
                if (mother == null) {
                    mother = new MctsMother(value);
                }
                return mother;
            }
        });
        mapping.put(BENEFICIARY_NAME, new GetString());
        mapping.put(MSISDN, new GetLong());
        mapping.put(LMP, new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                if (value == null) {
                    return null;
                }

                DateTime lmp;

                try {
                    DateTimeParser[] parsers = {
                            DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                            DateTimeFormat.forPattern("dd/MM/yyyy").getParser() };
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

                    lmp = formatter.parseDateTime(value);

                } catch (IllegalArgumentException e) {
                    throw new CsvImportDataException(String.format("LMP date %s is invalid", value), e);
                }

                return lmp;
            }
        });

        // TODO: Any other fields needed for mothers? e.g. Abortion, etc.

        return mapping;
    }

    private Map<String, CellProcessor> getChildProcessorMapping() {
        Map<String, CellProcessor> mapping = getBeneficiaryProcessorMapping();

        mapping.put(BENEFICIARY_ID, new GetInstanceByString<MctsChild>() {
            @Override
            public MctsChild retrieve(String value) {
                MctsChild child = mctsChildDataService.findByBeneficiaryId(value);
                if (child == null) {
                    child = new MctsChild(value);
                }
                return child;
            }
        });
        mapping.put(BENEFICIARY_NAME, new GetString());
        mapping.put(MOTHER_ID, new Optional(new GetInstanceByString<MctsMother>() {
            @Override
            public MctsMother retrieve(String value) {
                if (value == null) {
                    return null;
                }
                return mctsMotherDataService.findByBeneficiaryId(value);
            }
        }));
        mapping.put(MSISDN, new GetLong());
        mapping.put(DOB, new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                if (value == null) {
                    return null;
                }

                DateTime dob;

                try {
                    DateTimeParser[] parsers = {
                            DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                            DateTimeFormat.forPattern("dd/MM/yyyy").getParser() };
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

                    dob = formatter.parseDateTime(value);

                } catch (IllegalArgumentException e) {
                    throw new CsvImportDataException(String.format("DOB date %s is invalid", value), e);
                }

                return dob;
            }
        });

        // TODO: Any other fields needed for children?

        return mapping;

    }

    private void verify(boolean condition, String message, String... args) {
        if (!condition) {
            throw new CsvImportDataException(String.format(message, args));
        }
    }

}