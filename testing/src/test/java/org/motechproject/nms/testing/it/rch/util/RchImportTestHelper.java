package org.motechproject.nms.testing.it.rch.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public final class RchImportTestHelper {

    public static String getRchMothersResponseData() throws IOException {
        return readRchFile("rch-mothers-data.xml");
    }

    public static String getRchMotherActiveResponseData() throws IOException {
        return readRchFile("motherActiveImport.xml");
    }

    public static String getRchMothersResponseDataFail() throws IOException {
        return readRchFile("rch-mothers-data-fail.xml");
    }

    public static String getRchMothersResponseDataForNoUpdateDate() throws IOException {
        return readRchFile("rch-mothers-data-no-exec-date.xml");
    }

    public static String getRchMothersResponseDataForOneUpdateDate() throws IOException {
        return readRchFile("rch-mothers-data-one-exec-date.xml");
    }

    public static String getRchChildrenResponseData() throws IOException {
        return readRchFile("rch-children-data.xml");
    }

    public static String getRchChildrenResponseDataForZeroMother() throws IOException {
        return readRchFile("rch-children-data-zero-mother.xml");
    }

    public static String getRchChildrenResponseDataFail() throws IOException {
        return readRchFile("rch-children-data-fail.xml");
    }

    public static String getRchChildrenResponseDataForNoUpdateDate() throws IOException {
        return readRchFile("rch-children-data-no-exec-date.xml");
    }

    public static String getRchChildrenResponseDataForOneUpdateDate() throws IOException {
        return readRchFile("rch-children-data-one-exec-date.xml");
    }

    public static String getAnmAshaResponseData() throws IOException {
        return readRchFile("rch-anm-asha-data.xml");
    }

    public static String getAnmAshaResponseDataFail() throws IOException {
        return readRchFile("rch-anm-asha-data-fail.xml");
    }

    public static String getAnmAshaResponseDataForNoUpdateDate() throws IOException {
        return readRchFile("rch-anm-asha-data-no-exec-date.xml");
    }

    public static String getAnmAshaResponseDataForOneUpdateDate() throws IOException {
        return readRchFile("rch-anm-asha-data-one-exec-date.xml");
    }
    public static String getVillageLocationdataResponse() throws IOException {
        return readRchFile("RCH_Village_import.xml");
    }
    public static String getDistrictLocationdataResponse() throws IOException {
        return readRchFile("RCH_District_import.xml");
    }
    public static String getTalukaLocationdataResponse() throws IOException {
        return readRchFile("RCH_Taluka_import.xml");
    }
    public static String getHealthBlockLocationdataResponse() throws IOException {
        return readRchFile("RCH_HealthBlock_import.xml");
    }
    public static String getHealthFacilityLocationdataResponse() throws IOException {
        return readRchFile("RCH_HealthFacility_import.xml");
    }
    public static String getHealthSubFacilityLocationdataResponse() throws IOException {
        return readRchFile("RCH_HealthSubFacility_import.xml");
    }


    private static String readRchFile(String fileName) throws IOException {
        try (InputStream in = RchImportTestHelper.class.getResourceAsStream("/rch/" + fileName)) {
            return IOUtils.toString(in);
        }
    }

    private RchImportTestHelper() {

    }
}
