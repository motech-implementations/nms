package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
