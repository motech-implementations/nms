package org.motechproject.nms.region.utils;

/**
 * Created by beehyv on 22/6/18.
 */
public final class LocationConstants {

    public static final String INVALID = "<%s - %s : Invalid location>";
    public static final String STATE_ID = "StateID";
    public static final String DISTRICT_ID = "District_ID";
    public static final String DISTRICT_NAME = "District_Name";
    public static final String TALUKA_ID = "Taluka_ID";
    public static final String TALUKA_NAME = "Taluka_Name";
    public static final String HEALTHBLOCK_ID = "HealthBlock_ID";
    public static final String HEALTHBLOCK_NAME = "HealthBlock_Name";
    public static final String PHC_ID = "PHC_ID";
    public static final String PHC_NAME = "PHC_Name";
    public static final String SUBCENTRE_ID = "SubCentre_ID";
    public static final String SUBCENTRE_NAME = "SubCentre_Name";
    public static final String VILLAGE_ID = "Village_ID";
    public static final String VILLAGE_NAME = "Village_Name";
    public static final String NON_CENSUS_VILLAGE = "SVID";

    public static final String CSV_STATE_ID = "State_ID";
    public static final String HEALTHFACILITY_ID = "HealthFacility_ID";
    public static final String HEALTHFACILITY_NAME = "HealthFacility_Name";
    public static final String HEALTHSUBFACILITY_ID = "HealthSubFacility_ID";
    public static final String HEALTHSUBFACILITY_NAME = "HealthSubFacility_Name";



    public static final String OR_SQL_STRING = " OR ";
    public static final String CODE_SQL_STRING = " (code = ";
    public static final String OPEN_PARANTHESES_STRING = " ( ";
    public static final String COMMA_QUOTATION_STRING = ", '";
    public static final String QUOTATION_COMMA_STRING = "', ";
    public static final String COMMA_STRING = " , ";

    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    public static final Long LOCATION_PART_SIZE = 5000L;

    private LocationConstants() {
    }
}
