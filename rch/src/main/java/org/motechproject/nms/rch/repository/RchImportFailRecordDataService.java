package org.motechproject.nms.rch.repository;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rch.domain.RchImportFailRecord;
import org.motechproject.mds.util.Constants;
import org.motechproject.nms.rch.domain.RchUserType;

import java.util.List;

/**
 * Created by beehyvsc on 1/6/17.
 */
public interface RchImportFailRecordDataService extends MotechDataService<RchImportFailRecord> {
    @Lookup
    List<RchImportFailRecord> getByStateAndImportdateAndUsertype(@LookupField(name = "stateCode") Long stateCode,
                                                                 @LookupField(name = "importDate", customOperator = Constants.Operators.GT_EQ)LocalDate importDate,
                                                                 @LookupField(name = "userType") RchUserType userType,
                                                                 QueryParams queryParams);
}
