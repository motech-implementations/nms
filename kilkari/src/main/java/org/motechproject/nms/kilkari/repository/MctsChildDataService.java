package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.MctsChild;

public interface MctsChildDataService extends MotechDataService<MctsChild> {

    @Lookup
    MctsChild findByBeneficiaryId(@LookupField(name = "beneficiaryId") String beneficiaryId);

    @Lookup
    MctsChild findByRchId(@LookupField(name = "rchId") String rchId);

}
