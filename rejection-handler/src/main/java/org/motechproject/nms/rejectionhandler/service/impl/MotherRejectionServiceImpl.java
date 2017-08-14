package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.nms.rejectionhandler.repository.MotherRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.MotherRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.motechproject.nms.tracking.utils.TrackChangeUtils.LOGGER;

/**
 * Created by beehyv on 17/7/17.
 */
@Service("motherRejectionService")
public class MotherRejectionServiceImpl implements MotherRejectionService {

    @Autowired
    private MotherRejectionDataService motherRejectionDataService;

    @Override
    public MotherImportRejection findByMotherId(String idNo, String registrationNo) {
        return motherRejectionDataService.findRejectedMother(idNo, registrationNo);
    }

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public void createOrUpdateMother(MotherImportRejection motherImportRejection) {
        if (motherImportRejection.getIdNo() != null || motherImportRejection.getRegistrationNo() != null) {
            MotherImportRejection motherImportRejection1 = motherRejectionDataService.findRejectedMother(motherImportRejection.getIdNo(), motherImportRejection.getRegistrationNo());

            if (motherImportRejection1 == null && !motherImportRejection.getAccepted()) {
                motherRejectionDataService.create(motherImportRejection);
            } else if (motherImportRejection1 == null && motherImportRejection.getAccepted()) {
                LOGGER.debug(String.format("There is no mother rejection data for mctsId %s and rchId %s", motherImportRejection.getIdNo(), motherImportRejection.getRegistrationNo()));
            } else if (motherImportRejection1 != null && !motherImportRejection1.getAccepted()) {
                motherImportRejection1 = setNewData1(motherImportRejection, motherImportRejection1);
                motherRejectionDataService.update(motherImportRejection1);
            } else if (motherImportRejection1 != null && motherImportRejection1.getAccepted()) {
                motherImportRejection1 = setNewData1(motherImportRejection, motherImportRejection1);
                motherRejectionDataService.update(motherImportRejection1);
            }
        }
    }

    private static MotherImportRejection setNewData1(MotherImportRejection motherImportRejection, MotherImportRejection motherImportRejection1) {
        motherImportRejection1.setStateId(motherImportRejection.getStateId());
        motherImportRejection1.setDistrictId(motherImportRejection.getDistrictId());
        motherImportRejection1.setDistrictName(motherImportRejection.getDistrictName());
        motherImportRejection1.setTalukaId(motherImportRejection.getTalukaId());
        motherImportRejection1.setTalukaName(motherImportRejection.getTalukaName());
        motherImportRejection1.setHealthBlockId(motherImportRejection.getHealthBlockId());
        motherImportRejection1.setHealthBlockName(motherImportRejection.getHealthBlockName());
        motherImportRejection1.setPhcId(motherImportRejection.getPhcId());
        motherImportRejection1.setPhcName(motherImportRejection.getPhcName());
        motherImportRejection1.setSubcentreId(motherImportRejection.getSubcentreId());
        motherImportRejection1.setSubcentreName(motherImportRejection.getSubcentreName());
        motherImportRejection1.setVillageId(motherImportRejection.getVillageId());
        motherImportRejection1.setVillageName(motherImportRejection.getVillageName());
        motherImportRejection1.setYr(motherImportRejection.getYr());
        motherImportRejection1.setgPVillage(motherImportRejection.getgPVillage());
        motherImportRejection1.setAddress(motherImportRejection.getAddress());
        motherImportRejection1.setIdNo(motherImportRejection.getIdNo());
        motherImportRejection1.setName(motherImportRejection.getName());
        motherImportRejection1.setHusbandName(motherImportRejection.getHusbandName());
        motherImportRejection1.setPhoneNumberWhom(motherImportRejection.getPhoneNumberWhom());
        motherImportRejection1.setBirthDate(motherImportRejection.getBirthDate());
        motherImportRejection1.setjSYBeneficiary(motherImportRejection.getjSYBeneficiary());
        motherImportRejection1.setCaste(motherImportRejection.getCaste());
        motherImportRejection1.setSubcenterName1(motherImportRejection.getSubcenterName1());
        motherImportRejection1.setaNMName(motherImportRejection.getaNMName());
        setNewData2(motherImportRejection, motherImportRejection1);
        setNewData3(motherImportRejection, motherImportRejection1);
        return motherImportRejection1;
    }

    private static void setNewData2(MotherImportRejection motherImportRejection, MotherImportRejection motherImportRejection1) {
        motherImportRejection1.setaNMPhone(motherImportRejection.getaNMPhone());
        motherImportRejection1.setAshaName(motherImportRejection.getAshaName());
        motherImportRejection1.setAshaPhone(motherImportRejection.getAshaPhone());
        motherImportRejection1.setDeliveryLnkFacility(motherImportRejection.getDeliveryLnkFacility());
        motherImportRejection1.setFacilityName(motherImportRejection.getFacilityName());
        motherImportRejection1.setLmpDate(motherImportRejection.getLmpDate());
        motherImportRejection1.setaNC1Date(motherImportRejection.getaNC1Date());
        motherImportRejection1.setaNC2Date(motherImportRejection.getaNC2Date());
        motherImportRejection1.setaNC3Date(motherImportRejection.getaNC3Date());
        motherImportRejection1.setaNC4Date(motherImportRejection.getaNC4Date());
        motherImportRejection1.settT1Date(motherImportRejection.gettT1Date());
        motherImportRejection1.settT2Date(motherImportRejection.gettT2Date());
        motherImportRejection1.settTBoosterDate(motherImportRejection.gettTBoosterDate());
        motherImportRejection1.setiFA100GivenDate(motherImportRejection.getiFA100GivenDate());
        motherImportRejection1.setAnemia(motherImportRejection.getAnemia());
        motherImportRejection1.setaNCComplication(motherImportRejection.getaNCComplication());
        motherImportRejection1.setrTISTI(motherImportRejection.getrTISTI());
        motherImportRejection1.setDlyDate(motherImportRejection.getDlyDate());
        motherImportRejection1.setDlyPlaceHomeType(motherImportRejection.getDlyPlaceHomeType());
        motherImportRejection1.setDlyPlacePublic(motherImportRejection.getDlyPlacePublic());
        motherImportRejection1.setDlyPlacePrivate(motherImportRejection.getDlyPlacePrivate());
        motherImportRejection1.setDlyType(motherImportRejection.getDlyType());
        motherImportRejection1.setDlyComplication(motherImportRejection.getDlyComplication());
        motherImportRejection1.setDischargeDate(motherImportRejection.getDischargeDate());
        motherImportRejection1.setjSYPaidDate(motherImportRejection.getjSYPaidDate());
        motherImportRejection1.setAbortion(motherImportRejection.getAbortion());
    }

    private static void setNewData3(MotherImportRejection motherImportRejection, MotherImportRejection motherImportRejection1) {
        motherImportRejection1.setpNCHomeVisit(motherImportRejection.getpNCHomeVisit());
        motherImportRejection1.setpNCComplication(motherImportRejection.getpNCComplication());
        motherImportRejection1.setpPCMethod(motherImportRejection.getpPCMethod());
        motherImportRejection1.setpNCCheckup(motherImportRejection.getpNCCheckup());
        motherImportRejection1.setOutcomeNos(motherImportRejection.getOutcomeNos());
        motherImportRejection1.setChild1Name(motherImportRejection.getChild1Name());
        motherImportRejection1.setChild1Sex(motherImportRejection.getChild1Sex());
        motherImportRejection1.setChild1Wt(motherImportRejection.getChild1Wt());
        motherImportRejection1.setChild1Brestfeeding(motherImportRejection.getChild1Brestfeeding());
        motherImportRejection1.setChild2Name(motherImportRejection.getChild2Name());
        motherImportRejection1.setChild2Sex(motherImportRejection.getChild2Sex());
        motherImportRejection1.setChild2Wt(motherImportRejection.getChild2Wt());
        motherImportRejection1.setChild2Brestfeeding(motherImportRejection.getChild2Brestfeeding());
        motherImportRejection1.setChild3Name(motherImportRejection.getChild3Name());
        motherImportRejection1.setChild3Sex(motherImportRejection.getChild3Sex());
        motherImportRejection1.setChild3Wt(motherImportRejection.getChild3Wt());
        motherImportRejection1.setChild3Brestfeeding(motherImportRejection.getChild3Brestfeeding());
        motherImportRejection1.setChild4Name(motherImportRejection.getChild4Name());
        motherImportRejection1.setChild4Sex(motherImportRejection.getChild4Sex());
        motherImportRejection1.setChild4Wt(motherImportRejection.getChild4Wt());
        motherImportRejection1.setChild4Brestfeeding(motherImportRejection.getChild4Brestfeeding());
        motherImportRejection1.setAge(motherImportRejection.getAge());
        motherImportRejection1.setmTHRREGDATE(motherImportRejection.getmTHRREGDATE());
        motherImportRejection1.setLastUpdateDate(motherImportRejection.getLastUpdateDate());
        motherImportRejection1.setRemarks(motherImportRejection.getRemarks());
        motherImportRejection1.setaNMID(motherImportRejection.getaNMID());
        motherImportRejection1.setaSHAID(motherImportRejection.getaSHAID());
        motherImportRejection1.setCallAns(motherImportRejection.getCallAns());
        motherImportRejection1.setNoCallReason(motherImportRejection.getNoCallReason());
        motherImportRejection1.setNoPhoneReason(motherImportRejection.getNoPhoneReason());
        motherImportRejection1.setCreatedBy(motherImportRejection.getCreatedBy());
        motherImportRejection1.setUpdatedBy(motherImportRejection.getUpdatedBy());
        motherImportRejection1.setAadharNo(motherImportRejection.getAadharNo());
        motherImportRejection1.setbPLAPL(motherImportRejection.getbPLAPL());
        motherImportRejection1.seteID(motherImportRejection.geteID());
        motherImportRejection1.seteIDTime(motherImportRejection.geteIDTime());
        motherImportRejection1.setEntryType(motherImportRejection.getEntryType());
        motherImportRejection1.setRegistrationNo(motherImportRejection.getRegistrationNo());
        motherImportRejection1.setCaseNo(motherImportRejection.getCaseNo());
        motherImportRejection1.setMobileNo(motherImportRejection.getMobileNo());
        motherImportRejection1.setAbortionType(motherImportRejection.getAbortionType());
        motherImportRejection1.setDeliveryOutcomes(motherImportRejection.getDeliveryOutcomes());
        motherImportRejection1.setExecDate(motherImportRejection.getExecDate());
        motherImportRejection1.setAccepted(motherImportRejection.getAccepted());
        motherImportRejection1.setRejectionReason(motherImportRejection.getRejectionReason());
        motherImportRejection1.setSource(motherImportRejection.getSource());
        motherImportRejection1.setAction(motherImportRejection.getAction());
    }

}
