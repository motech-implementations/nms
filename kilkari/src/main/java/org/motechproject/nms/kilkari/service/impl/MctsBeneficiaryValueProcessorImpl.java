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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.annotations.Transactional;
import java.util.Map;

@Service("mctsBeneficiaryValueProcessor")
public class MctsBeneficiaryValueProcessorImpl implements MctsBeneficiaryValueProcessor {

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
            if (mctsId == null || ("NULL").equalsIgnoreCase(mctsId)) {
                motherByRchId = new MctsMother(rchId, null);
                return motherByRchId;
            } else {
                motherByMctsId = mctsMotherDataService.findByBeneficiaryId(mctsId);
                if (motherByMctsId == null) {
                    motherByRchId = new MctsMother(rchId, mctsId);
                    return motherByRchId;
                } else {
                    motherByMctsId.setRchId(rchId);
                    return motherByMctsId;
                }
            }
        } else {
            if (mctsId == null || ("NULL").equalsIgnoreCase(mctsId)) {
                return motherByRchId;
            } else {
                motherByMctsId = mctsMotherDataService.findByBeneficiaryId(mctsId);
                if (motherByMctsId == null) {
                    motherByRchId.setBeneficiaryId(mctsId);
                    return motherByRchId;
                } else {
                    if (motherByRchId.getId().equals(motherByMctsId.getId())) {
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
            return "9".equals(value.trim()) || "Death".equalsIgnoreCase(value.trim()); // 9 indicates beneficiary death; other values do not
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
            if (mctsId == null) {
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
            if (mctsId == null) {
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
    @Transactional
    public void setLocationFieldsCSV(LocationFinder locationFinder, Map<String, Object> record, MctsBeneficiary beneficiary) throws InvalidLocationException {

        StringBuffer mapKey = new StringBuffer(record.get(KilkariConstants.STATE_ID).toString());
        if (isValidID(record, KilkariConstants.STATE_ID) && (locationFinder.getStateHashMap().get(mapKey.toString()) != null)) {
            beneficiary.setState(locationFinder.getStateHashMap().get(mapKey.toString()));
            String districtCode = record.get(KilkariConstants.DISTRICT_ID).toString();
            mapKey.append("_");
            mapKey.append(districtCode);

            if (isValidID(record, KilkariConstants.DISTRICT_ID) && (locationFinder.getDistrictHashMap().get(mapKey.toString()) != null)) {
                beneficiary.setDistrict(locationFinder.getDistrictHashMap().get(mapKey.toString()));
                Long talukaCode = Long.parseLong(record.get(KilkariConstants.TALUKA_ID) == null ? "0" : record.get(KilkariConstants.TALUKA_ID).toString().trim());
                mapKey.append("_");
                mapKey.append(talukaCode);
                Taluka taluka = locationFinder.getTalukaHashMap().get(mapKey.toString());
                if (taluka != null && taluka.getId() != null) {
                    beneficiary.setTaluka(taluka);
                } else {
                    beneficiary.setTaluka(null);
                }

                String villageSvid = record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID).toString();
                String villageCode = record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.CENSUS_VILLAGE_ID).toString();
                String healthBlockCode = record.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? "0" : record.get(KilkariConstants.HEALTH_BLOCK_ID).toString();
                String healthFacilityCode = record.get(KilkariConstants.PHC_ID) == null ? "0" : record.get(KilkariConstants.PHC_ID).toString();
                String healthSubFacilityCode = record.get(KilkariConstants.SUB_CENTRE_ID) == null ? "0" : record.get(KilkariConstants.SUB_CENTRE_ID).toString();

                Village village = locationFinder.getVillageHashMap().get(mapKey.toString() + "_" + Long.parseLong(villageCode) + "_" + Long.parseLong(villageSvid));
                if (village != null && village.getId() != null) {
                    beneficiary.setVillage(village);
                } else {
                    beneficiary.setVillage(null);
                }
                mapKey = new StringBuffer(record.get(KilkariConstants.STATE_ID).toString() + "_" + districtCode);
                mapKey.append("_");
                mapKey.append(Long.parseLong(healthBlockCode));
                HealthBlock healthBlock = locationFinder.getHealthBlockHashMap().get(mapKey.toString());
                if (healthBlock != null && healthBlock.getId() != null) {
                    beneficiary.setHealthBlock(healthBlock);
                } else {
                    beneficiary.setHealthBlock(null);
                }
                mapKey.append("_");
                mapKey.append(Long.parseLong(healthFacilityCode));
                HealthFacility healthFacility = locationFinder.getHealthFacilityHashMap().get(mapKey.toString());
                if (healthFacility != null && healthFacility.getId() != null) {
                    beneficiary.setHealthFacility(healthFacility);
                } else {
                    beneficiary.setHealthFacility(null);
                }
                mapKey.append("_");
                mapKey.append(Long.parseLong(healthSubFacilityCode));
                HealthSubFacility healthSubFacility = locationFinder.getHealthSubFacilityHashMap().get(mapKey.toString());
                if (healthSubFacility != null && healthSubFacility.getId() != null) {
                    beneficiary.setHealthSubFacility(healthSubFacility);
                } else {
                    beneficiary.setHealthSubFacility(null);
                }
            } else {
                throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.DISTRICT_ID, record.get(KilkariConstants.DISTRICT_ID)));
            }
        } else {
            throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.STATE_ID, record.get(KilkariConstants.STATE_ID)));
        }
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
