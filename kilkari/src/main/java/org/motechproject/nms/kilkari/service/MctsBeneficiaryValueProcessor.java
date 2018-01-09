package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;


public interface MctsBeneficiaryValueProcessor {

    MctsMother getOrCreateMotherInstance(String value);

    MctsMother getMotherInstanceByBeneficiaryId(String value);

    MctsMother getOrCreateRchMotherInstance(String rchId, String mctsId);

    Boolean getAbortionDataFromString(String value);

    Boolean getStillBirthFromString(String value);

    Boolean getDeathFromString(String value);

    MctsChild getOrCreateChildInstance(String value);

    DateTime getDateByString(String value);

    LocalDate getLocalDateByString(String value);

    Long getCaseNoByString(String value);

    Long getMsisdnByString(String value);

    MctsChild getOrCreateRchChildInstance(String childId, String mctsId);
}
