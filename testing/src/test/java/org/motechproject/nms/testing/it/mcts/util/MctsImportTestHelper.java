package org.motechproject.nms.testing.it.mcts.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public final class MctsImportTestHelper {

    public static String getMotherResponseData() throws IOException {
        return readMctsFile("mcts-mothers-data.xml");
    }

    public static String getMotherResponseDataFail() throws IOException {
        return readMctsFile("mcts-mothers-data-fail.xml");
    }

    public static String getChildrenResponseData() throws IOException {
        return readMctsFile("mcts-children-data.xml");
    }

    public static String getChildrenResponseDataFail() throws IOException {
        return readMctsFile("mcts-children-data-fail.xml");
    }

    public static String getAnmAshaResponseData() throws IOException {
        return readMctsFile("mcts-anm-asha-data.xml");
    }

    public static String getAnmAshaResponseDataFail() throws IOException {
        return readMctsFile("mcts-anm-asha-data-fail.xml");
    }

    public static String getEmptyAnmAshaResponseData() throws IOException {
        return readMctsFile("mcts-empty-anm-asha-data.xml");
    }

    private static String readMctsFile(String fileName) throws IOException {
        try (InputStream in = MctsImportTestHelper.class.getResourceAsStream("/mcts/" + fileName)) {
            return IOUtils.toString(in);
        }
    }
    public static String getMotherResponseDataForNoUpdateDate() throws IOException {
        return readMctsFile("mcts-mothers-data-no-update-date.xml");
    }
    public static String getChildrenResponseDataForNoUpdateDate() throws IOException {
        return readMctsFile("mcts-children-data-no-update-date.xml");
    }
    public static String getAnmAshaResponseDataForNoUpdateDate() throws IOException {
        return readMctsFile("mcts-anm-asha-data-no-update-date.xml");
    }

    public static String getMotherResponseDataForOneUpdateDate() throws IOException {
        return readMctsFile("mcts-mothers-data-one-update-date.xml");
    }
    public static String getChildrenResponseDataForOneUpdateDate() throws IOException {
        return readMctsFile("mcts-children-data-one-update-date.xml");
    }
    public static String getAnmAshaResponseDataForOneUpdateDate() throws IOException {
        return readMctsFile("mcts-anm-asha-data-one-update-date.xml");
    }

    private MctsImportTestHelper() {
    }
}
