package org.motechproject.nms.testing.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service("testingService")
public class TestingServiceImpl implements TestingService {

    private static final String TESTING_ENVIRONMENT = "testing.environment";
    private static final String TESTING_DIRECTORY = "testing.directory";
    private static final int PREGNANCY_PACK_WEEKS = 72;
    private static final int CHILD_PACK_WEEKS = 48;
    private static final int TWO_MINUTES = 120;
    private static final int TEN_SECS = 10;
    private static final long MIN_ID_NO = 100000000000000000L;
    private static final long MAX_ID_NO = 999999999999999999L;
    private static final int ABORTION_PERCENT = 3;
    private static final int PROGRESS_INTERVAL = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestingServiceImpl.class);
    public static final String CHILD_PACK = "childPack";
    public static final String PREGNANCY_PACK = "pregnancyPack";
    public static final String TESTING_SERVICE_FORBIDDEN = "calling TestingService in a production environment is forbidden!";
    public static final String MCTSMOMS = "mctsmoms.csv";
    public static final long MAX_MSISDN = 9999999999L;
    public static final long MIN_MSISDN = 1000000000L;
    public static final int MIN_MOM_AGE = 15;
    public static final int MAX_MOM_AGE = 50;
    public static final int MIN_LMP_DAYS = 84;
    public static final int MAX_LMP_DAYS = 329;
    public static final int STILL_BIRTH_PERCENT = 3;
    public static final int DEATH_PERCENT = 1;
    public static final String DEATH = "9";
    public static final String DEATH_NONE = "";
    public static final String ABORTION1 = "Spontaneous";
    public static final String ABORTION2 = "MTP<12 Weeks";
    public static final String ABORTION3 = "MTP>12 Weeks";
    public static final String STILL_BIRTH = "0";
    public static final String STILL_BIRTH_NONE = "";
    public static final String ABORTION_NONE1 = "";
    public static final String ABORTION_NONE2 = "None";

    private List<Long> states;
    private Map<Long, List<Long>> districts;
    private Map<Long, String> districtNames;


    private static final String[] TABLES = {
        "ALERTS_MODULE_ALERT",
        "ALERTS_MODULE_ALERT_DATA",
        "ALERTS_MODULE_ALERT__TRASH",
        "ALERTS_MODULE_ALERT__TRASH_DATA",
        "MTRAINING_MODULE_ACTIVITYRECORD",
        "MTRAINING_MODULE_ACTIVITYRECORD__TRASH",
        "MTRAINING_MODULE_BOOKMARK",
        "MTRAINING_MODULE_BOOKMARK__TRASH",
        "MTRAINING_MODULE_CHAPTER",
        "MTRAINING_MODULE_CHAPTER__TRASH",
        "MTRAINING_MODULE_COURSE",
        "MTRAINING_MODULE_COURSEUNITMETADATA",
        "MTRAINING_MODULE_COURSEUNITMETADATA__TRASH",
        "MTRAINING_MODULE_COURSE__TRASH",
        "MTRAINING_MODULE_LESSON",
        "MTRAINING_MODULE_LESSON__TRASH",
        "MTRAINING_MODULE_QUESTION",
        "MTRAINING_MODULE_QUESTION__TRASH",
        "MTRAINING_MODULE_QUIZ",
        "MTRAINING_MODULE_QUIZ__TRASH",
        "NMS_KK_SUMMARY_RECORDS_STATUSSTATS",
        "NMS_KK_SUMMARY_RECORDS__TRASH_STATUSSTATS",
        "nms_call_content",
        "nms_call_content__TRASH",
        "nms_states_join_circles",
        "nms_states_join_circles__TRASH",
        "nms_circles",
        "nms_circles__TRASH",
        "nms_csv_audit_records",
        "nms_csv_audit_records__TRASH",
        "nms_deployed_services",
        "nms_deployed_services__TRASH",
        "nms_districts",
        "nms_districts__TRASH",
        "nms_flw_cdrs",
        "nms_flw_cdrs__TRASH",
        "nms_front_line_workers",
        "nms_front_line_workers__TRASH",
        "nms_health_blocks",
        "nms_health_blocks__TRASH",
        "nms_health_facilities",
        "nms_health_facilities__TRASH",
        "nms_health_facility_types",
        "nms_health_facility_types__TRASH",
        "nms_health_sub_facilities",
        "nms_health_sub_facilities__TRASH",
        "nms_imi_cdrs",
        "nms_imi_cdrs__TRASH",
        "nms_imi_csrs",
        "nms_imi_csrs__TRASH",
        "nms_imi_file_audit_records",
        "nms_imi_file_audit_records__TRASH",
        "nms_inbox_call_data",
        "nms_inbox_call_data__TRASH",
        "nms_inbox_call_details",
        "nms_inbox_call_details__TRASH",
        "nms_kk_retry_records",
        "nms_kk_retry_records__TRASH",
        "nms_kk_summary_records",
        "nms_kk_summary_records__TRASH",
        "nms_ma_completion_records",
        "nms_ma_completion_records__TRASH",
        "nms_ma_course",
        "nms_ma_course__TRASH",
        "nms_mcts_beneficiaries__TRASH",
        "nms_mcts_children",
        "nms_mcts_children__TRASH",
        "nms_mcts_mothers",
        "nms_mcts_mothers__TRASH",
        "nms_national_default_language",
        "nms_national_default_language__TRASH",
        "nms_service_usage_caps",
        "nms_service_usage_caps__TRASH",
        "nms_states",
        "nms_states__TRASH",
        "nms_subscribers",
        "nms_subscribers__TRASH",
        "nms_languages",
        "nms_languages__TRASH",
        "nms_subscription_errors",
        "nms_subscription_errors__TRASH",
        "nms_subscription_pack_messages",
        "nms_subscription_pack_messages__TRASH",
        "nms_subscription_packs",
        "nms_subscription_packs__TRASH",
        "nms_subscriptions",
        "nms_subscriptions__TRASH",
        "nms_talukas",
        "nms_talukas__TRASH",
        "nms_villages",
        "nms_villages__TRASH",
        "nms_whitelist_entries",
        "nms_whitelist_entries__TRASH",
        "nms_whitelisted_states",
        "nms_whitelisted_states__TRASH",
    };

    private Random random = new Random(System.currentTimeMillis());




    /**
     * Kilkari
     */
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionPackDataService subscriptionPackDataService;

    /**
     * Region
     */
    @Autowired
    private DistrictDataService districtDataService;
    @Autowired
    private StateDataService stateDataService;


    /**
     * SettingsFacade
     */
    @Autowired
    @Qualifier("testingSettings")
    private SettingsFacade settingsFacade;



    public TestingServiceImpl() {
        //
        // Should only happen on dev / CI machines, so no need to save/restore settings
        //
        System.setProperty("org.motechproject.testing.osgi.http.numTries", "1");
    }


    private void changeConstraints(final boolean disable) {
        SqlQueryExecution sqe = new SqlQueryExecution() {

            @Override
            public String getSqlQuery() {
                return String.format("SET FOREIGN_KEY_CHECKS = %d", disable ? 0 : 1);
            }

            @Override
            public Object execute(Query query) {
                query.execute();
                return null;
            }
        };
        stateDataService.executeSQLQuery(sqe);
    }


    private void disableConstraints() {
        changeConstraints(true);
    }


    private void enableConstraints() {
        changeConstraints(false);
    }


    private void truncateTable(final String table) throws SQLIntegrityConstraintViolationException{
        SqlQueryExecution sqe = new SqlQueryExecution() {

            @Override
            public String getSqlQuery() {
                return String.format("DELETE FROM %s WHERE 1=1", table);
            }

            @Override
            public Object execute(Query query) {
                query.execute();
                return null;
            }
        };
        stateDataService.executeSQLQuery(sqe);
    }


    @Override
    public void clearDatabase() {
        Timer timer = new Timer();

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException(TESTING_SERVICE_FORBIDDEN);
        }

        disableConstraints();

        for (String table : TABLES) {
            try {
                truncateTable(table);
            } catch (SQLIntegrityConstraintViolationException e) {
                throw new IllegalStateException(String.format("%s while deleting %s", e.getMessage(), table), e);
            }
        }

        enableConstraints();

        LOGGER.debug("clearDatabase: {}", timer.time());
    }


    public SubscriptionPack childPack() {
        if (subscriptionPackDataService.byName(CHILD_PACK) == null) {
            createSubscriptionPack(CHILD_PACK, SubscriptionPackType.CHILD, CHILD_PACK_WEEKS, 1);
        }
        return subscriptionService.getSubscriptionPack(CHILD_PACK);
    }


    public SubscriptionPack pregnancyPack() {
        if (subscriptionPackDataService.byName(PREGNANCY_PACK) == null) {
            createSubscriptionPack(PREGNANCY_PACK, SubscriptionPackType.PREGNANCY, PREGNANCY_PACK_WEEKS, 2);
        }
        return subscriptionService.getSubscriptionPack(PREGNANCY_PACK);
    }


    private void createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                        int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = genratePackMessageList(weeks, messagesPerWeek);
        subscriptionPackDataService.create(new SubscriptionPack(name, type, weeks, messagesPerWeek, messages));
    }


    private List<SubscriptionPackMessage> genratePackMessageList(int packWeeks, int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= packWeeks; week++) {
            messages.add(new SubscriptionPackMessage(String.format("w%s_1", week),
                    String.format("w%s_1.wav", week),
                    TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(String.format("w%s_2", week),
                        String.format("w%s_2.wav", week),
                        TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));
            }
        }
        return messages;
    }


    @Override
    public void createSubscriptionPacks() {

        LOGGER.debug("createSubscriptionPacks()");

        Timer timer = new Timer();

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException(TESTING_SERVICE_FORBIDDEN);
        }

        subscriptionPackDataService.create(
                new SubscriptionPack(
                        CHILD_PACK,
                        SubscriptionPackType.CHILD,
                        CHILD_PACK_WEEKS,
                        1,
                        genratePackMessageList(CHILD_PACK_WEEKS, 1)
                )
        );
        subscriptionPackDataService.create(
                new SubscriptionPack(
                        PREGNANCY_PACK,
                        SubscriptionPackType.PREGNANCY,
                        PREGNANCY_PACK_WEEKS,
                        2,
                        genratePackMessageList(PREGNANCY_PACK_WEEKS, 2)
                )
        );

        LOGGER.debug("createSubscriptionPacks: {}", timer.time());
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


    private DateTime randomBirthDate() {
        DateTime today = new DateTime();
        int year = today.getYear() - randomInt(MIN_MOM_AGE, MAX_MOM_AGE);
        int day = randomInt(1, isLeapYear(year) ? 365 : 364);

        return new DateTime().withYear(year).withDayOfYear(day);
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


    @Override
    public String createMctsMoms(int count) throws IOException { //NOPMD NcssMethodCount

        String[] fields = {"StateID", "District_ID", "District_Name", "Taluka_ID", "Taluka_Name", "HealthBlock_ID",
                "HealthBlock_Name", "PHC_ID", "PHC_Name", "SubCentre_ID", "SubCentre_Name", "Village_ID",
                "Village_Name", "Yr", "GP_Village", "Address", "ID_No", "Name", "Husband_Name", "PhoneNo_Of_Whom",
                "Whom_PhoneNo", "Birthdate", "JSY_Beneficiary", "Caste", "SubCentre_Name1", "ANM_Name", "ANM_Phone",
                "ASHA_Name", "ASHA_Phone", "Delivery_Lnk_Facility", "Facility_Name", "LMP_Date", "ANC1_Date",
                "ANC2_Date", "ANC3_Date", "ANC4_Date", "TT1_Date", "TT2_Date", "TTBooster_Date", "IFA100_Given_Date",
                "Anemia", "ANC_Complication", "RTI_STI", "Dly_Date", "Dly_Place_Home_Type", "Dly_Place_Public",
                "Dly_Place_Private", "Dly_Type", "Dly_Complication", "Discharge_Date", "JSY_Paid_Date", "Abortion",
                "PNC_Home_Visit", "PNC_Complication", "PPC_Method", "PNC_Checkup", "Outcome_Nos", "Child1_Name",
                "Child1_Sex", "Child1_Wt", "Child1_Brestfeeding", "Child2_Name", "Child2_Sex", "Child2_Wt",
                "Child2_Brestfeeding", "Child3_Name", "Child3_Sex", "Child3_Wt", "Child3_Brestfeeding", "Child4_Name",
                "Child4_Sex", "Child4_Wt", "Child4_Brestfeeding", "Age", "MTHR_REG_DATE", "LastUpdateDate", "Remarks",
                "ANM_ID", "ASHA_ID", "Call_Ans", "NoCall_Reason", "NoPhone_Reason", "Created_By", "Updated_By",
                "Aadhar_No", "BPL_APL", "EID", "EIDTime", "Entry_Type"};

        LOGGER.debug("createMctsMoms(count={})", count);

        if (!Boolean.parseBoolean(settingsFacade.getProperty(TESTING_ENVIRONMENT))) {
            throw new IllegalStateException(TESTING_SERVICE_FORBIDDEN);
        }

        Timer timer = new Timer("mom", "moms");
        File file = new File(getTestingDirectory(), MCTSMOMS);
        int retry = 0;
        while (file.exists()) {
            retry += 1;
            file = new File(getTestingDirectory(), String.format("%s.%d", MCTSMOMS, retry));
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("###ignore this line###");
        writer.newLine();
        writer.newLine();
        writer.write(StringUtils.join(fields, "\t"));
        writer.newLine();
        for (int i = 0; i < count; i++) {

            Long stateId = randomStateId();
            Long districtId = randomDistrictId(stateId);
            String districtName = districtNames.get(districtId);
            String death = randomDeath();
            String abortion = randomAbortion(death);
            String stillBirth = randomStillBirth(death, abortion);
            Long idNo = randomIdNo();
            String name = randomName();
            Long msisdn = randomMsisdn();
            DateTime birthDate = randomBirthDate();
            DateTime lmpdate = randomLmpDate();

            //StateID
            writer.write(stateId.toString());
            writer.write("\t");

            //District_ID
            writer.write(districtId.toString());
            writer.write("\t");

            //District_Name
            writer.write(districtName);
            writer.write("\t");

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
            writer.write("\t\t\t\t\t\t\t\t\t\t\t\t\t");

            //ID_No
            writer.write(idNo.toString());
            writer.write("\t");

            //Name
            writer.write(name);
            writer.write("\t");

            //Husband_Name
            //PhoneNo_Of_Whom
            writer.write("\t\t");

            //Whom_PhoneNo
            writer.write(msisdn.toString());
            writer.write("\t");

            //Birthdate
            writer.write(birthDate.toString("dd-MM-yyyy"));
            writer.write("\t");

            //JSY_Beneficiary
            //Caste
            //SubCentre_Name1
            //ANM_Name
            //ANM_Phone
            //ASHA_Name
            //ASHA_Phone
            //Delivery_Lnk_Facility
            //Facility_Name
            writer.write("\t\t\t\t\t\t\t\t\t");

            //LMP_Date
            writer.write(lmpdate.toString("dd-MM-yyyy"));
            writer.write("\t");

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
            writer.write("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

            //Abortion
            writer.write(abortion);
            writer.write("\t");

            //PNC_Home_Visit
            //PNC_Complication
            //PPC_Method
            //PNC_Checkup
            writer.write("\t\t\t\t");

            //Outcome_Nos
            writer.write(stillBirth);
            writer.write("\t");

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
            writer.write("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

            //Entry_Type
            writer.write(death);
            writer.newLine();

            if (i > 0 && i % PROGRESS_INTERVAL == 0) {
                LOGGER.debug("Created {}", timer.frequency(i));
            }
        }
        writer.close();

        LOGGER.debug("Created {}", timer.frequency(count));

        return String.format("%s\t%s", file.getAbsolutePath(), new DecimalFormat("#,##0").format(count));
    }
}

