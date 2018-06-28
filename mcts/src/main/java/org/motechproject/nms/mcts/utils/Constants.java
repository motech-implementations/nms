package org.motechproject.nms.mcts.utils;

public final class Constants {

    /**
     * Settings
     */
    public static final String MCTS_USER_ID = "mcts.credentials.user_id";
    public static final String MCTS_PASSWORD = "mcts.credentials.password";
    public static final String MCTS_LOCATIONS = "mcts.state_ids";
    public static final String MCTS_SYNC_CRON = "mcts.sync.cron";
    public static final String MCTS_ENDPOINT = "mcts.endpointUrl";
    public static final String DAYS_TO_PULL = "mcts.days_to_pull";
    public static final String HPD_STATES = "mcts.hpd.states";
    public static final String BASE_HPD_CONFIG = "mcts.hpd.state";
    public static final String MCTS_DID = "mcts.credentials.did";
    public static final String MCTS_PID = "mcts.credentials.pid";
    public static final String REMOTE_RESPONSE_DIR_CSV = "mcts.remote_response_dir_csv";

    /**
     * Events
     */
    public static final String BASE_MCTS_SUBJECT = "org.motechproject.nms.mcts";
    public static final String MCTS_IMPORT_EVENT = BASE_MCTS_SUBJECT + ".import";
    public static final String MCTS_CHILD_IMPORT_SUBJECT = BASE_MCTS_SUBJECT + ".child.import";
    public static final String MCTS_MOTHER_IMPORT_SUBJECT = BASE_MCTS_SUBJECT + ".mother.import";
    public static final String MCTS_ASHA_IMPORT_SUBJECT = BASE_MCTS_SUBJECT + ".asha.import";
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
    public static final String DEFAULT_MCTS_IMPORT_CRON_EXPRESSION = "0 0 16 * * ? *";

    private Constants() {
    }
}
