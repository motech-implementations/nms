package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.DeactivatedBeneficiary;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.repository.DeactivatedBeneficiaryDataService;
import org.motechproject.nms.kilkari.service.DeactivatedBeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("deactivatedBeneficiaryService")
public class DeactivatedBeneficiaryServiceImpl implements DeactivatedBeneficiaryService {

    private DeactivatedBeneficiaryDataService deactivatedBeneficiaryDataService;

    @Autowired
    public DeactivatedBeneficiaryServiceImpl(DeactivatedBeneficiaryDataService deactivatedBeneficiaryDataService) {
        this.deactivatedBeneficiaryDataService = deactivatedBeneficiaryDataService;
    }

    public List<DeactivatedBeneficiary> findByExternalId(String externalId) {
        List<DeactivatedBeneficiary> deactivatedBeneficiaryWithSelectDeactivationReasons = new ArrayList<>();
        List<DeactivatedBeneficiary> deactivatedBeneficiaries = deactivatedBeneficiaryDataService.findByExternalId(externalId);
        if (deactivatedBeneficiaries.size() != 0) {
            for (DeactivatedBeneficiary deactivatedBeneficiary : deactivatedBeneficiaries
                    ) {
                if (!deactivatedBeneficiary.getDeactivationReason().equals(DeactivationReason.LOW_LISTENERSHIP) && !deactivatedBeneficiary.getDeactivationReason().equals(DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED)) {
                    deactivatedBeneficiaryWithSelectDeactivationReasons.add(deactivatedBeneficiary);
                }
            }
        }
        return deactivatedBeneficiaryWithSelectDeactivationReasons;
    }
}
