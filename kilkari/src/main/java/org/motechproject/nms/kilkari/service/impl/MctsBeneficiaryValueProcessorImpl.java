package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.motechproject.nms.region.utils.LocationConstants.DISTRICT_ID;
import static org.motechproject.nms.region.utils.LocationConstants.STATE_ID;

@Service("mctsBeneficiaryValueProcessor")
public class MctsBeneficiaryValueProcessorImpl implements MctsBeneficiaryValueProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryValueProcessorImpl.class);
    @Autowired
    private MctsMotherDataService mctsMotherDataService;

    @Autowired
    private MctsChildDataService mctsChildDataService;

    @Override
    public MctsMother getOrCreateMotherInstance(String value) {
        if (value == null || "".equals(value.trim())) {
            return null;
        }
        MctsMother mother = mctsMotherDataService.findByBeneficiaryId(value);
        if (mother == null) {
            mother = new MctsMother(value);
        }
        return mother;
    }

    @Override
    public MctsMother getMotherInstanceByBeneficiaryId(String value) {
        if (value == null || "".equals(value.trim())) {
            return null;
        }
        return getOrCreateMotherInstance(value);
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    public MctsMother getOrCreateRchMotherInstance(String rchId, String mctsId) {

        if (rchId == null || "".equals(rchId.trim())) {
            return null;
        }
        MctsMother motherByRchId = mctsMotherDataService.findByRchId(rchId);
        MctsMother motherByMctsId;
        if (motherByRchId == null) {
            if (mctsId == null || ("NULL").equalsIgnoreCase(mctsId) || mctsId.isEmpty() || "".equals(mctsId.trim())) {
                motherByRchId = new MctsMother(rchId, null);
                return motherByRchId;
            } else {
                motherByMctsId = mctsMotherDataService.findByBeneficiaryId(mctsId);
                if (motherByMctsId == null) {
                    motherByRchId = new MctsMother(rchId, mctsId);
                    return motherByRchId;
                } else {
                    if(motherByMctsId.getRchId() != null && !rchId.equals(motherByMctsId.getRchId())){
                        return null;
                    }
                    motherByMctsId.setRchId(rchId);
                    return motherByMctsId;
                }
            }
        } else {
            if (mctsId == null || ("NULL").equalsIgnoreCase(mctsId) || "".equals(mctsId.trim())) {
                return motherByRchId;
            } else {
                motherByMctsId = mctsMotherDataService.findByBeneficiaryId(mctsId);
                if (motherByMctsId == null) {// removed the condition motherByRchId.getBeneficiaryId() != null to fix "null mcts field update" issue
                    motherByRchId.setBeneficiaryId(mctsId);
                    return motherByRchId;
                } else {
                    if (motherByMctsId != null && motherByRchId.getId().equals(motherByMctsId.getId())) {
                        return motherByRchId;
                    } else {
                       return null;
                    }
                }
            }
        }
    }

    @Override
    public Boolean getAbortionDataFromString(String value) {
        if (value != null) {
            String trimmedValue = value.trim();
            return "Spontaneous".equals(trimmedValue) || "MTP<12 Weeks".equals(trimmedValue) ||
                    "MTP>12 Weeks".equals(trimmedValue) || "Induced".equals(trimmedValue); // "None" or blank indicates no abortion/miscarriage
        } else {
            return null;
        }
    }

    @Override
    public Boolean getStillBirthFromString(String value) {
        if (value != null) {
            return "0".equals(value.trim()); // This value indicates the number of live births that resulted from this pregnancy.
            // 0 implies stillbirth, other values (including blank) do not.
        } else {
            return null;
        }
    }

    @Override
    public Boolean getDeathFromString(String value) {
        if (value != null) {
            return "Death".equalsIgnoreCase(value.trim()); // 9 indicates beneficiary death; other values do not
        } else {
            return null;
        }
    }

    @Override
    public MctsChild getOrCreateChildInstance(String value) {
        if (value == null || "".equals(value.trim())) {
            return null;
        }
        MctsChild child = mctsChildDataService.findByBeneficiaryId(value);
        if (child == null) {
            child = new MctsChild(value);
        }
        return child;
    }

    @Override
    public MctsChild getOrCreateRchChildInstance(String rchId, String mctsId) {
        if (rchId == null || "".equals(rchId.trim())) {
            return null;
        }
        MctsChild childByRchId = mctsChildDataService.findByRchId(rchId);
        MctsChild childByMctsId;
        if (childByRchId == null) {
            if (mctsId == null || mctsId.isEmpty() || "NULL".equalsIgnoreCase(mctsId) || "".equals(mctsId.trim())) {
                childByRchId = new MctsChild(rchId, null);
                return childByRchId;
            } else {
                childByMctsId = mctsChildDataService.findByBeneficiaryId(mctsId);
                if (childByMctsId == null) {
                    childByRchId = new MctsChild(rchId, mctsId);
                    return childByRchId;
                } else {
                    childByMctsId.setRchId(rchId);
                    return childByMctsId;
                }
            }
        } else {
            if (mctsId == null || "NULL".equalsIgnoreCase(mctsId) || "".equals(mctsId.trim()) ) {
                return childByRchId;
            } else {
                childByMctsId = mctsChildDataService.findByBeneficiaryId(mctsId);
                if (childByMctsId == null) {
                    childByRchId.setBeneficiaryId(mctsId);
                    return childByRchId;
                } else {
                    if (childByRchId.getId().equals(childByMctsId.getId())) {
                        return childByRchId;
                    } else {
                                return null;
                    }
                }
            }
        }
    }

    @Override
    public DateTime getDateByString(String value) {
        if (value == null) {
            return null;
        }

        DateTime referenceDate;

        try {
            DateTimeParser[] parsers = {
                    DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                    DateTimeFormat.forPattern("dd/MM/yyyy").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").getParser()};
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

            referenceDate = formatter.parseDateTime(value);

        } catch (IllegalArgumentException e) {
           return null;
        }

        return referenceDate;
    }
    @Override
    public LocalDate getLocalDateByString(String value) {
        if (value == null) {
            return null;
        }

        DateTime referenceDate = this.getDateByString(value);

        return referenceDate == null ? null : referenceDate.toLocalDate();
    }

    @Override
    public Long getCaseNoByString(String value) {

        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Override
    public Long getMsisdnByString(String value) {
        String msisdn;
        if (value.length() < KilkariConstants.MSISDN_LENGTH) {
            msisdn = value;
        } else if (value.length() == (KilkariConstants.MSISDN_LENGTH + 2) && (("91").equals(value.substring(0, 2)))) {
             msisdn = value.substring(value.length() - KilkariConstants.MSISDN_LENGTH);
        } else if (value.length() == (KilkariConstants.MSISDN_LENGTH + 3) && (("+91").equals(value.substring(0, 3)))) {
            msisdn = value.substring(value.length() - KilkariConstants.MSISDN_LENGTH);
        } else {
            msisdn = value;
        }
        if (msisdn.isEmpty()) {
            return (long) 0;
        }
        return Long.parseLong(msisdn);
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    public void setLocationFieldsCSV(LocationFinder locationFinder, Map<String, Object> record, MctsBeneficiary beneficiary) throws InvalidLocationException {

        LOGGER.debug("Enter:: setLocationFieldsCSV %s" , beneficiary.getId());
        String mapMainKey = new String(record.get(STATE_ID).toString());
        StringBuffer mapKey = new StringBuffer(record.get(KilkariConstants.STATE_ID).toString());
        if (isValidID(record, KilkariConstants.STATE_ID) && (locationFinder.getStateHashMap().get(mapKey.toString()) != null)) {
            beneficiary.setState(locationFinder.getStateHashMap().get(mapKey.toString()));
            String districtCode = record.get(KilkariConstants.DISTRICT_ID).toString();
            mapKey.append("_");
            mapKey.append(districtCode);
            if (isValidID(record, KilkariConstants.DISTRICT_ID) && (locationFinder.getDistrictHashMap().get(mapKey.toString()) != null)) {
                beneficiary.setDistrict(locationFinder.getDistrictHashMap().get(mapKey.toString()));
                String talukaCode = record.get(KilkariConstants.TALUKA_ID) == null ? "0" : record.get(KilkariConstants.TALUKA_ID).toString().trim();
                Taluka taluka = locationFinder.getTalukaHashMap().get(mapMainKey + "_" + talukaCode);
                LOGGER.debug("taluka code: {}", talukaCode.toString());
                LOGGER.debug("Taluka: {}", taluka == null ? null : taluka.getId());
                if (taluka != null && taluka.getId() != null) {
                    beneficiary.setTaluka(taluka);
                } else {
                    beneficiary.setTaluka(null);
//                    beneficiary.setHealthBlock(null);
//                    beneficiary.setHealthFacility(null);
//                    beneficiary.setHealthSubFacility(null);
//                    beneficiary.setVillage(null);
//                    return;
                }

                String villageSvid = record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID).toString();
                String villageCode = record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.CENSUS_VILLAGE_ID).toString();
                String healthBlockCode = record.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? "0" : record.get(KilkariConstants.HEALTH_BLOCK_ID).toString();
                String healthFacilityCode = record.get(KilkariConstants.PHC_ID) == null ? "0" : record.get(KilkariConstants.PHC_ID).toString();
                String healthSubFacilityCode = record.get(KilkariConstants.SUB_CENTRE_ID) == null ? "0" : record.get(KilkariConstants.SUB_CENTRE_ID).toString();

                LOGGER.debug("State: {}, District: {}, Taluka: {}, VilageSvid: {}, VillageCode: {}, HB: {}, HF: {}, HSF: {}", record.get(KilkariConstants.STATE_ID).toString(), districtCode,
                        talukaCode, villageSvid, villageCode, healthBlockCode, healthFacilityCode, healthSubFacilityCode);
                Village village = locationFinder.getVillageHashMap().get(mapMainKey + "_" + Long.parseLong(villageCode) + "_" + Long.parseLong(villageSvid));
                if (village != null && village.getId() != null) {
                    beneficiary.setVillage(village);
                } else {
                    beneficiary.setVillage(null);
                }
                HealthBlock healthBlock = locationFinder.getHealthBlockHashMap().get(mapMainKey + "_" + Long.parseLong(healthBlockCode));
                if (healthBlock != null && healthBlock.getId() != null) {
                    beneficiary.setHealthBlock(healthBlock);
                } else {
                    beneficiary.setHealthBlock(null);
                    /*beneficiary.setHealthFacility(null);
                    beneficiary.setHealthSubFacility(null);
                    beneficiary.setVillage(null);
                    return;*/
                }
                HealthFacility healthFacility = locationFinder.getHealthFacilityHashMap().get(mapMainKey + "_" + Long.parseLong(healthFacilityCode));
                if (healthFacility != null && healthFacility.getId() != null) {
                    beneficiary.setHealthFacility(healthFacility);
                } else {
                    beneficiary.setHealthFacility(null);
                    /*beneficiary.setHealthSubFacility(null);
                    return;*/
                }
                HealthSubFacility healthSubFacility = locationFinder.getHealthSubFacilityHashMap().get(mapMainKey + "_" + Long.parseLong(healthSubFacilityCode));
                if (healthSubFacility != null && healthSubFacility.getId() != null) {
                    beneficiary.setHealthSubFacility(healthSubFacility);
                } else {
                    beneficiary.setHealthSubFacility(null);
                    return;
                }
            } else {
                throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.DISTRICT_ID, record.get(KilkariConstants.DISTRICT_ID)));
            }
        } else {
            throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.STATE_ID, record.get(KilkariConstants.STATE_ID)));
        }
        LOGGER.debug("Exit:: setLocationFieldsCSV");
    }

    @Override
    public void checkLocationFieldsCSV(LocationFinder locationFinder, Map<String, Object> record, MctsBeneficiary beneficiary) throws InvalidLocationException {

        StringBuffer mapKey = new StringBuffer(record.get(KilkariConstants.STATE_ID).toString());
        if (isValidID(record, KilkariConstants.STATE_ID) && (locationFinder.getStateHashMap().get(mapKey.toString()) != null)) {
            String districtCode = record.get(KilkariConstants.DISTRICT_ID).toString();
            mapKey.append("_");
            mapKey.append(districtCode);

            if (isValidID(record, KilkariConstants.DISTRICT_ID) && (locationFinder.getDistrictHashMap().get(mapKey.toString()) != null)) {
                LOGGER.debug("State: {}, District: {}" , record.get(KilkariConstants.STATE_ID).toString(), districtCode);
            } else {
                throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.DISTRICT_ID, record.get(KilkariConstants.DISTRICT_ID)));
            }
        } else {
            throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.STATE_ID, record.get(KilkariConstants.STATE_ID)));
        }
        LOGGER.debug("Exit:: checkLocationFieldsCSV");
    }

    private boolean isValidID(final Map<String, Object> map, final String key) {
        Object obj = map.get(key);
        if (obj == null || obj.toString().isEmpty() || "NULL".equalsIgnoreCase(obj.toString())) {
            return false;
        }

        if (obj.getClass().equals(Long.class)) {
            return (Long) obj > 0L;
        }

        return !"0".equals(obj);
    }

}
