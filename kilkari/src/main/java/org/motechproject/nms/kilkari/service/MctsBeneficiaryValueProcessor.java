package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.exception.InvalidLocationException;

import java.util.Map;


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

    void setLocationFieldsCSV(LocationFinder locationFinder, Map<String, Object> record, MctsBeneficiary beneficiary) throws InvalidLocationException;

    void checkLocationFieldsCSV(LocationFinder locationFinder, Map<String, Object> record, MctsBeneficiary beneficiary) throws InvalidLocationException;
}
