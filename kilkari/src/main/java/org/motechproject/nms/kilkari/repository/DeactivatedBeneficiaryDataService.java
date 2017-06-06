package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.DeactivatedBeneficiary;

import java.util.List;

/**
 * Created by ajai on 5/6/17.
 */
public interface DeactivatedBeneficiaryDataService extends MotechDataService<DeactivatedBeneficiary> {

    @Lookup
    List<DeactivatedBeneficiary> findByExternalId(@LookupField(name = "externalId") String externalId);

}
