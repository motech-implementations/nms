package org.motechproject.nms.kilkari.utils;

import org.motechproject.nms.kilkari.contract.AnmAshaRecord;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;
import org.motechproject.nms.kilkari.contract.RchAnmAshaRecord;
import org.motechproject.nms.kilkari.contract.RchChildRecord;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;


import java.util.Map;

public final class RejectedObjectConverter {

    private RejectedObjectConverter() {
    }


    public static FlwImportRejection flwRejectionRch(RchAnmAshaRecord record, Boolean accepted, String rejectionReason, String action) {
        FlwImportRejection flwImportRejection = new FlwImportRejection();
        flwImportRejection.setStateId(record.getStateId());
        flwImportRejection.setDistrictId(record.getDistrictId());
        flwImportRejection.setDistrictName(record.getDistrictName());
        flwImportRejection.setMsisdn(record.getMobileNo());
        flwImportRejection.setGfName(record.getGfName());
        flwImportRejection.setType(record.getGfType());
        flwImportRejection.setGfStatus(record.getGfStatus());
        flwImportRejection.setExecDate(record.getExecDate());
        flwImportRejection.setSource("RCH-Import");
        flwImportRejection.setAccepted(accepted);
        flwImportRejection.setTalukaId(record.getTalukaId());
        flwImportRejection.setHealthBlockId(record.getHealthBlockId());
        flwImportRejection.setHealthBlockName(record.getHealthBlockName());
        flwImportRejection.setPhcId(record.getPhcId());
        flwImportRejection.setPhcName(record.getPhcName());
        flwImportRejection.setSubcentreId(record.getSubCentreId());
        flwImportRejection.setSubcentreName(record.getSubCentreName());
        flwImportRejection.setVillageId(record.getVillageId());
        flwImportRejection.setVillageName(record.getVillageName());
        flwImportRejection.setFlwId(record.getGfId());
        flwImportRejection.setRejectionReason(rejectionReason);
        flwImportRejection.setAction(action);

        return flwImportRejection;
    }

    public static FlwImportRejection flwRejectionMcts(AnmAshaRecord record, Boolean accepted, String rejectionReason, String action) { //NOPMD NcssMethodCount
        FlwImportRejection flwImportRejection = new FlwImportRejection();
        flwImportRejection.setStateId(record.getStateId());
        flwImportRejection.setDistrictId(record.getDistrictId());
        flwImportRejection.setDistrictName(record.getDistrictName());
        flwImportRejection.setTalukaId(record.getTalukaId());
        flwImportRejection.setHealthBlockId(record.getHealthBlockId());
        flwImportRejection.setHealthBlockName(record.getHealthBlockName());
        flwImportRejection.setPhcId(record.getPhcId());
        flwImportRejection.setPhcName(record.getPhcName());
        flwImportRejection.setSubcentreId(record.getSubCentreId());
        flwImportRejection.setSubcentreName(record.getSubCentreName());
        flwImportRejection.setVillageId(record.getVillageId());
        flwImportRejection.setVillageName(record.getVillageName());
        flwImportRejection.setFlwId(record.getId());
        flwImportRejection.setMsisdn(record.getContactNo());
        flwImportRejection.setGfName(record.getName());
        flwImportRejection.setType(record.getType());
        flwImportRejection.setGfStatus(record.getGfStatus());
        flwImportRejection.setRegDate(record.getRegDate());
        flwImportRejection.setSex(record.getSex());
        if (record.getSmsReply() != null && record.getSmsReply().length() > 255) {
            flwImportRejection.setSmsReply(record.getSmsReply().substring(0, 255));
        } else {
            flwImportRejection.setSmsReply(record.getSmsReply());
        }
        flwImportRejection.setAadharNo(record.getAadharNo());
        flwImportRejection.setCreatedOn(record.getCreatedOn());
        flwImportRejection.setUpdatedOn(record.getUpdatedOn());
        flwImportRejection.setBankId(record.getBankId());
        flwImportRejection.setBranchName(record.getBranchName());
        flwImportRejection.setIfscIdCode(record.getIfscIdCode());
        flwImportRejection.setBankName(record.getBankName());
        flwImportRejection.setAccountNumber(record.getAccNo());
        flwImportRejection.setAadharLinked(record.getIsAadharLinked());
        flwImportRejection.setVerifyDate(record.getVerifyDate());
        flwImportRejection.setVerifierName(record.getVerifierName());
        flwImportRejection.setVerifierId(record.getVerifierId());
        flwImportRejection.setCallAns(record.getCallAns());
        flwImportRejection.setPhoneNoCorrect(record.getIsPhoneNoCorrect());
        flwImportRejection.setNoCallReason(record.getNoCallReason());
        flwImportRejection.setNoPhoneReason(record.getNoPhoneReason());
        flwImportRejection.setVerifierRemarks(record.getVerifierRemarks());
        flwImportRejection.setGfAddress(record.getGfAddress());
        flwImportRejection.setHusbandName(record.getHusbandName());
        flwImportRejection.setMddsStateId(record.getMddsStateId());
        flwImportRejection.setMddsDistrictId(record.getMddsDistrictId());
        flwImportRejection.setMddsTalukaId(record.getMddsTalukaId());
        flwImportRejection.setMddsVillageId(record.getMddsVillageId());
        flwImportRejection.setSource("MCTS-Import");
        flwImportRejection.setAccepted(accepted);
        flwImportRejection.setRejectionReason(rejectionReason);
        flwImportRejection.setAction(action);

        return flwImportRejection;
    }

    public static ChildImportRejection childRejectionRch(RchChildRecord record, Boolean accepted, String rejectionReason, String action) {
        ChildImportRejection childImportRejection = new ChildImportRejection();
        childImportRejection.setSubcentreId(record.getSubCentreId());
        childImportRejection.setSubcentreName(record.getSubCentreName());
        childImportRejection.setVillageId(record.getVillageId());
        childImportRejection.setVillageName(record.getVillageName());
        childImportRejection.setName(record.getName());
        childImportRejection.setMobileNo(record.getMobileNo());
        childImportRejection.setStateId(record.getStateId());
        childImportRejection.setDistrictId(record.getDistrictId());
        childImportRejection.setDistrictName(record.getDistrictName());
        childImportRejection.setTalukaId(record.getTalukaId());
        childImportRejection.setTalukaName(record.getTalukaName());
        childImportRejection.setHealthBlockId(record.getHealthBlockId());
        childImportRejection.setHealthBlockName(record.getHealthBlockName());
        childImportRejection.setPhcId(record.getPhcId());
        childImportRejection.setPhcName(record.getPhcName());
        childImportRejection.setBirthDate(record.getBirthdate());
        childImportRejection.setRegistrationDate(record.getRegistrationDate());
        childImportRejection.setRegistrationNo(record.getRegistrationNo());
        childImportRejection.setEntryType(record.getEntryType());
        childImportRejection.setIdNo(record.getMctsId());
        childImportRejection.setmCTSMotherIDNo(record.getMctsMotherIdNo());
        childImportRejection.setMotherId(record.getMotherRegistrationNo());
        childImportRejection.setExecDate(record.getExecDate());
        childImportRejection.setSource("RCH-Import");
        childImportRejection.setAccepted(accepted);
        childImportRejection.setRejectionReason(rejectionReason);
        childImportRejection.setAction(action);

        return childImportRejection;
    }

    public static ChildImportRejection childRejectionMcts(ChildRecord record, Boolean accepted, String rejectionReason, String action) { //NOPMD NcssMethodCount
        ChildImportRejection childImportRejection = new ChildImportRejection();
        childImportRejection.setStateId(record.getStateID());
        childImportRejection.setDistrictId(record.getDistrictId());
        childImportRejection.setDistrictName(record.getDistrictName());
        childImportRejection.setTalukaId(record.getTalukaId());
        childImportRejection.setTalukaName(record.getTalukaName());
        childImportRejection.setHealthBlockId(record.getHealthBlockId());
        childImportRejection.setHealthBlockName(record.getHealthBlockName());
        childImportRejection.setPhcId(record.getPhcId());
        childImportRejection.setPhcName(record.getPhcName());
        childImportRejection.setSubcentreId(record.getSubCentreId());
        childImportRejection.setSubcentreName(record.getSubCentreName());
        childImportRejection.setVillageId(record.getVillageId());
        childImportRejection.setVillageName(record.getVillageName());
        childImportRejection.setYr(record.getYr());
        childImportRejection.setCityMaholla(record.getCityMaholla());
        childImportRejection.setgPVillage(record.getGpVillage());
        childImportRejection.setAddress(record.getAddress());
        childImportRejection.setIdNo(record.getIdNo());
        childImportRejection.setName(record.getName());
        childImportRejection.setMotherName(record.getMotherName());
        childImportRejection.setmCTSMotherIDNo(record.getMotherId());
        childImportRejection.setPhoneNumberWhom(record.getPhoneNoOfWhom());
        childImportRejection.setMobileNo(record.getWhomPhoneNo());
        childImportRejection.setBirthDate(record.getBirthdate());
        childImportRejection.setRegistrationDate(record.getRegistrationDate());
        childImportRejection.setPlaceOfDelivery(record.getPlaceOfDelivery());
        childImportRejection.setBloodGroup(record.getBloodGroup());
        childImportRejection.setCaste(record.getCaste());
        childImportRejection.setSubcenterName1(record.getSubCentreName1());
        childImportRejection.setaNMName(record.getAnmName());
        childImportRejection.setaNMPhone(record.getAnmPhone());
        childImportRejection.setAshaName(record.getAshaName());
        childImportRejection.setAshaPhone(record.getAshaPhone());
        childImportRejection.setbCGDt(record.getBcgDt());
        childImportRejection.setoPV0Dt(record.getOpv0Dt());
        childImportRejection.setHepatitisB1Dt(record.getHepatitisB1Dt());
        childImportRejection.setdPT1Dt(record.getDpt1Dt());
        childImportRejection.setoPV1Dt(record.getOpv1Dt());
        childImportRejection.setHepatitisB2Dt(record.getHepatitisB2Dt());
        childImportRejection.setdPT2Dt(record.getdPT2Dt());
        childImportRejection.setoPV2Dt(record.getOpv2Dt());
        childImportRejection.setHepatitisB3Dt(record.getHepatitisB3Dt());
        childImportRejection.setdPT3Dt(record.getDpt3Dt());
        childImportRejection.setoPV3Dt(record.getOpv3Dt());
        childImportRejection.setHepatitisB4Dt(record.getHepatitisB4Dt());
        childImportRejection.setMeaslesDt(record.getMeaslesDt());
        childImportRejection.setVitADose1Dt(record.getVitADose1Dt());
        childImportRejection.setmRDt(record.getMrDt());
        childImportRejection.setdPTBoosterDt(record.getDptBoosterDt());
        childImportRejection.setoPVBoosterDt(record.getOpvBoosterDt());
        childImportRejection.setVitADose2Dt(record.getVitADose2Dt());
        childImportRejection.setVitADose3Dt(record.getVitADose3Dt());
        childImportRejection.setjEDt(record.getJeDt());
        childImportRejection.setVitADose9Dt(record.getVitADose9Dt());
        childImportRejection.setdT5Dt(record.getDt5Dt());
        childImportRejection.settT10Dt(record.getTt10Dt());
        childImportRejection.settT16Dt(record.getTt16Dt());
        childImportRejection.setcLDRegDATE(record.getCldRegDate());
        childImportRejection.setSex(record.getSex());
        childImportRejection.setVitADose5Dt(record.getVitADose5Dt());
        childImportRejection.setVitADose6Dt(record.getVitADose6Dt());
        childImportRejection.setVitADose7Dt(record.getVitADose7Dt());
        childImportRejection.setVitADose8Dt(record.getVitADose8Dt());
        childImportRejection.setLastUpdateDate(record.getLastUpdateDate());
        childImportRejection.setRemarks(record.getRemarks());
        childImportRejection.setaNMID(record.getAnmID());
        childImportRejection.setAshaID(record.getAshaID());
        childImportRejection.setCreatedBy(record.getCreatedBy());
        childImportRejection.setUpdatedBy(record.getUpdatedBy());
        childImportRejection.setMeasles2Dt(record.getMeasles2Dt());
        childImportRejection.setWeightOfChild(record.getWeightofChild());
        childImportRejection.setChildAadhaarNo(record.getChildAadhaarNo());
        childImportRejection.setChildEID(record.getChildEID());
        childImportRejection.setChildEIDTime(record.getChildEIDTime());
        childImportRejection.setFatherName(record.getFatherName());
        childImportRejection.setBirthCertificateNumber(record.getBirthCertificateNumber());
        childImportRejection.setEntryType(record.getEntryType());
        childImportRejection.setSource("MCTS-Import");
        childImportRejection.setAccepted(accepted);
        childImportRejection.setRejectionReason(rejectionReason);
        childImportRejection.setAction(action);


        return childImportRejection;
    }

    public static MotherImportRejection motherRejectionRch(RchMotherRecord record, Boolean accepted, String rejectionReason, String action) {
        MotherImportRejection motherImportRejection = new MotherImportRejection();
        motherImportRejection.setStateId(record.getStateId());
        motherImportRejection.setDistrictId(record.getDistrictId());
        motherImportRejection.setDistrictName(record.getDistrictName());
        motherImportRejection.setTalukaId(record.getTalukaId());
        motherImportRejection.setTalukaName(record.getTalukaName());
        motherImportRejection.setHealthBlockId(record.getHealthBlockId());
        motherImportRejection.setHealthBlockName(record.getHealthBlockName());
        motherImportRejection.setPhcId(record.getPhcId());
        motherImportRejection.setPhcName(record.getPhcName());
        motherImportRejection.setSubcentreId(record.getSubCentreId());
        motherImportRejection.setSubcentreName(record.getSubCentreName());
        motherImportRejection.setVillageId(record.getVillageId());
        motherImportRejection.setVillageName(record.getVillageName());
        motherImportRejection.setIdNo(record.getMctsIdNo());
        motherImportRejection.setRegistrationNo(record.getRegistrationNo());
        motherImportRejection.setCaseNo(record.getCaseNo());
        motherImportRejection.setName(record.getName());
        motherImportRejection.setMobileNo(record.getMobileNo());
        motherImportRejection.setRegistrationDate(record.getRegistrationDate());
        motherImportRejection.setLmpDate(record.getLmpDate());
        motherImportRejection.setBirthDate(record.getBirthDate());
        motherImportRejection.setAbortionType(record.getAbortionType());
        motherImportRejection.setDeliveryOutcomes(record.getDeliveryOutcomes());
        motherImportRejection.setEntryType(record.getEntryType());
        motherImportRejection.setExecDate(record.getExecDate());
        motherImportRejection.setSource("RCH-Import");
        motherImportRejection.setAccepted(accepted);
        motherImportRejection.setRejectionReason(rejectionReason);
        motherImportRejection.setAction(action);


        return motherImportRejection;
    }

    public static MotherImportRejection motherRejectionMcts(MotherRecord record, Boolean accepted, String rejectionReason, String action) { //NOPMD NcssMethodCount
        MotherImportRejection motherImportRejection = new MotherImportRejection();
        motherImportRejection.setStateId(record.getStateId());
        motherImportRejection.setDistrictId(record.getDistrictId());
        motherImportRejection.setDistrictName(record.getDistrictName());
        motherImportRejection.setTalukaId(record.getTalukaId());
        motherImportRejection.setTalukaName(record.getTalukaName());
        motherImportRejection.setHealthBlockId(record.getHealthBlockId());
        motherImportRejection.setHealthBlockName(record.getHealthBlockName());
        motherImportRejection.setPhcId(record.getPhcid());
        motherImportRejection.setPhcName(record.getPhcName());
        motherImportRejection.setSubcentreId(record.getSubCentreid());
        motherImportRejection.setSubcentreName(record.getSubCentreName());
        motherImportRejection.setVillageId(record.getVillageId());
        motherImportRejection.setVillageName(record.getVillageName());
        motherImportRejection.setYr(record.getYr());
        motherImportRejection.setgPVillage(record.getGpVillage());
        motherImportRejection.setAddress(record.getAddress());
        motherImportRejection.setIdNo(record.getIdNo());
        motherImportRejection.setName(record.getName());
        motherImportRejection.setHusbandName(record.getHusbandName());
        motherImportRejection.setPhoneNumberWhom(record.getPhoneNoOfWhom());
        motherImportRejection.setMobileNo(record.getWhomPhoneNo());
        motherImportRejection.setBirthDate(record.getBirthdate());
        motherImportRejection.setjSYBeneficiary(record.getJsyBeneficiary());
        motherImportRejection.setCaste(record.getCaste());
        motherImportRejection.setSubcenterName1(record.getSubCentreName1());
        motherImportRejection.setaNMName(record.getAnmName());
        motherImportRejection.setaNMPhone(record.getAnmPhone());
        motherImportRejection.setAshaName(record.getAshaName());
        motherImportRejection.setAshaPhone(record.getAshaPhone());
        motherImportRejection.setDeliveryLnkFacility(record.getDeliveryLnkFacility());
        motherImportRejection.setFacilityName(record.getFacilityName());
        motherImportRejection.setLmpDate(record.getLmpDate());
        motherImportRejection.setaNC1Date(record.getAnc1Date());
        motherImportRejection.setaNC2Date(record.getAnc2Date());
        motherImportRejection.setaNC3Date(record.getAnc3Date());
        motherImportRejection.setaNC4Date(record.getAnc4Date());
        motherImportRejection.settT1Date(record.getTt1Date());
        motherImportRejection.settT2Date(record.getTt2Date());
        motherImportRejection.settTBoosterDate(record.getTtBoosterDate());
        motherImportRejection.setiFA100GivenDate(record.getIfA100GivenDate());
        motherImportRejection.setAnemia(record.getAnemia());
        motherImportRejection.setaNCComplication(record.getAncComplication());
        motherImportRejection.setrTISTI(record.getRtiSTI());
        motherImportRejection.setDlyDate(record.getDlyDate());
        motherImportRejection.setDlyPlaceHomeType(record.getDlyPlaceHomeType());
        motherImportRejection.setDlyPlacePublic(record.getDlyPlacePublic());
        motherImportRejection.setDlyPlacePrivate(record.getDlyPlacePrivate());
        motherImportRejection.setDlyType(record.getDlyType());
        motherImportRejection.setDlyComplication(record.getDlyComplication());
        motherImportRejection.setDischargeDate(record.getDischargeDate());
        motherImportRejection.setjSYPaidDate(record.getJsyPaidDate());
        motherImportRejection.setAbortion(record.getAbortion());
        motherImportRejection.setpNCHomeVisit(record.getPncHomeVisit());
        motherImportRejection.setpNCComplication(record.getPncComplication());
        motherImportRejection.setpPCMethod(record.getPpcMethod());
        motherImportRejection.setpNCCheckup(record.getPncCheckup());
        motherImportRejection.setOutcomeNos(record.getOutcomeNos());
        motherImportRejection.setChild1Name(record.getChild1Name());
        motherImportRejection.setChild1Sex(record.getChild1Sex());
        motherImportRejection.setChild1Wt(record.getChild1Wt());
        motherImportRejection.setChild1Brestfeeding(record.getChild1Brestfeeding());
        motherImportRejection.setChild2Name(record.getChild2Name());
        motherImportRejection.setChild2Sex(record.getChild2Sex());
        motherImportRejection.setChild2Wt(record.getChild2Wt());
        motherImportRejection.setChild2Brestfeeding(record.getChild2Brestfeeding());
        motherImportRejection.setChild3Name(record.getChild3Name());
        motherImportRejection.setChild3Sex(record.getChild3Sex());
        motherImportRejection.setChild3Wt(record.getChild3Wt());
        motherImportRejection.setChild3Brestfeeding(record.getChild3Brestfeeding());
        motherImportRejection.setChild4Name(record.getChild4Name());
        motherImportRejection.setChild4Sex(record.getChild4Sex());
        motherImportRejection.setChild4Wt(record.getChild4Wt());
        motherImportRejection.setChild4Brestfeeding(record.getChild4Brestfeeding());
        motherImportRejection.setAge(record.getAge());
        motherImportRejection.setmTHRREGDATE(record.getMthrRegDate());
        motherImportRejection.setLastUpdateDate(record.getLastUpdateDate());
        motherImportRejection.setRemarks(record.getRemarks());
        motherImportRejection.setaNMID(record.getAnmID());
        motherImportRejection.setaSHAID(record.getAshaID());
        motherImportRejection.setCallAns(record.getCallAns());
        motherImportRejection.setNoCallReason(record.getNoCallReason());
        motherImportRejection.setNoPhoneReason(record.getNoPhoneReason());
        motherImportRejection.setCreatedBy(record.getCreatedBy());
        motherImportRejection.setUpdatedBy(record.getUpdatedBy());
        motherImportRejection.setAadharNo(record.getAadharNo());
        motherImportRejection.setbPLAPL(record.getBplAPL());
        motherImportRejection.seteID(record.geteId());
        motherImportRejection.seteIDTime(record.geteIdTime());
        motherImportRejection.setEntryType(record.getEntryType());
        motherImportRejection.setSource("MCTS-Import");
        motherImportRejection.setAccepted(accepted);
        motherImportRejection.setRejectionReason(rejectionReason);
        motherImportRejection.setAction(action);
        motherImportRejection.setRegistrationDate(record.getMotherRegistrationDate());


        return motherImportRejection;
    }



    public static MotherRecord convertMapToMother(Map<String, Object> record) { //NO CHECKSTYLE CyclomaticComplexity
        MotherRecord motherRecord = new MotherRecord();
        motherRecord.setStateId(record.get(KilkariConstants.STATE_ID) == null ? null : (Long) record.get(KilkariConstants.STATE_ID));
        motherRecord.setDistrictId(record.get(KilkariConstants.DISTRICT_ID) == null ? null : (Long) record.get(KilkariConstants.DISTRICT_ID));
        motherRecord.setDistrictName(record.get(KilkariConstants.DISTRICT_NAME) == null ? null : record.get(KilkariConstants.DISTRICT_NAME).toString());
        motherRecord.setTalukaId(record.get(KilkariConstants.TALUKA_ID) == null ? null : record.get(KilkariConstants.TALUKA_ID).toString());
        motherRecord.setTalukaName(record.get(KilkariConstants.TALUKA_NAME) == null ? null : record.get(KilkariConstants.TALUKA_NAME).toString());
        motherRecord.setHealthBlockId(record.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? null : (Long) record.get(KilkariConstants.HEALTH_BLOCK_ID));
        motherRecord.setHealthBlockName(record.get(KilkariConstants.HEALTH_BLOCK_NAME) == null ? null : record.get(KilkariConstants.HEALTH_BLOCK_NAME).toString());
        motherRecord.setPhcid(record.get(KilkariConstants.PHC_ID) == null ? null : (Long) record.get(KilkariConstants.PHC_ID));
        motherRecord.setPhcName(record.get(KilkariConstants.PHC_NAME) == null ? null : record.get(KilkariConstants.PHC_NAME).toString());
        motherRecord.setSubCentreid(record.get(KilkariConstants.SUB_CENTRE_ID) == null ? null : (Long) record.get(KilkariConstants.SUB_CENTRE_ID));
        motherRecord.setSubCentreName(record.get(KilkariConstants.SUB_CENTRE_NAME) == null ? null : record.get(KilkariConstants.SUB_CENTRE_NAME).toString());
        motherRecord.setVillageId(record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? null : (Long) record.get(KilkariConstants.CENSUS_VILLAGE_ID));
        motherRecord.setVillageName(record.get(KilkariConstants.VILLAGE_NAME) == null ? null : record.get(KilkariConstants.VILLAGE_NAME).toString());
        motherRecord.setLastUpdateDate(record.get(KilkariConstants.LAST_UPDATE_DATE) == null ? null : record.get(KilkariConstants.LAST_UPDATE_DATE).toString());

        motherRecord.setIdNo(record.get(KilkariConstants.BENEFICIARY_ID) == null ? null : record.get(KilkariConstants.BENEFICIARY_ID).toString());
        motherRecord.setName(record.get(KilkariConstants.BENEFICIARY_NAME) == null ? null : record.get(KilkariConstants.BENEFICIARY_NAME).toString());
        motherRecord.setWhomPhoneNo(record.get(KilkariConstants.MSISDN) == null ? null : record.get(KilkariConstants.MSISDN).toString());
        motherRecord.setLmpDate(record.get(KilkariConstants.LMP) == null ? null : record.get(KilkariConstants.LMP).toString());
        motherRecord.setMotherRegistrationDate(record.get(KilkariConstants.MOTHER_REGISTRATION_DATE) == null ? null : record.get(KilkariConstants.MOTHER_REGISTRATION_DATE).toString());
        motherRecord.setBirthdate(record.get(KilkariConstants.MOTHER_DOB) == null ? null : record.get(KilkariConstants.MOTHER_DOB).toString());
        motherRecord.setAbortion(record.get(KilkariConstants.ABORTION) == null ? null : record.get(KilkariConstants.ABORTION).toString());
        motherRecord.setOutcomeNos(record.get(KilkariConstants.STILLBIRTH) == null ? null : ((Boolean) record.get(KilkariConstants.STILLBIRTH) ? 1 : 0));
        motherRecord.setEntryType(record.get(KilkariConstants.DEATH) == null ? null : ((Boolean) record.get(KilkariConstants.DEATH) ? 1 : 0));

        motherRecord.setYr(record.get("Yr") == null || record.get("Yr").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("Yr").toString()));
        motherRecord.setGpVillage(record.get("GP_Village") == null ? null : (String) record.get("GP_Village"));
        motherRecord.setAddress(record.get("Address") == null ? null : (String) record.get("Address"));
        motherRecord.setHusbandName(record.get("Husband_Name") == null ? null : (String) record.get("Husband_Name"));
        motherRecord.setPhoneNoOfWhom(record.get(KilkariConstants.PH_OF_WHOM) == null ? null : (String) record.get(KilkariConstants.PH_OF_WHOM));
        motherRecord.setJsyBeneficiary(record.get("JSY_Beneficiary") == null ? null : (String) record.get("JSY_Beneficiary"));
        motherRecord.setCaste(record.get(KilkariConstants.CASTE) == null ? null : (String) record.get(KilkariConstants.CASTE));
        motherRecord.setSubCentreName1(record.get(KilkariConstants.SUB_CENTRE_NAME1) == null ? null : (String) record.get(KilkariConstants.SUB_CENTRE_NAME1));
        motherRecord.setAnmName(record.get(KilkariConstants.ANM_NAME) == null ? null : (String) record.get(KilkariConstants.ANM_NAME));
        motherRecord.setAnmPhone(record.get("ANM_Phone") == null ? null : (String) record.get("ANM_Phone"));
        motherRecord.setAshaName(record.get(KilkariConstants.ASHA_NAME) == null ? null : (String) record.get(KilkariConstants.ASHA_NAME));
        motherRecord.setAshaPhone(record.get(KilkariConstants.ASHA_PHONE) == null ? null : (String) record.get(KilkariConstants.ASHA_PHONE));
        motherRecord.setDeliveryLnkFacility(record.get("Delivery_Lnk_Facility") == null ? null : (String) record.get("Delivery_Lnk_Facility"));
        motherRecord.setFacilityName(record.get("Facility_Name") == null ? null : (String) record.get("Facility_Name"));
        motherRecord.setAnc1Date(record.get("ANC1_Date") == null ? null : (String) record.get("ANC1_Date"));
        motherRecord.setAnc2Date(record.get("ANC2_Date") == null ? null : (String) record.get("ANC2_Date"));
        motherRecord.setAnc3Date(record.get("ANC3_Date") == null ? null : (String) record.get("ANC3_Date"));
        motherRecord.setAnc4Date(record.get("ANC4_Date") == null ? null : (String) record.get("ANC4_Date"));
        motherRecord.setTt1Date(record.get("TT1_Date") == null ? null : (String) record.get("TT1_Date"));
        motherRecord.setTt2Date(record.get("TT2_Date") == null ? null : (String) record.get("TT2_Date"));
        motherRecord.setTtBoosterDate(record.get("TTBooster_Date") == null ? null : (String) record.get("TTBooster_Date"));
        motherRecord.setIfA100GivenDate(record.get("IFA100_Given_Date") == null ? null : (String) record.get("IFA100_Given_Date"));

        motherRecord = convertToMother(motherRecord, record);

        return motherRecord;
    }

    private static MotherRecord convertToMother(MotherRecord motherRecord, Map<String, Object> record) { // NO CHECKSTYLE Cyclomatic Complexity
        motherRecord.setAnemia(record.get("Anemia") == null ? null : (String) record.get("Anemia"));
        motherRecord.setAncComplication(record.get("ANC_Complication") == null ? null : (String) record.get("ANC_Complication"));
        motherRecord.setRtiSTI(record.get("RTI_STI") == null ? null : (String) record.get("RTI_STI"));
        motherRecord.setDlyDate(record.get("Dly_Date") == null ? null : (String) record.get("Dly_Date"));
        motherRecord.setDlyPlaceHomeType(record.get("Dly_Place_Home_Type") == null ? null : (String) record.get("Dly_Place_Home_Type"));
        motherRecord.setDlyPlacePublic(record.get("Dly_Place_Public") == null ? null : (String) record.get("Dly_Place_Public"));
        motherRecord.setDlyPlacePrivate(record.get("Dly_Place_Private") == null ? null : (String) record.get("Dly_Place_Private"));
        motherRecord.setDlyType(record.get("Dly_Type") == null ? null : (String) record.get("Dly_Type"));
        motherRecord.setDlyComplication(record.get("Dly_Complication") == null ? null : (String) record.get("Dly_Complication"));
        motherRecord.setDischargeDate(record.get("Discharge_Date") == null ? null : (String) record.get("Discharge_Date"));
        motherRecord.setJsyPaidDate(record.get("JSY_Paid_Date") == null ? null : (String) record.get("JSY_Paid_Date"));
        motherRecord.setPncHomeVisit(record.get("PNC_Home_Visit") == null ? null : (String) record.get("PNC_Home_Visit"));
        motherRecord.setPncComplication(record.get("PNC_Complication") == null ? null : (String) record.get("PNC_Complication"));
        motherRecord.setPpcMethod(record.get("PPC_Method") == null ? null : (String) record.get("PPC_Method"));
        motherRecord.setPncCheckup(record.get("PNC_Checkup") == null ? null : (String) record.get("PNC_Checkup"));
        motherRecord.setChild1Name(record.get("Child1_Name") == null ? null : (String) record.get("Child1_Name"));
        motherRecord.setChild1Sex(record.get("Child1_Sex") == null ? null : (String) record.get("Child1_Sex"));
        motherRecord.setChild1Wt(record.get(KilkariConstants.CHILD1_WT) == null || record.get(KilkariConstants.CHILD1_WT).toString().trim().isEmpty() ? null : Double.parseDouble(record.get(KilkariConstants.CHILD1_WT).toString()));
        motherRecord.setChild1Brestfeeding(record.get("Child1_Brestfeeding") == null ? null : (String) record.get("Child1_Brestfeeding"));
        motherRecord.setChild2Name(record.get("Child2_Name") == null ? null : (String) record.get("Child2_Name"));
        motherRecord.setChild2Sex(record.get("Child2_Sex") == null ? null : (String) record.get("Child2_Sex"));
        motherRecord.setChild2Wt(record.get(KilkariConstants.CHILD2_WT) == null || record.get(KilkariConstants.CHILD2_WT).toString().trim().isEmpty() ? null : Double.parseDouble(record.get(KilkariConstants.CHILD2_WT).toString()));
        motherRecord.setChild2Brestfeeding(record.get("Child2_Brestfeeding") == null ? null : (String) record.get("Child2_Brestfeeding"));
        motherRecord.setChild3Name(record.get("Child3_Name") == null ? null : (String) record.get("Child3_Name"));
        motherRecord.setChild3Sex(record.get("Child3_Sex") == null ? null : (String) record.get("Child3_Sex"));
        motherRecord.setChild3Wt(record.get(KilkariConstants.CHILD3_WT) == null || record.get(KilkariConstants.CHILD3_WT).toString().trim().isEmpty() ? null : Double.parseDouble(record.get(KilkariConstants.CHILD3_WT).toString()));
        motherRecord.setChild3Brestfeeding(record.get("Child3_Brestfeeding") == null ? null : (String) record.get("Child3_Brestfeeding"));
        motherRecord.setChild4Name(record.get("Child4_Name") == null ? null : (String) record.get("Child4_Name"));
        motherRecord.setChild4Sex(record.get("Child4_Sex") == null ? null : (String) record.get("Child4_Sex"));
        motherRecord.setChild4Wt(record.get(KilkariConstants.CHILD4_WT) == null || record.get(KilkariConstants.CHILD4_WT).toString().trim().isEmpty() ? null : Double.parseDouble(record.get(KilkariConstants.CHILD4_WT).toString()));
        motherRecord.setChild4Brestfeeding(record.get("Child4_Brestfeeding") == null ? null : (String) record.get("Child4_Brestfeeding"));
        motherRecord.setAge(record.get("Age") == null ? null : Integer.parseInt(record.get("Age").toString()));
        motherRecord.setMthrRegDate(record.get("MTHR_REG_DATE") == null ? null : (String) record.get("MTHR_REG_DATE"));
        motherRecord.setRemarks(record.get(KilkariConstants.REMARKS) == null ? null : (String) record.get(KilkariConstants.REMARKS));
        motherRecord.setAnmID(record.get(KilkariConstants.ANM_ID) == null ? null : Integer.parseInt(record.get(KilkariConstants.ANM_ID).toString()));
        motherRecord.setAshaID(record.get(KilkariConstants.ASHA_ID) == null ? null : Integer.parseInt(record.get(KilkariConstants.ASHA_ID).toString()));
        motherRecord.setCallAns(record.get("Call_Ans") == null ? null : (Boolean) record.get("Call_Ans"));
        motherRecord.setNoCallReason(record.get("NoCall_Reason") == null || record.get("NoCall_Reason").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("NoCall_Reason").toString()));
        motherRecord.setNoPhoneReason(record.get("NoPhone_Reason") == null || record.get("NoPhone_Reason").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("NoPhone_Reason").toString()));
        motherRecord.setCreatedBy(record.get(KilkariConstants.CREATED_BY) == null || record.get(KilkariConstants.CREATED_BY).toString().trim().isEmpty() ? null : Integer.parseInt(record.get(KilkariConstants.CREATED_BY).toString()));
        motherRecord.setUpdatedBy(record.get(KilkariConstants.UPDATED_BY) == null || record.get(KilkariConstants.UPDATED_BY).toString().trim().isEmpty() ? null : Integer.parseInt(record.get(KilkariConstants.UPDATED_BY).toString()));
        motherRecord.setAadharNo(record.get("Aadhar_No") == null || record.get("Aadhar_No").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("Aadhar_No").toString()));
        motherRecord.setBplAPL(record.get("BPL_APL") == null || record.get("BPL_APL").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("BPL_APL").toString()));
        motherRecord.seteId(record.get("EID") == null || record.get("EID").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("EID").toString()));
        motherRecord.seteIdTime(record.get("EIDTime") == null ? null : (String) record.get("EIDTime"));

        return motherRecord;
    }

    public static RchMotherRecord convertMapToRchMother(Map<String, Object> record) { //NO CHECKSTYLE CyclomaticComplexity
        RchMotherRecord rchMotherRecord = new RchMotherRecord();
        rchMotherRecord.setStateId(record.get(KilkariConstants.STATE_ID) == null ? null : (Long) record.get(KilkariConstants.STATE_ID));
        rchMotherRecord.setDistrictId(record.get(KilkariConstants.DISTRICT_ID) == null ? null : (Long) record.get(KilkariConstants.DISTRICT_ID));
        rchMotherRecord.setDistrictName(record.get(KilkariConstants.DISTRICT_NAME) == null ? null : record.get(KilkariConstants.DISTRICT_NAME).toString());
        rchMotherRecord.setTalukaId(record.get(KilkariConstants.TALUKA_ID) == null ? null : record.get(KilkariConstants.TALUKA_ID).toString());
        rchMotherRecord.setTalukaName(record.get(KilkariConstants.TALUKA_NAME) == null ? null : record.get(KilkariConstants.TALUKA_NAME).toString());
        rchMotherRecord.setHealthBlockId(record.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? null : (Long) record.get(KilkariConstants.HEALTH_BLOCK_ID));
        rchMotherRecord.setHealthBlockName(record.get(KilkariConstants.HEALTH_BLOCK_NAME) == null ? null : record.get(KilkariConstants.HEALTH_BLOCK_NAME).toString());
        rchMotherRecord.setPhcId(record.get(KilkariConstants.PHC_ID) == null ? null : (Long) record.get(KilkariConstants.PHC_ID));
        rchMotherRecord.setPhcName(record.get(KilkariConstants.PHC_NAME) == null ? null : record.get(KilkariConstants.PHC_NAME).toString());
        rchMotherRecord.setSubCentreId(record.get(KilkariConstants.SUB_CENTRE_ID) == null ? null : (Long) record.get(KilkariConstants.SUB_CENTRE_ID));
        rchMotherRecord.setSubCentreName(record.get(KilkariConstants.SUB_CENTRE_NAME) == null ? null : record.get(KilkariConstants.SUB_CENTRE_NAME).toString());
        rchMotherRecord.setVillageId(record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? null : (Long) record.get(KilkariConstants.CENSUS_VILLAGE_ID));
        rchMotherRecord.setVillageName(record.get(KilkariConstants.VILLAGE_NAME) == null ? null : record.get(KilkariConstants.VILLAGE_NAME).toString());

        rchMotherRecord.setMctsIdNo(record.get(KilkariConstants.MCTS_ID) == null ? null : record.get(KilkariConstants.MCTS_ID).toString());
        rchMotherRecord.setRegistrationNo(record.get(KilkariConstants.RCH_ID) == null ? null : record.get(KilkariConstants.RCH_ID).toString());
        rchMotherRecord.setName(record.get(KilkariConstants.BENEFICIARY_NAME) == null ? null : record.get(KilkariConstants.BENEFICIARY_NAME).toString());
        rchMotherRecord.setMobileNo(record.get(KilkariConstants.MOBILE_NO) == null ? null : record.get(KilkariConstants.MOBILE_NO).toString());
        rchMotherRecord.setRegistrationDate(record.get(KilkariConstants.MOTHER_REGISTRATION_DATE) == null ? null : record.get(KilkariConstants.MOTHER_REGISTRATION_DATE).toString());
        rchMotherRecord.setLmpDate(record.get(KilkariConstants.LMP) == null ? null : record.get(KilkariConstants.LMP).toString());
        rchMotherRecord.setBirthDate(record.get(KilkariConstants.MOTHER_DOB) == null ? null : record.get(KilkariConstants.MOTHER_DOB).toString());
        rchMotherRecord.setAbortionType(record.get(KilkariConstants.ABORTION_TYPE) == null ? null : record.get(KilkariConstants.ABORTION_TYPE).toString());
        rchMotherRecord.setDeliveryOutcomes(record.get(KilkariConstants.DELIVERY_OUTCOMES) == null ? null : record.get(KilkariConstants.DELIVERY_OUTCOMES).toString());
        rchMotherRecord.setEntryType(record.get(KilkariConstants.DEATH) == null || record.get(KilkariConstants.DEATH).toString().trim().isEmpty() ? null : ((Boolean) record.get(KilkariConstants.DEATH) ? 1 : 0));
        rchMotherRecord.setExecDate(record.get(KilkariConstants.EXECUTION_DATE) == null ? null : record.get(KilkariConstants.EXECUTION_DATE).toString());
        rchMotherRecord.setCaseNo(record.get(KilkariConstants.CASE_NO) == null ? null : (Long) record.get(KilkariConstants.CASE_NO));
        return rchMotherRecord;
    }

    public static ChildRecord convertMapToChild(Map<String, Object> record) { //NO CHECKSTYLE CyclomaticComplexity //NOPMD NcssMethodCount
        ChildRecord childRecord = new ChildRecord();
        childRecord.setStateID(record.get(KilkariConstants.STATE_ID) == null ? null : (Long) record.get(KilkariConstants.STATE_ID));
        childRecord.setDistrictId(record.get(KilkariConstants.DISTRICT_ID) == null ? null : (Long) record.get(KilkariConstants.DISTRICT_ID));
        childRecord.setDistrictName(record.get(KilkariConstants.DISTRICT_NAME) == null ? null : record.get(KilkariConstants.DISTRICT_NAME).toString());
        childRecord.setTalukaId(record.get(KilkariConstants.TALUKA_ID) == null ? null : record.get(KilkariConstants.TALUKA_ID).toString());
        childRecord.setTalukaName(record.get(KilkariConstants.TALUKA_NAME) == null ? null : record.get(KilkariConstants.TALUKA_NAME).toString());
        childRecord.setHealthBlockId((Long) record.get(KilkariConstants.HEALTH_BLOCK_ID));
        childRecord.setHealthBlockName(record.get(KilkariConstants.HEALTH_BLOCK_NAME) == null ? null : record.get(KilkariConstants.HEALTH_BLOCK_NAME).toString());
        childRecord.setPhcId(record.get(KilkariConstants.PHC_ID) == null ? null : (Long) record.get(KilkariConstants.PHC_ID));
        childRecord.setPhcName(record.get(KilkariConstants.PHC_NAME) == null ? null : record.get(KilkariConstants.PHC_NAME).toString());
        childRecord.setSubCentreId(record.get(KilkariConstants.SUB_CENTRE_ID) == null ? null : (Long) record.get(KilkariConstants.SUB_CENTRE_ID));
        childRecord.setSubCentreName(record.get(KilkariConstants.SUB_CENTRE_NAME) == null ? null : record.get(KilkariConstants.SUB_CENTRE_NAME).toString());
        childRecord.setVillageId(record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? null : (Long) record.get(KilkariConstants.CENSUS_VILLAGE_ID));
        childRecord.setVillageName(record.get(KilkariConstants.VILLAGE_NAME) == null ? null : record.get(KilkariConstants.VILLAGE_NAME).toString());
        childRecord.setLastUpdateDate(record.get(KilkariConstants.LAST_UPDATE_DATE) == null ? null : record.get(KilkariConstants.LAST_UPDATE_DATE).toString());

        childRecord.setName(record.get(KilkariConstants.BENEFICIARY_NAME) == null ? null : record.get(KilkariConstants.BENEFICIARY_NAME).toString());

        childRecord.setWhomPhoneNo(record.get(KilkariConstants.MSISDN) == null ? null : record.get(KilkariConstants.MSISDN).toString());
        childRecord.setBirthdate(record.get(KilkariConstants.DOB) == null ? null : record.get(KilkariConstants.DOB).toString());
        childRecord.setRegistrationDate(record.get(KilkariConstants.CHILD_REGISTRATION_DATE) == null ? null : record.get(KilkariConstants.CHILD_REGISTRATION_DATE).toString());
        childRecord.setIdNo(record.get(KilkariConstants.BENEFICIARY_ID) == null ? null : record.get(KilkariConstants.BENEFICIARY_ID).toString());

        String motherId = null;

        if (record.get(KilkariConstants.MOTHER_ID) != null) {
            Object motherRecord = record.get(KilkariConstants.MOTHER_ID);

            try {
                MctsMother motherInstance = (MctsMother) record.get(KilkariConstants.MOTHER_ID);
                motherId = motherInstance.getBeneficiaryId();
            } catch (Exception e) {
                motherId = motherRecord.toString();
            }
        }


        childRecord.setMotherId(motherId);

        childRecord.setEntryType(record.get(KilkariConstants.DEATH) == null || record.get(KilkariConstants.DEATH).toString().trim().isEmpty() ? null : String.valueOf((Boolean) record.get(KilkariConstants.DEATH) ? 1 : 0));

        childRecord.setGpVillage(record.get(KilkariConstants.GP_VILLAGE) == null ? null : (String) record.get(KilkariConstants.GP_VILLAGE));
        childRecord.setAddress(record.get(KilkariConstants.ADDRESS) == null ? null : (String) record.get(KilkariConstants.ADDRESS));
        childRecord.setYr(record.get("Yr") == null || record.get("Yr").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("Yr").toString()));
        childRecord.setCityMaholla(record.get("City_Maholla") == null ? null : (String) record.get("City_Maholla"));
        childRecord.setMotherName(record.get("Mother_Name") == null ? null : (String) record.get("Mother_Name"));
        childRecord.setPhoneNoOfWhom(record.get(KilkariConstants.PH_OF_WHOM) == null ? null : (String) record.get(KilkariConstants.PH_OF_WHOM));
        childRecord.setPlaceOfDelivery(record.get("Place_of_Delivery") == null ? null : (String) record.get("Place_of_Delivery"));
        childRecord.setAnmPhone(record.get(KilkariConstants.ANM_PHONE) == null ? null : (String) record.get(KilkariConstants.ANM_PHONE));
        childRecord.setBloodGroup(record.get("Blood_Group") == null ? null : (String) record.get("Blood_Group"));
        childRecord.setAshaName(record.get(KilkariConstants.ASHA_NAME) == null ? null : (String) record.get(KilkariConstants.ASHA_NAME));
        childRecord.setAshaPhone(record.get(KilkariConstants.ASHA_PHONE) == null ? null : (String) record.get(KilkariConstants.ASHA_PHONE));
        childRecord.setSubCentreName1(record.get(KilkariConstants.SUB_CENTRE_NAME1) == null ? null : (String) record.get(KilkariConstants.SUB_CENTRE_NAME1));
        childRecord.setAnmName(record.get(KilkariConstants.ANM_NAME) == null ? null : (String) record.get(KilkariConstants.ANM_NAME));
        childRecord.setCaste(record.get(KilkariConstants.CASTE) == null ? null : (String) record.get(KilkariConstants.CASTE));
        childRecord.setBcgDt(record.get("BCG_Dt") == null ? null : (String) record.get("BCG_Dt"));
        childRecord.setOpv0Dt(record.get("OPV0_Dt") == null ? null : (String) record.get("OPV0_Dt"));
        childRecord.setHepatitisB1Dt(record.get("HepatitisB1_Dt") == null ? null : (String) record.get("HepatitisB1_Dt"));
        childRecord.setDpt1Dt(record.get("DPT1_Dt") == null ? null : (String) record.get("DPT1_Dt"));
        childRecord.setOpv1Dt(record.get("OPV1_Dt") == null ? null : (String) record.get("OPV1_Dt"));

        childRecord = convertToChild(childRecord, record);

        return childRecord;
    }

    private static ChildRecord convertToChild(ChildRecord childRecord, Map<String, Object> record) { // NO CHECKSTYLE Cyclomatic Complexity
        childRecord.setHepatitisB2Dt(record.get("HepatitisB2_Dt") == null ? null : (String) record.get("HepatitisB2_Dt"));
        childRecord.setdPT2Dt(record.get("DPT2_Dt") == null ? null : (String) record.get("DPT2_Dt"));
        childRecord.setOpv2Dt(record.get("OPV2_Dt") == null ? null : (String) record.get("OPV2_Dt"));
        childRecord.setHepatitisB3Dt(record.get("HepatitisB3_Dt") == null ? null : (String) record.get("HepatitisB3_Dt"));
        childRecord.setDpt3Dt(record.get("DPT3_Dt") == null ? null : (String) record.get("DPT3_Dt"));
        childRecord.setOpv3Dt(record.get("OPV3_Dt") == null ? null : (String) record.get("OPV3_Dt"));
        childRecord.setHepatitisB4Dt(record.get("HepatitisB4_Dt") == null ? null : (String) record.get("HepatitisB4_Dt"));
        childRecord.setMeaslesDt(record.get("Measles_Dt") == null ? null : (String) record.get("Measles_Dt"));
        childRecord.setVitADose1Dt(record.get("VitA_Dose1_Dt") == null ? null : (String) record.get("VitA_Dose1_Dt"));
        childRecord.setMrDt(record.get("MR_Dt") == null ? null : (String) record.get("MR_Dt"));
        childRecord.setDptBoosterDt(record.get("DPTBooster_Dt") == null ? null : (String) record.get("DPTBooster_Dt"));
        childRecord.setOpvBoosterDt(record.get("OPVBooster_Dt") == null ? null : (String) record.get("OPVBooster_Dt"));
        childRecord.setVitADose2Dt(record.get("VitA_Dose2_Dt") == null ? null : (String) record.get("VitA_Dose2_Dt"));
        childRecord.setVitADose3Dt(record.get("VitA_Dose3_Dt") == null ? null : (String) record.get("VitA_Dose3_Dt"));
        childRecord.setJeDt(record.get("JE_Dt") == null ? null : (String) record.get("JE_Dt"));
        childRecord.setVitADose9Dt(record.get("VitA_Dose9_Dt") == null ? null : (String) record.get("VitA_Dose9_Dt"));
        childRecord.setDt5Dt(record.get("DT5_Dt") == null ? null : (String) record.get("DT5_Dt"));
        childRecord.setTt10Dt(record.get("TT10_Dt") == null ? null : (String) record.get("TT10_Dt"));
        childRecord.setTt16Dt(record.get("TT16_Dt") == null ? null : (String) record.get("TT16_Dt"));
        childRecord.setCldRegDate(record.get("CLD_REG_DATE") == null ? null : (String) record.get("CLD_REG_DATE"));
        childRecord.setSex(record.get("Sex") == null ? null : (String) record.get("Sex"));
        childRecord.setVitADose5Dt(record.get("VitA_Dose5_Dt") == null ? null : (String) record.get("VitA_Dose5_Dt"));
        childRecord.setRemarks(record.get(KilkariConstants.REMARKS) == null ? null : (String) record.get(KilkariConstants.REMARKS));
        childRecord.setVitADose6Dt(record.get("VitA_Dose6_Dt") == null ? null : (String) record.get("VitA_Dose6_Dt"));
        childRecord.setAnmID(record.get(KilkariConstants.ANM_ID) == null || record.get(KilkariConstants.ANM_ID).toString().trim().isEmpty() ? null : Integer.parseInt(record.get(KilkariConstants.ANM_ID).toString()));
        childRecord.setAshaID(record.get(KilkariConstants.ASHA_ID) == null || record.get(KilkariConstants.ASHA_ID).toString().trim().isEmpty() ? null : Integer.parseInt(record.get(KilkariConstants.ASHA_ID).toString()));
        childRecord.setVitADose7Dt(record.get("VitA_Dose7_Dt") == null ? null : (String) record.get("VitA_Dose7_Dt"));
        childRecord.setVitADose8Dt(record.get("VitA_Dose8_Dt") == null ? null : (String) record.get("VitA_Dose8_Dt"));
        childRecord.setCreatedBy(record.get(KilkariConstants.CREATED_BY) == null || record.get(KilkariConstants.CREATED_BY).toString().trim().isEmpty() ? null : Integer.parseInt(record.get(KilkariConstants.CREATED_BY).toString()));
        childRecord.setUpdatedBy(record.get(KilkariConstants.UPDATED_BY) == null || record.get(KilkariConstants.UPDATED_BY).toString().trim().isEmpty() ? null : Integer.parseInt(record.get(KilkariConstants.UPDATED_BY).toString()));
        childRecord.setMeasles2Dt(record.get("Measles2_Dt") == null ? null : (String) record.get("Measles2_Dt"));
        childRecord.setWeightofChild(record.get("Weight_of_Child") == null || record.get("Weight_of_Child").toString().trim().isEmpty() ? null : Double.parseDouble(record.get("Weight_of_Child").toString()));
        childRecord.setChildAadhaarNo(record.get("Child_Aadhaar_No") == null || record.get("Child_Aadhaar_No").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("Child_Aadhaar_No").toString()));
        childRecord.setChildEID(record.get("Child_EID") == null || record.get("Child_EID").toString().trim().isEmpty() ? null : Integer.parseInt(record.get("Child_EID").toString()));
        childRecord.setChildEIDTime(record.get("Child_EIDTime") == null ? null : (String) record.get("Child_EIDTime"));
        childRecord.setFatherName(record.get("Father_Name") == null ? null : (String) record.get("Father_Name"));
        childRecord.setBirthCertificateNumber(record.get("Birth_Certificate_Number") == null ? null : (String) record.get("Birth_Certificate_Number"));

        return childRecord;
    }

    public static RchChildRecord convertMapToRchChild(Map<String, Object> record) { //NO CHECKSTYLE CyclomaticComplexity
        RchChildRecord rchChildRecord = new RchChildRecord();
        rchChildRecord.setStateId(record.get(KilkariConstants.STATE_ID) == null ? null : (Long) record.get(KilkariConstants.STATE_ID));
        rchChildRecord.setDistrictId(record.get(KilkariConstants.DISTRICT_ID) == null ? null : (Long) record.get(KilkariConstants.DISTRICT_ID));
        rchChildRecord.setDistrictName(record.get(KilkariConstants.DISTRICT_NAME) == null ? null : record.get(KilkariConstants.DISTRICT_NAME).toString());
        rchChildRecord.setTalukaId(record.get(KilkariConstants.TALUKA_ID) == null ? null : record.get(KilkariConstants.TALUKA_ID).toString());
        rchChildRecord.setTalukaName(record.get(KilkariConstants.TALUKA_NAME) == null ? null : record.get(KilkariConstants.TALUKA_NAME).toString());
        rchChildRecord.setHealthBlockId(record.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? null : (Long) record.get(KilkariConstants.HEALTH_BLOCK_ID));
        rchChildRecord.setHealthBlockName(record.get(KilkariConstants.HEALTH_BLOCK_NAME) == null ? null : record.get(KilkariConstants.HEALTH_BLOCK_NAME).toString());
        rchChildRecord.setPhcId(record.get(KilkariConstants.PHC_ID) == null ? null : (Long) record.get(KilkariConstants.PHC_ID));
        rchChildRecord.setPhcName(record.get(KilkariConstants.PHC_NAME) == null ? null : record.get(KilkariConstants.PHC_NAME).toString());
        rchChildRecord.setSubCentreId(record.get(KilkariConstants.SUB_CENTRE_ID) == null ? null : (Long) record.get(KilkariConstants.SUB_CENTRE_ID));
        rchChildRecord.setSubCentreName(record.get(KilkariConstants.SUB_CENTRE_NAME) == null ? null : record.get(KilkariConstants.SUB_CENTRE_NAME).toString());
        rchChildRecord.setVillageId(record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? null : (Long) record.get(KilkariConstants.CENSUS_VILLAGE_ID));
        rchChildRecord.setVillageName(record.get(KilkariConstants.VILLAGE_NAME) == null ? null : record.get(KilkariConstants.VILLAGE_NAME).toString());

        rchChildRecord.setName(record.get(KilkariConstants.BENEFICIARY_NAME) == null ? null : record.get(KilkariConstants.BENEFICIARY_NAME).toString());

        rchChildRecord.setMobileNo(record.get(KilkariConstants.MOBILE_NO) == null ? null : record.get(KilkariConstants.MOBILE_NO).toString());
        rchChildRecord.setBirthdate(record.get(KilkariConstants.DOB) == null ? null : record.get(KilkariConstants.DOB).toString());
        rchChildRecord.setRegistrationDate(record.get(KilkariConstants.CHILD_REGISTRATION_DATE) == null ? null : record.get(KilkariConstants.CHILD_REGISTRATION_DATE).toString());
        rchChildRecord.setMctsId(record.get(KilkariConstants.MCTS_ID) == null ? null : record.get(KilkariConstants.MCTS_ID).toString());
        rchChildRecord.setMctsMotherIdNo(record.get(KilkariConstants.MCTS_MOTHER_ID) == null ? null : record.get(KilkariConstants.MCTS_MOTHER_ID).toString());
        rchChildRecord.setRegistrationNo(record.get(KilkariConstants.RCH_ID) == null ? null : record.get(KilkariConstants.RCH_ID).toString());
        rchChildRecord.setMotherRegistrationNo(record.get(KilkariConstants.RCH_MOTHER_ID) == null ? null : record.get(KilkariConstants.RCH_MOTHER_ID).toString());
        rchChildRecord.setEntryType(record.get(KilkariConstants.DEATH) == null || record.get(KilkariConstants.DEATH).toString().trim().isEmpty() ? null : String.valueOf((Boolean) record.get(KilkariConstants.DEATH) ? 1 : 0));
        rchChildRecord.setExecDate(record.get(KilkariConstants.EXECUTION_DATE) == null ? null : record.get(KilkariConstants.EXECUTION_DATE).toString());

        return rchChildRecord;
    }
}
