package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;

public interface MctsBeneficiaryValueProcessor {

    MctsMother getOrCreateMotherInstance(String value);

    MctsMother getMotherInstanceByBeneficiaryId(String value);

    Boolean getAbortionDataFromString(String value);

    Boolean getStillBirthFromString(String value);

    Boolean getDeathFromString(String value);

    MctsChild getChildInstanceByString(String value);
}
