package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.repository.ChildRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.ChildRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.motechproject.nms.tracking.utils.TrackChangeUtils.LOGGER;

@Service("childRejectionService")
public class ChildRejectionServiceImpl implements ChildRejectionService {

    @Autowired
    private ChildRejectionDataService childRejectionDataService;

    @Override
    public ChildImportRejection findByChildId(String idNo, String registrationNo) {
        return childRejectionDataService.findRejectedChild(idNo, registrationNo);
    }

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public void createOrUpdateChild(ChildImportRejection childImportRejection) {
        if (childImportRejection.getIdNo() != null || childImportRejection.getRegistrationNo() != null) {
            ChildImportRejection childImportRejection1 = childRejectionDataService.findRejectedChild(childImportRejection.getIdNo(), childImportRejection.getRegistrationNo());

            if (childImportRejection1 == null && !childImportRejection.getAccepted()) {
                childRejectionDataService.create(childImportRejection);
            } else if (childImportRejection1 == null && childImportRejection.getAccepted()) {
                LOGGER.debug(String.format("There is no mother rejection data for mctsId %s and rchId %s", childImportRejection.getIdNo(), childImportRejection.getRegistrationNo()));
            } else if (childImportRejection1 != null && !childImportRejection1.getAccepted()) {
                childImportRejection1 = setNewData1(childImportRejection, childImportRejection1);
                childRejectionDataService.update(childImportRejection1);
            } else if (childImportRejection1 != null && childImportRejection1.getAccepted()) {
                childImportRejection1 = setNewData1(childImportRejection, childImportRejection1);
                childRejectionDataService.update(childImportRejection1);
            }
        }
    }

    private static ChildImportRejection setNewData1(ChildImportRejection childImportRejection, ChildImportRejection childImportRejection1) {
        childImportRejection1.setStateId(childImportRejection.getStateId());
        childImportRejection1.setDistrictId(childImportRejection.getDistrictId());
        childImportRejection1.setDistrictName(childImportRejection.getDistrictName());
        childImportRejection1.setTalukaId(childImportRejection.getTalukaId());
        childImportRejection1.setTalukaName(childImportRejection.getTalukaName());
        childImportRejection1.setHealthBlockId(childImportRejection.getHealthBlockId());
        childImportRejection1.setHealthBlockName(childImportRejection.getHealthBlockName());
        childImportRejection1.setPhcId(childImportRejection.getPhcId());
        childImportRejection1.setPhcName(childImportRejection.getPhcName());
        childImportRejection1.setSubcentreId(childImportRejection.getSubcentreId());
        childImportRejection1.setSubcentreName(childImportRejection.getSubcentreName());
        childImportRejection1.setVillageId(childImportRejection.getVillageId());
        childImportRejection1.setVillageName(childImportRejection.getVillageName());
        childImportRejection1.setYr(childImportRejection.getYr());
        childImportRejection1.setCityMaholla(childImportRejection.getCityMaholla());
        childImportRejection1.setgPVillage(childImportRejection.getgPVillage());
        childImportRejection1.setAddress(childImportRejection.getAddress());
        childImportRejection1.setIdNo(childImportRejection.getIdNo());
        childImportRejection1.setName(childImportRejection.getName());
        childImportRejection1.setMobileNo(childImportRejection.getMobileNo());
        childImportRejection1.setMotherName(childImportRejection.getMotherName());
        childImportRejection1.setMotherId(childImportRejection.getMotherId());
        childImportRejection1.setAshaPhone(childImportRejection.getAshaPhone());
        childImportRejection1.setbCGDt(childImportRejection.getbCGDt());
        childImportRejection1.setoPV0Dt(childImportRejection.getoPV0Dt());
        childImportRejection1.setHepatitisB1Dt(childImportRejection.getHepatitisB1Dt());
        childImportRejection1.setdPT1Dt(childImportRejection.getdPT1Dt());
        childImportRejection1.setoPV1Dt(childImportRejection.getoPV1Dt());
        childImportRejection1.setHepatitisB2Dt(childImportRejection.getHepatitisB2Dt());
        childImportRejection1.setPhoneNumberWhom(childImportRejection.getPhoneNumberWhom());
        childImportRejection1.setBirthDate(childImportRejection.getBirthDate());
        childImportRejection1.setPlaceOfDelivery(childImportRejection.getPlaceOfDelivery());
        childImportRejection1.setBloodGroup(childImportRejection.getBloodGroup());
        childImportRejection1.setCaste(childImportRejection.getCaste());
        childImportRejection1.setSubcenterName1(childImportRejection1.getSubcenterName1());
        childImportRejection1.setaNMName(childImportRejection.getaNMName());
        childImportRejection1.setaNMPhone(childImportRejection.getaNMPhone());
        childImportRejection1.setAshaName(childImportRejection.getAshaName());
        childImportRejection1.setdPT2Dt(childImportRejection.getdPT2Dt());
        childImportRejection1.setoPV2Dt(childImportRejection.getoPV2Dt());
        setNewData2(childImportRejection, childImportRejection1);
        return childImportRejection1;
    }

    private static void setNewData2(ChildImportRejection childImportRejection, ChildImportRejection childImportRejection1) {
        childImportRejection1.setHepatitisB3Dt(childImportRejection.getHepatitisB3Dt());
        childImportRejection1.setdPT3Dt(childImportRejection.getdPT3Dt());
        childImportRejection1.setoPV3Dt(childImportRejection.getoPV3Dt());
        childImportRejection1.setHepatitisB4Dt(childImportRejection.getHepatitisB4Dt());
        childImportRejection1.setMeaslesDt(childImportRejection.getMeaslesDt());
        childImportRejection1.setVitADose1Dt(childImportRejection.getVitADose1Dt());
        childImportRejection1.setmRDt(childImportRejection.getmRDt());
        childImportRejection1.setdPTBoosterDt(childImportRejection.getdPTBoosterDt());
        childImportRejection1.setoPVBoosterDt(childImportRejection.getoPVBoosterDt());
        childImportRejection1.setVitADose2Dt(childImportRejection.getVitADose2Dt());
        childImportRejection1.setVitADose3Dt(childImportRejection.getVitADose3Dt());
        childImportRejection1.settT10Dt(childImportRejection.gettT10Dt());
        childImportRejection1.setjEDt(childImportRejection.getjEDt());
        childImportRejection1.setVitADose9Dt(childImportRejection.getVitADose9Dt());
        childImportRejection1.setdT5Dt(childImportRejection.getdT5Dt());
        childImportRejection1.settT16Dt(childImportRejection.gettT16Dt());
        childImportRejection1.setcLDRegDATE(childImportRejection.getcLDRegDATE());
        childImportRejection1.setAshaID(childImportRejection.getAshaID());
        childImportRejection1.setLastUpdateDate(childImportRejection.getLastUpdateDate());
        childImportRejection1.setVitADose6Dt(childImportRejection.getVitADose6Dt());
        childImportRejection1.setRemarks(childImportRejection.getRemarks());
        childImportRejection1.setaNMID(childImportRejection.getaNMID());
        childImportRejection1.setCreatedBy(childImportRejection.getCreatedBy());
        childImportRejection1.setUpdatedBy(childImportRejection.getUpdatedBy());
        childImportRejection1.setMeasles2Dt(childImportRejection.getMeasles2Dt());
        childImportRejection1.setWeightOfChild(childImportRejection.getWeightOfChild());
        childImportRejection1.setChildAadhaarNo(childImportRejection.getChildAadhaarNo());
        childImportRejection1.setChildEID(childImportRejection.getChildEID());
        childImportRejection1.setSex(childImportRejection.getSex());
        childImportRejection1.setVitADose5Dt(childImportRejection.getVitADose5Dt());
        childImportRejection1.setVitADose7Dt(childImportRejection.getVitADose7Dt());
        childImportRejection1.setVitADose8Dt(childImportRejection.getVitADose8Dt());
        childImportRejection1.setChildEIDTime(childImportRejection.getChildEIDTime());
        childImportRejection1.setFatherName(childImportRejection.getFatherName());
        childImportRejection1.setExecDate(childImportRejection.getExecDate());
        childImportRejection1.setAccepted(childImportRejection.getAccepted());
        childImportRejection1.setRejectionReason(childImportRejection.getRejectionReason());
        childImportRejection1.setBirthCertificateNumber(childImportRejection.getBirthCertificateNumber());
        childImportRejection1.setEntryType(childImportRejection.getEntryType());
        childImportRejection1.setSource(childImportRejection.getSource());
        childImportRejection1.setRegistrationNo(childImportRejection.getRegistrationNo());
        childImportRejection1.setmCTSMotherIDNo(childImportRejection.getmCTSMotherIDNo());
        childImportRejection1.setAction(childImportRejection.getAction());
    }

}
