package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.MctsMother;

public interface MctsMotherDataService extends MotechDataService<MctsMother> {

    @Lookup
    MctsMother findByBeneficiaryId(@LookupField(name = "beneficiaryId") String beneficiaryId);

    @Lookup
    MctsMother findByRchId(@LookupField(name = "rchId") String rchId);
}
