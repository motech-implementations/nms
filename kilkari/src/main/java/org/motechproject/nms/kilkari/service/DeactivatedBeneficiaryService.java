package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.DeactivatedBeneficiary;

import java.util.List;

/**
 * Created by vishnu on 12/4/18.
 */
public interface DeactivatedBeneficiaryService {

    List<DeactivatedBeneficiary> findDeactivatedBeneficiariesOtherThanManualDeactivation(String externalId);
}
