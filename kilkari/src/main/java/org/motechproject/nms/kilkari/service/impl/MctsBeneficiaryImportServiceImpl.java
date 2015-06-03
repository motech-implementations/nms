package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.LanguageLocation;
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

    private StateDataService stateDataService;
    private DistrictDataService districtDataService;
    private TalukaDataService talukaDataService;
    private HealthBlockDataService healthBlockDataService;
    private HealthFacilityDataService healthFacilityDataService;
    private VillageDataService villageDataService;
    private MctsMotherDataService mctsMotherDataService;
    private SubscriptionService subscriptionService;
    private SubscriberService subscriberService;
    private SubscriptionPackDataService subscriptionPackDataService;

    @Autowired
    MctsBeneficiaryImportServiceImpl(StateDataService stateDataService, DistrictDataService districtDataService,
                                     TalukaDataService talukaDataService, HealthBlockDataService healthBlockDataService,
                                     HealthFacilityDataService healthFacilityDataService,
                                     VillageDataService villageDataService, MctsMotherDataService mctsMotherDataService,
                                     SubscriptionService subscriptionService, SubscriberService subscriberService,
                                     SubscriptionPackDataService subscriptionPackDataService) {
        this.stateDataService = stateDataService;
        this.districtDataService = districtDataService;
        this.talukaDataService = talukaDataService;
        this.healthBlockDataService = healthBlockDataService;
        this.healthFacilityDataService = healthFacilityDataService;
        this.villageDataService = villageDataService;
        this.mctsMotherDataService = mctsMotherDataService;
        this.subscriptionService = subscriptionService;
        this.subscriberService = subscriberService;
        this.subscriptionPackDataService = subscriptionPackDataService;
    }


    @Override
    @Transactional
    public void importMotherData(Reader reader) throws IOException {
        CsvMapImporter csvImporter = new CsvMapImporter();
        csvImporter.open(reader, getMotherProcessorMapping(), CsvPreference.TAB_PREFERENCE);
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            try {
                importMotherRecord(record);
            } catch (ConstraintViolationException e) {
                throw new CsvImportDataException(String.format("CSV import error, constraints violated: %s",
                        ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
            }
        }
    }

    @Override
    @Transactional
    public void importChildData(Reader reader) throws IOException {
        CsvMapImporter csvImporter = new CsvMapImporter();
        csvImporter.open(reader, getChildProcessorMapping(), CsvPreference.TAB_PREFERENCE);
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            try {
                importChildRecord(record);
            } catch (ConstraintViolationException e) {
                throw new CsvImportDataException(String.format("CSV import error, constraints violated: %s",
                        ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
            }
        }
    }


    private void importMotherRecord(Map<String, Object> record) {
        State state = (State) record.get(STATE);
        District district = (District) record.get(DISTRICT);
        Taluka taluka = (Taluka) record.get(TALUKA);
        HealthBlock healthBlock = (HealthBlock) record.get(HEALTH_BLOCK);
        HealthFacility phc = (HealthFacility) record.get(PHC);
        Village village = (Village) record.get(VILLAGE);
        MctsMother mother = (MctsMother) record.get(BENEFICIARY_ID);
        String name = (String) record.get(BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(MSISDN);
        DateTime lmp = (DateTime) record.get(LMP);

        // TODO: more data validation specified in #111
        if (state == null || district == null) {
            rejectBeneficiary();
        }

        mother.setState(state);
        mother.setDistrict(district);
        mother.setTaluka(taluka);
        mother.setHealthBlock(healthBlock);
        mother.setPrimaryHealthCenter(phc);
        mother.setVillage(village);
        mother.setName(name);

        processSubscriptionForBeneficiary(mother, msisdn, lmp, SubscriptionPackType.PREGNANCY);
    }

    private void processSubscriptionForBeneficiary(MctsBeneficiary beneficiary, Long msisdn, DateTime referenceDate,
                                                  SubscriptionPackType type) {
        LanguageLocation languageLocation = beneficiary.getDistrict().getLanguageLocation();
        SubscriptionPack pack = subscriptionPackDataService.byType(type);
        Subscriber subscriber = subscriberService.getSubscriber(msisdn);

        if (subscriber == null) {
            // there's no subscriber with this MSISDN, create one

            subscriber = new Subscriber(msisdn, languageLocation);
            subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, type);
            subscriberService.create(subscriber);

        } else if (subscriptionService.subscriberHasActiveSubscription(subscriber, type)) {
            // subscriber already has an active subscription to this pack

            MctsBeneficiary existingBeneficiary = (type == SubscriptionPackType.PREGNANCY) ? subscriber.getMother() :
                    subscriber.getChild();

            if (existingBeneficiary == null) {

                // TODO: what do we do if someone has an IVR-originated subscription and then they are imported via MCTS?
                // Does the MCTS data win, even though it might be a shared phone (and hence different beneficiary)?

            } else if (existingBeneficiary.getBeneficiaryId() != beneficiary.getBeneficiaryId()) {
                // if the MCTS ID doesn't match (i.e. there are two beneficiaries with the same phone number), reject the import
                rejectBeneficiary();
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

        subscriptionService.createSubscription(msisdn, languageLocation, pack, SubscriptionOrigin.MCTS_IMPORT);

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

    private void importChildRecord(Map<String, Object> record) {
        State state = (State) record.get(STATE);
        District district = (District) record.get(DISTRICT);
        Taluka taluka = (Taluka) record.get(TALUKA);
        HealthBlock healthBlock = (HealthBlock) record.get(HEALTH_BLOCK);
        HealthFacility phc = (HealthFacility) record.get(PHC);
        Village village = (Village) record.get(VILLAGE);
        MctsChild child = (MctsChild) record.get(BENEFICIARY_ID);
        String name = (String) record.get(BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(MSISDN);

        // TODO: more data validation specified in #111
        if (state == null || district == null) {
            rejectBeneficiary();
        }

        child.setState(state);
        child.setDistrict(district);
        child.setTaluka(taluka);
        child.setHealthBlock(healthBlock);
        child.setPrimaryHealthCenter(phc);
        child.setVillage(village);
        child.setName(name);

        processSubscriptionForBeneficiary(child, msisdn, new DateTime() /* TODO */, SubscriptionPackType.CHILD);
    }

    private void rejectBeneficiary() {
        // log in subscription error table
    }

    private Map<String, CellProcessor> getBeneficiaryProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(STATE, new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                return stateDataService.findByCode(value);
            }
        });
        mapping.put(DISTRICT, new GetInstanceByLong<District>() {
            @Override
            public District retrieve(Long value) {
                return districtDataService.findByCode(value);
            }
        });
        mapping.put(TALUKA, new Optional(new GetInstanceByString<Taluka>() {
            @Override
            public Taluka retrieve(String value) {
                return talukaDataService.findByCode(value);
            }
        }));
        mapping.put(HEALTH_BLOCK, new Optional(new GetInstanceByLong<HealthBlock>() {
            @Override
            public HealthBlock retrieve(Long value) {
                return healthBlockDataService.findByCode(value);
            }
        }));
        mapping.put(PHC, new Optional(new GetInstanceByLong<HealthFacility>() {
            @Override
            public HealthFacility retrieve(Long value) {
                return healthFacilityDataService.findById(value);
            }
        }));
        mapping.put(VILLAGE, new Optional(new GetInstanceByLong<Village>() {
            @Override
            public Village retrieve(Long value) {
                return villageDataService.findById(value);
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
                DateTime lmp = DateTime.parse(value);
                // handle invalid date?
                return lmp;
            }
        });

        // TODO: Any other fields needed for mothers? e.g. Abortion, etc.

        return mapping;
    }

    private Map<String, CellProcessor> getChildProcessorMapping() {
        Map<String, CellProcessor> mapping = getBeneficiaryProcessorMapping();

        // TODO: Any other fields needed for children? Do the field names match for mother/child?

        return mapping;

    }

}