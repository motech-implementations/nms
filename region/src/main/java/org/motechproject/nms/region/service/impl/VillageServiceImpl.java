package org.motechproject.nms.region.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.VillageService;
import org.motechproject.nms.region.utils.LocationConstants;
import org.motechproject.nms.rejectionhandler.service.VillageRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;
import java.util.List;
import java.util.Map;

@Service("villageService")
public class VillageServiceImpl implements VillageService {

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    private static final Logger LOGGER = LoggerFactory.getLogger(VillageServiceImpl.class);

    @Autowired
    private VillageDataService dataService;

    @Autowired
    private VillageRejectionService villageRejectionService;


    @Override
    public Village findByTalukaAndVcodeAndSvid(final Taluka taluka, final long vcode, final long svid) {
        if (taluka == null) { return null; }

        SqlQueryExecution<Village> queryExecution = new SqlQueryExecution<Village>() {

            @Override
            public String getSqlQuery() {
                return "select * from nms_villages where taluka_id_oid = ? and vcode = ? and svid = ?";
            }

            @Override
            public Village execute(Query query) {
                query.setClass(Village.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(taluka.getId(), vcode, svid);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (Village) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return dataService.executeSQLQuery(queryExecution);
    }

    @Override
    public Village create(Village village) {
        return dataService.create(village);
    }

    @Override
    public Village update(Village village) {
        return dataService.update(village);
    }

    @Override
    @Transactional
    public Long createUpdateVillages(final List<Map<String, Object>> villages, final Map<String, State> stateHashMap, final Map<String, District> districtHashMap, final Map<String, Taluka> talukaHashMap) {
        Timer queryTimer = new Timer();
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String villageValues = villageQuerySet(villages, stateHashMap, districtHashMap, talukaHashMap);
                String query = "";
                if (!villageValues.isEmpty()) {
                    query = "INSERT into nms_villages (`vcode`, `svid`, `name`, `state_id_OID`, `district_id_OID`,`taluka_id_OID`, " +
                            " `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                            villageValues +
                            " ON DUPLICATE KEY UPDATE " +
                            "name = VALUES(name), district_id_OID = VALUES(district_id_OID), taluka_id_OID = VALUES(taluka_id_OID), modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                }
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }



            @Override
            public Long execute(Query query) {
                return (Long) query.execute();
            }
        };

        Long createdVillages = 0L;
        if (!talukaHashMap.isEmpty() && !queryExecution.getSqlQuery().isEmpty()) {
            createdVillages = dataService.executeSQLQuery(queryExecution);
        }
        LOGGER.debug("VILLAGE Query time: {}", queryTimer.time());

        return createdVillages;
    }

    private String villageQuerySet(List<Map<String, Object>> villages, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap) { //NO CHECKSTYLE Cyclomatic Complexity
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (Map<String, Object> village : villages) {
            if (village.get(LocationConstants.CSV_STATE_ID) != null && village.get(LocationConstants.DISTRICT_ID) != null &&
                    village.get(LocationConstants.TALUKA_ID) != null) {
                State state = stateHashMap.get(village.get(LocationConstants.CSV_STATE_ID).toString());
                District district = districtHashMap.get(village.get(LocationConstants.CSV_STATE_ID).toString() + "_" + village.get(LocationConstants.DISTRICT_ID).toString());
                Taluka taluka = talukaHashMap.get(village.get(LocationConstants.CSV_STATE_ID).toString() + "_" +
                        village.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        village.get(LocationConstants.TALUKA_ID).toString().trim());
                Long villageCode = (Long) village.get(LocationConstants.VILLAGE_ID);
                String villageName = (String) village.get(LocationConstants.VILLAGE_NAME);
                if (taluka != null && taluka.getId() != null && (villageName != null && !villageName.trim().isEmpty()) && villageCode != null && !((Long) (0L)).equals(villageCode)) {
                    if (i != 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append("(");
                    stringBuilder.append(villageCode + ", ");
                    stringBuilder.append(0 + ", ");
                    stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(village.get(LocationConstants.VILLAGE_NAME) == null ?
                            "" : village.get(LocationConstants.VILLAGE_NAME).toString().replaceAll(":", "")) + QUOTATION_COMMA);
                    stringBuilder.append(state.getId() + ", ");
                    stringBuilder.append(district.getId() + ", ");
                    stringBuilder.append(taluka.getId() + ", ");
                    stringBuilder.append(MOTECH_STRING);
                    stringBuilder.append(MOTECH_STRING);
                    stringBuilder.append(MOTECH_STRING);
                    stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION_COMMA);
                    stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
                    stringBuilder.append(")");

                    i++;
                }
            }
            else if(village.get(LocationConstants.CSV_STATE_ID) == null || village.get(LocationConstants.DISTRICT_ID) == null || village.get(LocationConstants.TALUKA_ID) != null){

            }
        }

        return stringBuilder.toString();
    }
}
