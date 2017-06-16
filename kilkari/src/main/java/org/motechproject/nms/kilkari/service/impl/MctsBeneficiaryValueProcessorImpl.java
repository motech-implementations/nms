package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.exception.InvalidReferenceDateException;
import org.motechproject.nms.kilkari.exception.InvalidRegistrationIdException;
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

    @Override
    public MctsMother getOrCreateRchMotherInstance(String rchId, String mctsId) {

        if (rchId == null || "".equals(rchId.trim())) {
            return null;
        }
        MctsMother motherByRchId = mctsMotherDataService.findByRchId(rchId);
        MctsMother motherByMctsId;

        if (motherByRchId == null) {
            if (mctsId == null) {
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
            if (mctsId == null) {
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
                        throw new InvalidRegistrationIdException("Unrelated Mothers exists with this MctsId and RchId");
                    }
                }
            }
        }
    }

    @Override
    public Boolean getAbortionDataFromString(String value) {
        String trimmedValue = value.trim();
        return "Spontaneous".equals(trimmedValue) || "MTP<12 Weeks".equals(trimmedValue) ||
                "MTP>12 Weeks".equals(trimmedValue) || "Induced".equals(trimmedValue); // "None" or blank indicates no abortion/miscarriage
    }

    @Override
    public Boolean getStillBirthFromString(String value) {
        return "0".equals(value.trim()); // This value indicates the number of live births that resulted from this pregnancy.
        // 0 implies stillbirth, other values (including blank) do not.
    }

    @Override
    public Boolean getDeathFromString(String value) {
        return "9".equals(value.trim()) || "Death".equalsIgnoreCase(value.trim()); // 9 indicates beneficiary death; other values do not
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
                        throw new InvalidRegistrationIdException("Unrelated children exist with the same MCTS id and RCH id");
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
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser()};
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

            referenceDate = formatter.parseDateTime(value);

        } catch (IllegalArgumentException e) {
            throw new InvalidReferenceDateException(String.format("Reference date %s is invalid", value), e);
        }

        return referenceDate;
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
        if (value.length() < KilkariConstants.MSISDN_LENGTH) {
            throw new NumberFormatException("Beneficiary MSISDN too short, must be at least 10 digits");
        }
        String msisdn = value.substring(value.length() - KilkariConstants.MSISDN_LENGTH);

        return Long.parseLong(msisdn);
    }

}
