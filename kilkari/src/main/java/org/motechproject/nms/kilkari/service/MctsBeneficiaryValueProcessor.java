package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;

public interface MctsBeneficiaryValueProcessor {

    MctsMother getOrCreateMotherInstance(String value);

    MctsMother getMotherInstanceByBeneficiaryId(String value);

    Boolean getAbortionDataFromString(String value);

    Boolean getStillBirthFromString(String value);

    Boolean getDeathFromString(String value);

    MctsChild getChildInstanceByString(String value);

    DateTime getDateByString(String value);

    Long getMsisdnByString(String value);

    Long getCaseNoByString(String value);

    MctsMother getOrCreateRchMotherInstance(String rchId, String mctsId);

    MctsChild getOrCreateRchChildInstance(String rchId, String mctsId);
}
