package org.motechproject.nms.testing.it.mcts.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public final class MctsImportTestHelper {

    public static String getMotherResponseData() throws IOException {
        return readMctsFile("mcts-mothers-data.xml");
    }

    public static String getChildrenResponseData() throws IOException {
        return readMctsFile("mcts-children-data.xml");
    }

    public static String getAnmAshaResponseData() throws IOException {
        return readMctsFile("mcts-anm-asha-data.xml");
    }

    public static String getEmptyAnmAshaResponseData() throws IOException {
        return readMctsFile("mcts-empty-anm-asha-data.xml");
    }

    private static String readMctsFile(String fileName) throws IOException {
        try (InputStream in = MctsImportTestHelper.class.getResourceAsStream("/mcts/" + fileName)) {
            return IOUtils.toString(in);
        }
    }

    private MctsImportTestHelper() {
    }
}
