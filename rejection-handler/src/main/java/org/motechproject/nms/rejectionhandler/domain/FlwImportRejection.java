package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.LocalDate;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.rch.domain.RchUserType;

import java.util.Date;

/**
 * Created by vishnu on 12/7/17.
 */
@Entity(tableName = "nms_flw_rejects")
public class FlwImportRejection {

    @Field
    private Long stateId;

    @Field
    private Long districtId;

    @Field
    private String districtName;

    @Field
    private Long talukaId;

    @Field
    private String talukaName;

    @Field
    private Long healthBlockId;

    @Field
    private String healthBlockName;

    @Field
    private Long phcId;

    @Field
    private String phcName;

    @Field
    private Long subcentreId;

    @Field
    private String subcentreName;

    @Field
    private Long villageId;

    @Field
    private String villageName;

    @Field
    private Long flwId;

    @Field
    private Long msisdn;

    @Field
    private String gf_name;

    @Field
    private String gf_type;

    @Field
    private String gf_status;

    @Field
    private Date exec_date;

    @Field
    private Date reg_date;

    @Field
    private String sex;

    @Field
    private String type;

    @Field
    private String sms_reply;

    @Field
    private String aadharNo;

    @Field
    private Date createdOn;

    @Field
    private Date updatedOn;

    @Field
    private Long bankId;

    @Field
    private String branchName;

    @Field
    private String ifscIdCode;

    @Field
    private String bankName;

    @Field
    private String accountNumber;

    @Field
    private Boolean isAadharLinked;

    @Field
    private Date verifyDate;

    @Field
    private String verifierName;

    @Field
    private String verifierId;

    @Field
    private Boolean callAns;

    @Field
    private Boolean isPhoneNoCorrect;

    @Field
    private String noCall_Reason;

    @Field
    private String noPhone_Reason;

    @Field
    private String verifier_Remarks;

    @Field
    private String gf_address;

    @Field
    private String husband_name;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private String source;

    @Field
    private Date creationDate;

    @Field
    private Date modificationDate;


}
