package org.motechproject.nms.rch.utils;

public final class Constants {

    /**
     * Settings
     */
    public static final String RCH_USER_ID = "rch.credentials.user_id";
    public static final String RCH_PASSWORD = "rch.credentials.password";
    public static final String RCH_PROJECT_ID = "rch.credentials.project_id";
    public static final String RCH_DTID = "rch.credentials.dtid";
    public static final String RCH_LOCATIONS = "rch.state_ids";
    //public static final String RCH_SYNC_CRON = "rch.sync.cron";

    public static final String RCH_SYNC_MOTHER_CRON = "rch.sync.cron.mother";
    public static final String RCH_SYNC_CHILD_CRON = "rch.sync.cron.child";
    public static final String RCH_SYNC_DISTRICT_CRON = "rch.sync.cron.district";
    public static final String RCH_SYNC_TALUKA_CRON = "rch.sync.cron.taluka";
    public static final String RCH_SYNC_HEALTHBLOCK_CRON = "rch.sync.cron.healthblock";
    public static final String RCH_SYNC_VILLAGE_CRON = "rch.sync.cron.village";
    public static final String RCH_SYNC_HEALTHFACILITY_CRON = "rch.sync.cron.healthfacility";
    public static final String RCH_SYNC_HEALTHSUBFACILITY_CRON = "rch.sync.cron.healthsubfacility";
    public static final String RCH_SYNC_TALUKA_HEALTHBLOCK_CRON = "rch.sync.cron.talukahealthblock";
    public static final String RCH_SYNC_VILLAGE_HEALTHFACILITYCRON = "rch.sync.cron.villagesubfacility";
    public static final String RCH_SYNC_ASHA_CRON = "rch.sync.cron.asha";





    public static final String RCH_MOTHER_READ_CRON = "rch.mother.sync.cron";
    public static final String RCH_CHILD_READ_CRON = "rch.child.sync.cron";
    public static final String RCH_ASHA_READ_CRON = "rch.asha.sync.cron";
    public static final String RCH_LOCATION_READ_CRON = "rch.location.sync.cron";
    public static final String RCH_ENDPOINT = "rch.endpointUrl";
    public static final String DAYS_TO_PULL = "rch.days_to_pull";
    public static final String HPD_STATES = "rch.hpd.states";
    public static final String BASE_HPD_CONFIG = "rch.hpd.state";
    public static final String RCH_MOTHER_USER = "rch.mother";
    public static final String RCH_CHILD_USER = "rch.child";
    public static final String RCH_ASHA_USER = "rch.asha";
    public static final String RCH_LOCATION_TALUKA = "rch.taluka";
    public static final String RCH_LOCATION_HEALTHBLOCK = "rch.healthBlock";
    public static final String RCH_LOCATION_TALUKA_HEALTHBLOCK = "rch.talukaHealthBlock";
    public static final String RCH_LOCATION_HEALTHFACILITY = "rch.healthFacility";
    public static final String RCH_LOCATION_HEALTHSUBFACILITY = "rch.healthSubFacility";
    public static final String RCH_LOCATION_VILLAGE_HEALTHSUBFACILITY = "rch.villageHealthSubFacility";
    public static final String RCH_LOCATION_VILLAGE = "rch.village";
    public static final String RCH_LOCATION_DISTRICT = "rch.district";


    /**
     * Events
     */
    public static final String BASE_RCH_SUBJECT = "org.motechproject.nms.rch";
    public static final String RCH_IMPORT_EVENT = BASE_RCH_SUBJECT + ".import";

    /**
     * Split RCH_IMPORT_EVENT into 11 separate - so that each could be associated with its cron
     */
    public static final String RCH_IMPORT_EVENT_CRON = RCH_IMPORT_EVENT +".cron";
    public static final String RCH_CHILD_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".child";
    public static final String RCH_MOTHER_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".mother";
    public static final String RCH_ASHA_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".asha";
    public static final String RCH_TALUKA_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".taluka";
    public static final String RCH_VILLAGE_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".village";
    public static final String RCH_DISTRICT_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".district";
    public static final String RCH_HEALTHBLOCK_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".healthBlock";
    public static final String RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".talukaHealthBlock";
    public static final String RCH_HEALTHFACILITY_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".healthFacility";
    public static final String RCH_HEALTHSUBFACILITY_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".healthSubFacility";
    public static final String RCH_VILLAGEHEALTHSUBFACILITY_IMPORT_SUBJECT_CRON = RCH_IMPORT_EVENT_CRON + ".villageHealthSubFacility";



    public static final String RCH_CHILD_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".child.import";
    public static final String RCH_MOTHER_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".mother.import";
    public static final String RCH_ASHA_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".asha.import";
    public static final String RCH_TALUKA_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".taluka.import";
    public static final String RCH_VILLAGE_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".village.import";
    public static final String RCH_DISTRICT_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".district.import";
    public static final String RCH_HEALTHBLOCK_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".healthBlock.import";
    public static final String RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".talukaHealthBlock.import";
    public static final String RCH_HEALTHFACILITY_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".healthFacility.import";
    public static final String RCH_HEALTHSUBFACILITY_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".healthSubFacility.import";
    public static final String RCH_VILLAGEHEALTHSUBFACILITY_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".villageHealthSubFacility.import";

    public static final String RCH_CHILD_READ_SUBJECT = BASE_RCH_SUBJECT + ".child.read";
    public static final String RCH_CHILD_READ = "child.read";
    public static final String RCH_MOTHER_READ_SUBJECT = BASE_RCH_SUBJECT + ".mother.read";
    public static final String RCH_MOTHER_READ = "mother.read";
    public static final String RCH_ASHA_READ_SUBJECT = BASE_RCH_SUBJECT + ".asha.read";
    public static final String RCH_ASHA_READ = "asha.read";
    public static final String RCH_LOCATION_READ_SUBJECT = BASE_RCH_SUBJECT + ".location.read";
    public static final String RCH_DISTRICT_READ_SUBJECT = BASE_RCH_SUBJECT + ".district.read";
    public static final String RCH_TALUKA_READ_SUBJECT = BASE_RCH_SUBJECT + ".taluka.read";
    public static final String RCH_HEALTHBLOCK_READ_SUBJECT = BASE_RCH_SUBJECT + ".healthblock.read";
    public static final String RCH_TALUKA_HEALTHBLOCK_READ_SUBJECT = BASE_RCH_SUBJECT + ".talukahealthblock.read";
    public static final String RCH_HEALTHFACILITY_READ_SUBJECT = BASE_RCH_SUBJECT + ".healthfacility.read";
    public static final String RCH_HEALTHSUBFACILITY_READ_SUBJECT = BASE_RCH_SUBJECT + ".healthsubfacility.read";
    public static final String RCH_VILLAGE_READ_SUBJECT = BASE_RCH_SUBJECT + ".village.read";
    public static final String RCH_VILLAGE_HEALTHSUBFACILITY_READ_SUBJECT = BASE_RCH_SUBJECT + ".villagehealthsubfacility.read";

    public static final String STATE_ID_PARAM = "stateId";
    public static final String ENDPOINT_PARAM = "endpoint";
    public static final String STATE_NAME_PARAM = "stateName";
    public static final String STATE_CODE_PARAM = "stateCode";
    public static final String START_DATE_PARAM = "start_date";
    public static final String END_DATE_PARAM = "end_date";
    public static final String STATE_PARAM = "state";


    private Constants() {
    }
}
