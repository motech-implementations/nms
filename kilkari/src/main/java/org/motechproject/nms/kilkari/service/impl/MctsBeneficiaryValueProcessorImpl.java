package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MctsBeneficiaryValueProcessorImpl implements MctsBeneficiaryValueProcessor {

    @Autowired
    private MctsMotherDataService mctsMotherDataService;

    @Autowired
    private MctsChildDataService mctsChildDataService;

    @Override
    public MctsMother getOrCreateMotherInstance(String value) {
        MctsMother mother = mctsMotherDataService.findByBeneficiaryId(value);
        if (mother == null) {
            mother = new MctsMother(value);
        }
        return mother;
    }

    @Override
    public MctsMother getMotherInstanceByBeneficiaryId(String value) {
        if (value == null) {
            return null;
        }
        return mctsMotherDataService.findByBeneficiaryId(value);
    }

    @Override
    public Boolean getAbortionDataFromString(String value) {
        String trimmedValue = value.trim();
        return "Spontaneous".equals(trimmedValue) || "MTP<12 Weeks".equals(trimmedValue) ||
                "MTP>12 Weeks".equals(trimmedValue); // "None" or blank indicates no abortion/miscarriage
    }

    @Override
    public Boolean getStillBirthFromString(String value) {
        return "0".equals(value.trim()); // This value indicates the number of live births that resulted from this pregnancy.
        // 0 implies stillbirth, other values (including blank) do not.
    }

    @Override
    public Boolean getDeathFromString(String value) {
        return "9".equals(value.trim()); // 9 indicates beneficiary death; other values do not
    }

    @Override
    public MctsChild getChildInstanceByString(String value) {
        MctsChild child = mctsChildDataService.findByBeneficiaryId(value);
        if (child == null) {
            child = new MctsChild(value);
        }
        return child;
    }
}
