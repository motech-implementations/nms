package org.motechproject.nms.testing.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MctsHelper {

    private static final String TESTING_DIRECTORY = "testing.directory";
    private static final long MIN_ID_NO = 100000000000000000L;
    private static final long MAX_ID_NO = 999999999999999999L;
    private static final int ABORTION_PERCENT = 3;
    private static final int PROGRESS_INTERVAL = 100;

    private static final String MCTSMOMS = "mctsmoms.csv";
    private static final String MCTSKIDS = "mctskids.csv";
    private static final long MAX_MSISDN = 9999999999L;
    private static final long MIN_MSISDN = 1000000000L;
    private static final int MIN_MOM_AGE = 15;
    private static final int MAX_MOM_AGE = 50;
    private static final int MIN_LMP_DAYS = 84;
    private static final int MAX_LMP_DAYS = 329;
    private static final int STILL_BIRTH_PERCENT = 3;
    private static final int DEATH_PERCENT = 1;
    private static final String DEATH = "9";
    private static final String DEATH_NONE = "";
    private static final String ABORTION1 = "Spontaneous";
    private static final String ABORTION2 = "MTP<12 Weeks";
    private static final String ABORTION3 = "MTP>12 Weeks";
    private static final String STILL_BIRTH = "0";
    private static final String STILL_BIRTH_NONE = "";
    private static final String ABORTION_NONE1 = "";
    private static final String ABORTION_NONE2 = "None";
    private static final String[] MCTS_MOM_FIELDS = {"StateID", "District_ID", "District_Name", "Taluka_ID",
            "Taluka_Name", "HealthBlock_ID", "HealthBlock_Name", "PHC_ID", "PHC_Name", "SubCentre_ID", "SubCentre_Name",
            "Village_ID", "Village_Name", "Yr", "GP_Village", "Address", "ID_No", "Name", "Husband_Name",
            "PhoneNo_Of_Whom", "Whom_PhoneNo", "Birthdate", "JSY_Beneficiary", "Caste", "SubCentre_Name1", "ANM_Name",
            "ANM_Phone", "ASHA_Name", "ASHA_Phone", "Delivery_Lnk_Facility", "Facility_Name", "LMP_Date", "ANC1_Date",
            "ANC2_Date", "ANC3_Date", "ANC4_Date", "TT1_Date", "TT2_Date", "TTBooster_Date", "IFA100_Given_Date",
            "Anemia", "ANC_Complication", "RTI_STI", "Dly_Date", "Dly_Place_Home_Type", "Dly_Place_Public",
            "Dly_Place_Private", "Dly_Type", "Dly_Complication", "Discharge_Date", "JSY_Paid_Date", "Abortion",
            "PNC_Home_Visit", "PNC_Complication", "PPC_Method", "PNC_Checkup", "Outcome_Nos", "Child1_Name",
            "Child1_Sex", "Child1_Wt", "Child1_Brestfeeding", "Child2_Name", "Child2_Sex", "Child2_Wt",
            "Child2_Brestfeeding", "Child3_Name", "Child3_Sex", "Child3_Wt", "Child3_Brestfeeding", "Child4_Name",
            "Child4_Sex", "Child4_Wt", "Child4_Brestfeeding", "Age", "MTHR_REG_DATE", "LastUpdateDate", "Remarks",
            "ANM_ID", "ASHA_ID", "Call_Ans", "NoCall_Reason", "NoPhone_Reason", "Created_By", "Updated_By",
            "Aadhar_No", "BPL_APL", "EID", "EIDTime", "Entry_Type"};
    private static final String[] MCTS_KID_FIELDS = {"StateID", "District_ID", "District_Name", "Taluka_ID",
            "Taluka_Name", "HealthBlock_ID", "HealthBlock_Name", "PHC_ID", "PHC_Name", "SubCentre_ID", "SubCentre_Name",
            "Village_ID", "Village_Name", "Yr", "City_Maholla", "GP_Village", "Address", "ID_No", "Name", "Mother_Name",
            "Mother_ID", "PhoneNo_Of_Whom", "Whom_PhoneNo", "Birthdate", "Place_of_Delivery", "Blood_Group", "Caste",
            "SubCentre_Name1", "ANM_Name", "ANM_Phone", "ASHA_Name", "ASHA_Phone", "BCG_Dt", "OPV0_Dt",
            "HepatitisB1_Dt", "DPT1_Dt", "OPV1_Dt", "HepatitisB2_Dt", "DPT2_Dt", "OPV2_Dt", "HepatitisB3_Dt", "DPT3_Dt",
            "OPV3_Dt", "HepatitisB4_Dt", "Measles_Dt", "VitA_Dose1_Dt", "MR_Dt", "DPTBooster_Dt", "OPVBooster_Dt",
            "VitA_Dose2_Dt", "VitA_Dose3_Dt", "JE_Dt", "VitA_Dose9_Dt", "DT5_Dt", "TT10_Dt", "TT16_Dt", "CLD_REG_DATE",
            "Sex", "VitA_Dose5_Dt", "VitA_Dose6_Dt", "VitA_Dose7_Dt", "VitA_Dose8_Dt", "LastUpdateDate", "Remarks",
            "ANM_ID", "ASHA_ID", "Created_By", "Updated_By", "Measles2_Dt", "Weight_of_Child", "Entry_Type"
    };
   
    private SettingsFacade settingsFacade;
    private StateDataService stateDataService;
    private DistrictDataService districtDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsHelper.class);
    private Random random = new Random(System.currentTimeMillis());
    private List<Long> states;
    private Map<Long, List<Long>> districts;
    private Map<Long, String> districtNames;

    public MctsHelper(SettingsFacade settingsFacade, StateDataService stateDataService,
                      DistrictDataService districtDataService) {
        this.settingsFacade = settingsFacade;
        this.stateDataService = stateDataService;
        this.districtDataService = districtDataService;
    }

    private String getTestingDirectory() {
        String dir = settingsFacade.getProperty(TESTING_DIRECTORY);
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    private Long randomStateId() {
        if (states == null) {
            states = new ArrayList<>();
        }
        if (states.size() <= 0) {
            for (State state : stateDataService.retrieveAll()) {
                states.add(state.getCode());
            }
        }
        if (states.size() <= 0) {
            throw new IllegalStateException("There are no State entities in the database!");
        }
        return states.get(random.nextInt(states.size()));
    }


    private Long randomDistrictId(Long state) {
        if (districts == null) {
            districts = new HashMap<>();
            districtNames = new HashMap<>();
            if (stateDataService.count() > 0) {
                for (District district : districtDataService.retrieveAll()) {
                    if (!districts.containsKey(district.getState().getCode())) {
                        districts.put(district.getState().getCode(), new ArrayList<Long>());
                    }
                    districts.get(district.getState().getCode()).add(district.getCode());
                    districtNames.put(district.getCode(), district.getName());
                }
            } else {
                throw new IllegalStateException("There are no District entities in the database!");
            }
        }
        if (districts.containsKey(state)) {
            List<Long> stateDistricts = districts.get(state);
            int index = random.nextInt(stateDistricts.size());
            return stateDistricts.get(index);
        }
        throw new IllegalStateException(String.format("No districts for state %d", state));
    }


    private Long randomLong(long min, long max) {
        long m = max - min + 1;
        long bits;
        long val;
        do {
            bits = (random.nextLong() << 1) >>> 1;
            val = bits % m;
        } while (bits - val + (m - 1) < 0L);
        return min + val;
    }


    private Long randomIdNo() {
        return randomLong(MIN_ID_NO, MAX_ID_NO);
    }


    private Long randomMsisdn() {
        return randomLong(MIN_MSISDN, MAX_MSISDN);
    }


    private String randomName() {
        //todo: better!
        return String.format("RandomName%d", random.nextInt());
    }


    private Integer randomInt(int min, int max) {
        return min + random.nextInt(max - min);
    }


    private Boolean isLeapYear(int year) {
        DateTime feb29 = new DateTime().withYear(year).withMonthOfYear(2).withDayOfMonth(28).plusDays(1);
        return feb29.getMonthOfYear() == 2;
    }


    private DateTime randomLmpDate() {
        // between 12 - 47 weeks before today
        return new DateTime().minusDays(randomInt(MIN_LMP_DAYS, MAX_LMP_DAYS));
    }


    private DateTime randomMomBirthDate() {
        DateTime today = new DateTime();
        int year = today.getYear() - randomInt(MIN_MOM_AGE, MAX_MOM_AGE);
        int day = randomInt(1, isLeapYear(year) ? 365 : 364);

        return new DateTime().withYear(year).withDayOfYear(day);
    }


    private DateTime randomKidBirthDate() {
        DateTime today = new DateTime();
        //todo look into what's a realistic number of days
        int days = randomInt(1, 60);

        return today.minusDays(days);
    }


    private String randomDeath() {
        if (random.nextInt(100) < DEATH_PERCENT) {
            return DEATH;
        }

        return DEATH_NONE;
    }


    private String randomAbortion(String death) {
        if (DEATH_NONE.equals(death) && random.nextInt(100) < ABORTION_PERCENT) {
            int type = random.nextInt(100);
            if (type < 33) {
                return ABORTION1;
            } else if (type < 66) {
                return ABORTION2;
            } else {
                return ABORTION3;
            }
        }

        return random.nextInt(100) < 50 ? ABORTION_NONE1 : ABORTION_NONE2;
    }


    private String randomStillBirth(String death, String abortion) {
        if (DEATH_NONE.equals(death) &&
                (ABORTION_NONE1.equals(abortion) || ABORTION_NONE2.equals(abortion)) &&
                random.nextInt(100) < STILL_BIRTH_PERCENT) {
            return STILL_BIRTH;
        }

        return STILL_BIRTH_NONE;
    }


    public String oneMctsMom() {

        Long stateId = randomStateId();
        Long districtId = randomDistrictId(stateId);
        String districtName = districtNames.get(districtId);
        String death = randomDeath();
        String abortion = randomAbortion(death);
        String stillBirth = randomStillBirth(death, abortion);
        Long idNo = randomIdNo();
        String name = randomName();
        Long msisdn = randomMsisdn();
        DateTime birthDate = randomMomBirthDate();
        DateTime lmpdate = randomLmpDate();
        StringBuilder sb = new StringBuilder();

        //StateID
        sb.append(stateId.toString());
        sb.append("\t");

        //District_ID
        sb.append(districtId.toString());
        sb.append("\t");

        //District_Name
        sb.append(districtName);
        sb.append("\t");

        //Taluka_ID
        //Taluka_Name
        //HealthBlock_ID
        //HealthBlock_Name
        //PHC_ID
        //PHC_Name
        //SubCentre_ID
        //SubCentre_Name
        //Village_ID
        //Village_Name
        //Yr
        //GP_Village
        //Address
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t\t");

        //ID_No
        sb.append(idNo.toString());
        sb.append("\t");

        //Name
        sb.append(name);
        sb.append("\t");

        //Husband_Name
        //PhoneNo_Of_Whom
        sb.append("\t\t");

        //Whom_PhoneNo
        sb.append(msisdn.toString());
        sb.append("\t");

        //Birthdate
        sb.append(birthDate.toString("dd-MM-yyyy"));
        sb.append("\t");

        //JSY_Beneficiary
        //Caste
        //SubCentre_Name1
        //ANM_Name
        //ANM_Phone
        //ASHA_Name
        //ASHA_Phone
        //Delivery_Lnk_Facility
        //Facility_Name
        sb.append("\t\t\t\t\t\t\t\t\t");

        //LMP_Date
        sb.append(lmpdate.toString("dd-MM-yyyy"));
        sb.append("\t");

        //ANC1_Date
        //ANC2_Date
        //ANC3_Date
        //ANC4_Date
        //TT1_Date
        //TT2_Date
        //TTBooster_Date
        //IFA100_Given_Date
        //Anemia
        //ANC_Complication
        //RTI_STI
        //Dly_Date
        //Dly_Place_Home_Type
        //Dly_Place_Public
        //Dly_Place_Private
        //Dly_Type
        //Dly_Complication
        //Discharge_Date
        //JSY_Paid_Date
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        //Abortion
        sb.append(abortion);
        sb.append("\t");

        //PNC_Home_Visit
        //PNC_Complication
        //PPC_Method
        //PNC_Checkup
        sb.append("\t\t\t\t");

        //Outcome_Nos
        sb.append(stillBirth);
        sb.append("\t");

        //Child1_Name
        //Child1_Sex
        //Child1_Wt
        //Child1_Brestfeeding
        //Child2_Name
        //Child2_Sex
        //Child2_Wt
        //Child2_Brestfeeding
        //Child3_Name
        //Child3_Sex
        //Child3_Wt
        //Child3_Brestfeeding
        //Child4_Name
        //Child4_Sex
        //Child4_Wt
        //Child4_Brestfeeding
        //Age
        //MTHR_REG_DATE
        //LastUpdateDate
        //Remarks
        //ANM_ID
        //ASHA_ID
        //Call_Ans
        //NoCall_Reason
        //NoPhone_Reason
        //Created_By
        //Updated_By
        //Aadhar_No
        //BPL_APL
        //EID
        //EIDTime
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        //Entry_Type
        sb.append(death);

        return sb.toString();
    }


    public String oneMctsKid() {

        Long stateId = randomStateId();
        Long districtId = randomDistrictId(stateId);
        String districtName = districtNames.get(districtId);
        String death = randomDeath();
        Long idNo = randomIdNo();
        String name = randomName();
        Long msisdn = randomMsisdn();
        DateTime birthDate = randomKidBirthDate();

        StringBuilder sb = new StringBuilder();
        
        //StateID
        sb.append(stateId.toString());
        sb.append("\t");

        //District_ID
        sb.append(districtId.toString());
        sb.append("\t");

        //District_Name
        sb.append(districtName);
        sb.append("\t");

        //Taluka_ID
        //Taluka_Name
        //HealthBlock_ID
        //HealthBlock_Name
        //PHC_ID
        //PHC_Name
        //SubCentre_ID
        //SubCentre_Name
        //Village_ID
        //Village_Name
        //Yr
        //City_Maholla
        //GP_Village
        //Address
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        //ID_No
        sb.append(idNo.toString());
        sb.append("\t");

        //Name
        sb.append(name);
        sb.append("\t");

        //Mother_Name
        //todo try with some existing moms
        sb.append("\t");

        //Mother_ID
        //todo try with some existing moms
        sb.append("\t");

        //PhoneNo_Of_Whom
        sb.append("\t");

        //Whom_PhoneNo
        sb.append(msisdn.toString());
        sb.append("\t");

        //Birthdate
        sb.append(birthDate.toString("dd-MM-yyyy"));
        sb.append("\t");

        //Place_of_Delivery
        //Blood_Group
        //Caste
        //SubCentre_Name1
        //ANM_Name
        //ANM_Phone
        //ASHA_Name
        //ASHA_Phone
        //BCG_Dt
        //OPV0_Dt
        //HepatitisB1_Dt
        //DPT1_Dt
        //OPV1_Dt
        //HepatitisB2_Dt
        //DPT2_Dt
        //OPV2_Dt
        //HepatitisB3_Dt
        //DPT3_Dt
        //OPV3_Dt
        //HepatitisB4_Dt
        //Measles_Dt
        //VitA_Dose1_Dt
        //MR_Dt
        //DPTBooster_Dt
        //OPVBooster_Dt
        //VitA_Dose2_Dt
        //VitA_Dose3_Dt
        //JE_Dt
        //VitA_Dose9_Dt
        //DT5_Dt
        //TT10_Dt
        //TT16_Dt
        //CLD_REG_DATE
        //Sex
        //VitA_Dose5_Dt
        //VitA_Dose6_Dt
        //VitA_Dose7_Dt
        //VitA_Dose8_Dt
        //LastUpdateDate
        //Remarks
        //ANM_ID
        //ASHA_ID
        //Created_By
        //Updated_By
        //Measles2_Dt
        //Weight_of_Child
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        //Entry_Type
        sb.append(death);

        return sb.toString();
    }


    private File findNewFileName(boolean mom) {
        int retry = 1;
        File file = new File(getTestingDirectory(), String.format("%s.%d", mom ? MCTSMOMS : MCTSKIDS, retry));
        while (file.exists()) {
            retry += 1;
            file = new File(getTestingDirectory(), String.format("%s.%d", mom ? MCTSMOMS : MCTSKIDS, retry));
        }
        return file;
    }


    private String createMctsRecipient(boolean mom, int count) throws IOException {

        Timer timer = mom ? new Timer("mom", "moms") :  new Timer("kid", "kids");
        File file = findNewFileName(mom);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(StringUtils.join(mom ? MCTS_MOM_FIELDS : MCTS_KID_FIELDS, "\t"));
        writer.newLine();
        for (int i = 0; i < count; i++) {

            writer.write(mom ? oneMctsMom() : oneMctsKid());
            writer.newLine();

            if (i > 0 && i % PROGRESS_INTERVAL == 0) {
                LOGGER.debug("Created {}", timer.frequency(i));
            }
        }
        writer.close();

        LOGGER.debug("Created {}", timer.frequency(count));


        Runtime runtime = Runtime.getRuntime();
        runtime.exec(String.format("cp %s %s",
                file.getAbsolutePath(), new File(getTestingDirectory(), mom ? MCTSMOMS : MCTSKIDS).getAbsolutePath()));

        LOGGER.debug(file.getName());

        return String.format("%s\t%s", file.getAbsolutePath(), new DecimalFormat("#,##0").format(count));
    }


    public String createMoms(int count) throws IOException { //NOPMD NcssMethodCount

        LOGGER.debug("createMoms(count={})", count);
        return createMctsRecipient(true, count);
    }


    public String createKids(int count) throws IOException { //NOPMD NcssMethodCount

        LOGGER.debug("createKidss(count={})", count);
        return createMctsRecipient(false, count);
    }
}
