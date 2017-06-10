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
    public static final String RCH_SYNC_CRON = "rch.sync.cron";
    public static final String RCH_ENDPOINT = "rch.endpointUrl";
    public static final String DAYS_TO_PULL = "rch.days_to_pull";
    public static final String HPD_STATES = "rch.hpd.states";
    public static final String BASE_HPD_CONFIG = "rch.hpd.state";
    public static final String RCH_MOTHER_USER = "rch.mother";
    public static final String RCH_CHILD_USER = "rch.child";
    public static final String RCH_ASHA_USER = "rch.asha";

    /**
     * Events
     */
    public static final String BASE_RCH_SUBJECT = "org.motechproject.nms.rch";
    public static final String RCH_IMPORT_EVENT = BASE_RCH_SUBJECT + ".import";
    public static final String RCH_CHILD_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".child.import";
    public static final String RCH_MOTHER_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".mother.import";
    public static final String RCH_ASHA_IMPORT_SUBJECT = BASE_RCH_SUBJECT + ".asha.import";
    public static final String STATE_ID_PARAM = "stateId";
    public static final String ENDPOINT_PARAM = "endpoint";
    public static final String STATE_NAME_PARAM = "stateName";
    public static final String STATE_CODE_PARAM = "stateCode";
    public static final String START_DATE_PARAM = "start_date";
    public static final String END_DATE_PARAM = "end_date";
    public static final String STATE_PARAM = "state";

    /**
     * Scheduler
     */
    public static final String DEFAULT_RCH_IMPORT_CRON_EXPRESSION = "0 0 20 * * ? *";

    private Constants() {
    }
}
