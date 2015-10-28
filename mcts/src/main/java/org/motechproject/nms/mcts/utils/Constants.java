package org.motechproject.nms.mcts.utils;

public final class Constants {

    /**
     * Settings
     */
    public static final String MCTS_USER_ID = "mcts.credentials.user_id";
    public static final String MCTS_PASSWORD = "mcts.credentials.password";
    public static final String MCTS_LOCATIONS = "mcts.state_ids";
    public static final String MCTS_SYNC_START_TIME = "mcts.sync.start_time";
    public static final String MCTS_ENDPOINT = "mcts.endpointUrl";
    public static final String MCTS_IMPORT_TMP_LOCATION= "mcts.import_tmp_location";
    /**
     * Events
     */
    public static final String BASE_MCTS_SUBJECT = "org.motechproject.mcts";
    public static final String MCTS_IMPORT_EVENT = BASE_MCTS_SUBJECT + ".Import";

    /**
     * Scheduler
     */
    public static final String DEFAULT_MCTS_IMPORT_CRON_EXPRESSION = "0 0 16 * * ? *";

    private Constants() {
    }
}