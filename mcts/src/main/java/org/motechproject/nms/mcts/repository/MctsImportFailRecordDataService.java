package org.motechproject.nms.mcts.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.MotechDataService;
import org.joda.time.LocalDate;
import org.motechproject.mds.util.Constants;
import org.motechproject.nms.mcts.domain.MctsImportFailRecord;
import org.motechproject.nms.mcts.domain.MctsUserType;

import java.util.List;

/**
 * Data service to CRUD on MCTS import failures
 */
public interface MctsImportFailRecordDataService extends MotechDataService<MctsImportFailRecord> {

    @Lookup
    List<MctsImportFailRecord> getByStateAndImportdateAndUsertype(@LookupField(name = "stateCode") Long stateCode,
                                                            @LookupField(name = "importDate", customOperator = Constants.Operators.GT_EQ) LocalDate importDate,
                                                            @LookupField(name = "userType") MctsUserType userType,
                                                            QueryParams queryParams);


}

