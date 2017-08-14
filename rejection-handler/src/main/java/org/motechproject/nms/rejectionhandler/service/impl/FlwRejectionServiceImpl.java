package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.repository.FlwImportRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.FlwRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by vishnu on 15/7/17.
 */
@Service("flwRejectionService")
public class FlwRejectionServiceImpl implements FlwRejectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlwRejectionServiceImpl.class);

    @Autowired
    private FlwImportRejectionDataService flwImportRejectionDataService;

    @Override
    public FlwImportRejection findByFlwIdAndStateId(Long flwId, Long stateId) {
        return flwImportRejectionDataService.findByFlwIdAndStateId(flwId, stateId);
    }

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public void createUpdate(FlwImportRejection flwImportRejection) {
        if (flwImportRejection.getFlwId() != null && flwImportRejection.getStateId() != null) {
            FlwImportRejection flwImportRejection1 = findByFlwIdAndStateId(flwImportRejection.getFlwId(), flwImportRejection.getStateId());

            if (flwImportRejection1 == null && !flwImportRejection.getAccepted()) {
                flwImportRejectionDataService.create(flwImportRejection);
            } else if (flwImportRejection1 == null && flwImportRejection.getAccepted()) {
                LOGGER.debug(String.format("There is no rejection data for flwId %s and stateId %s", flwImportRejection.getFlwId().toString(), flwImportRejection.getStateId().toString()));
            } else if (flwImportRejection1 != null && !flwImportRejection1.getAccepted()) {
                flwImportRejection1 = setNewData(flwImportRejection, flwImportRejection1);
                flwImportRejectionDataService.update(flwImportRejection1);
            } else if (flwImportRejection1 != null && flwImportRejection1.getAccepted()) {
                flwImportRejection1 = setNewData(flwImportRejection, flwImportRejection1);
                flwImportRejectionDataService.update(flwImportRejection1);
            }
        }
    }

    private static FlwImportRejection setNewData(FlwImportRejection flwImportRejection, FlwImportRejection flwImportRejection1) {
        flwImportRejection1.setStateId(flwImportRejection.getStateId());
        flwImportRejection1.setDistrictId(flwImportRejection.getDistrictId());
        flwImportRejection1.setDistrictName(flwImportRejection.getDistrictName());
        flwImportRejection1.setTalukaId(flwImportRejection.getTalukaId());
        flwImportRejection1.setTalukaName(flwImportRejection.getTalukaName());
        flwImportRejection1.setHealthBlockId(flwImportRejection.getHealthBlockId());
        flwImportRejection1.setHealthBlockName(flwImportRejection.getHealthBlockName());
        flwImportRejection1.setPhcId(flwImportRejection.getPhcId());
        flwImportRejection1.setPhcName(flwImportRejection.getPhcName());
        flwImportRejection1.setSubcentreId(flwImportRejection.getSubcentreId());
        flwImportRejection1.setSubcentreName(flwImportRejection.getSubcentreName());
        flwImportRejection1.setVillageId(flwImportRejection.getVillageId());
        flwImportRejection1.setVillageName(flwImportRejection.getVillageName());
        flwImportRejection1.setFlwId(flwImportRejection.getFlwId());
        flwImportRejection1.setMsisdn(flwImportRejection.getMsisdn());
        flwImportRejection1.setGfName(flwImportRejection.getGfName());
        flwImportRejection1.setGfStatus(flwImportRejection.getGfStatus());
        flwImportRejection1.setExecDate(flwImportRejection.getExecDate());
        flwImportRejection1.setRegDate(flwImportRejection.getRegDate());
        flwImportRejection1.setSex(flwImportRejection.getSex());
        flwImportRejection1.setType(flwImportRejection.getType());
        flwImportRejection1.setSmsReply(flwImportRejection.getSmsReply());
        flwImportRejection1.setAadharNo(flwImportRejection.getAadharNo());
        flwImportRejection1.setCreatedOn(flwImportRejection.getCreatedOn());
        flwImportRejection1.setUpdatedOn(flwImportRejection.getUpdatedOn());
        flwImportRejection1.setBankId(flwImportRejection.getBankId());
        flwImportRejection1.setBranchName(flwImportRejection.getBranchName());
        flwImportRejection1.setIfscIdCode(flwImportRejection.getIfscIdCode());
        flwImportRejection1.setBankName(flwImportRejection.getBankName());
        flwImportRejection1.setAccountNumber(flwImportRejection.getAccountNumber());
        flwImportRejection1.setAadharLinked(flwImportRejection.getAadharLinked());
        flwImportRejection1.setVerifyDate(flwImportRejection.getVerifyDate());
        flwImportRejection1.setVerifierName(flwImportRejection.getVerifierName());
        flwImportRejection1.setVerifierId(flwImportRejection.getVerifierId());
        flwImportRejection1.setCallAns(flwImportRejection.getCallAns());
        flwImportRejection1.setPhoneNoCorrect(flwImportRejection.getPhoneNoCorrect());
        flwImportRejection1.setNoCallReason(flwImportRejection.getNoCallReason());
        flwImportRejection1.setNoPhoneReason(flwImportRejection.getNoPhoneReason());
        flwImportRejection1.setVerifierRemarks(flwImportRejection.getVerifierRemarks());
        flwImportRejection1.setGfAddress(flwImportRejection.getGfAddress());
        flwImportRejection1.setHusbandName(flwImportRejection.getHusbandName());
        flwImportRejection1.setAccepted(flwImportRejection.getAccepted());
        flwImportRejection1.setRejectionReason(flwImportRejection.getRejectionReason());
        flwImportRejection1.setSource(flwImportRejection.getSource());
        flwImportRejection1.setAction(flwImportRejection.getAction());
        return flwImportRejection1;
    }
}
