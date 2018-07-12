package org.motechproject.nms.region.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.VillageService;
import org.motechproject.nms.region.utils.LocationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public Long createUpdateVillages(final List<Map<String, Object>> villages, final Map<String, Taluka> talukaHashMap) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT into nms_villages (`vcode`, `svid`, `name`, `taluka_id_OID`, " +
                        " `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                        villageQuerySet(villages, talukaHashMap) +
                        " ON DUPLICATE KEY UPDATE " +
                        "name = VALUES(name), modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }



            @Override
            public Long execute(Query query) {
                return (Long) query.execute();
            }
        };

        Long createdVillages = dataService.executeSQLQuery(queryExecution);


        return createdVillages;
    }

    @Override
    public Map<String, Village> fillVillageIds(List<Map<String, Object>> villages, final Map<String, Taluka> talukaHashMap) {
        final Set<String> villageKeys = new HashSet<>();
        for(Map<String, Object> village : villages) {
            villageKeys.add(village.get(LocationConstants.CSV_STATE_ID).toString() + "_" + village.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                    village.get(LocationConstants.TALUKA_ID).toString() + "_" + village.get(LocationConstants.VILLAGE_ID).toString() + "_" + 0);
        }
        Map<String, Village> villageHashMap = new HashMap<>();
        Map<Long, String> talukaIdMap = new HashMap<>();
        for (String districtKey : talukaHashMap.keySet()) {
            talukaIdMap.put(talukaHashMap.get(districtKey).getId(), districtKey);
        }
        Timer queryTimer = new Timer();
        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<Village>> queryExecution = new SqlQueryExecution<List<Village>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_villages where";
                int count = villageKeys.size();
                for (String villageString : villageKeys) {
                    count--;
                    String[] ids = villageString.split("_");
                    Taluka taluka = talukaHashMap.get(ids[0] + "_" + ids[1] + "_" + ids[2]);
                    if (taluka != null && taluka.getId() != null) {
                        query += " (vcode = " + ids[3] + " and svid = " + ids[4] + " and taluka_id_oid = " + taluka.getId() + ")";
                        if (count > 0) {
                            query += LocationConstants.OR_SQL_STRING;
                        }
                    }
                }

                LOGGER.debug("VILLAGE Query: {}", query);
                return query;
            }

            @Override
            public List<Village> execute(Query query) {
                query.setClass(Village.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<Village> villages;
                if (fqr.isEmpty()) {
                    return null;
                }
                villages = (List<Village>) fqr;
                return villages;
            }
        };

        List<Village> dbVillages = dataService.executeSQLQuery(queryExecution);
        LOGGER.debug("VILLAGE Query time: {}", queryTimer.time());
        if(dbVillages != null && !dbVillages.isEmpty()) {
            for (Village village : dbVillages) {
                String talukaKey = talukaIdMap.get(village.getTaluka().getId());
                villageHashMap.put(talukaKey + "_" + village.getVcode() + "_" + village.getSvid(), village);
            }
        }
        return villageHashMap;
    }

    private String villageQuerySet(List<Map<String, Object>> villages, Map<String, Taluka> talukaHashMap) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (Map<String, Object> village : villages) {
            Taluka taluka = talukaHashMap.get(village.get(LocationConstants.CSV_STATE_ID).toString() + "_" +
                    village.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                    village.get(LocationConstants.TALUKA_ID).toString());
            if (taluka != null && taluka.getId() != null) {
                if (i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append("(");
                stringBuilder.append(village.get(LocationConstants.VILLAGE_ID) + ", ");
                stringBuilder.append(0 + ", ");
                stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(village.get(LocationConstants.VILLAGE_NAME).toString().replaceAll(":", "")) + QUOTATION_COMMA);
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

        return stringBuilder.toString();
    }
}
