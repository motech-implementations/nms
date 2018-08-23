package org.motechproject.nms.region.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.utils.LocationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("healthBlockService")
public class HealthBlockServiceImpl implements HealthBlockService {

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthBlockServiceImpl.class);


    @Autowired
    private HealthBlockDataService healthBlockDataService;

    @Override
    // Since Taluka <-> HealthBlocks are many to many, but we don't model that in our system
    // We instead just want to find a taluka with a matching code in the same state as the
    // provided taluka
    public HealthBlock findByTalukaAndCode(final Taluka taluka, final Long code) {
        if (taluka == null) { return null; }

        SqlQueryExecution<HealthBlock> queryExecution = new SqlQueryExecution<HealthBlock>() {

            @Override
            public String getSqlQuery() {
                return "select * " +
                         "from nms_health_blocks " +
                         "join nms_taluka_healthblock j on j.healthblock_id = nms_health_blocks.id " +
                         "join nms_talukas t on j.taluka_id = t.id " +
                         "join nms_districts d on t.district_id_oid = d.id " +
                         "join nms_states s on d.state_id_oid = s.id " +
                         "join nms_states s2 on s.id = s2.id " +
                         "join nms_districts d2 on d2.state_id_oid = s2.id " +
                         "join nms_talukas t2 on t2.district_id_oid = d2.id " +
                        "where nms_health_blocks.code = ? and t2.id = ?";
            }

            @Override
            public HealthBlock execute(Query query) {
                query.setClass(HealthBlock.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(code, taluka.getId());
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (HealthBlock) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return healthBlockDataService.executeSQLQuery(queryExecution);
    }

    @Override
    //Using this because TalukaId, healthblockCode combination might return more than 1 healthblocks
    public HealthBlock findByDistrictAndCode(final District district, final Long code) {
        if (district == null) { return null; }

        SqlQueryExecution<HealthBlock> queryExecution = new SqlQueryExecution<HealthBlock>() {

            @Override
            public String getSqlQuery() {
                return "select * " +
                        "from nms_health_blocks " +
                        "where code = ? and district_id_OID = ?";
            }

            @Override
            public HealthBlock execute(Query query) {
                query.setClass(HealthBlock.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(code, district.getId());
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (HealthBlock) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return healthBlockDataService.executeSQLQuery(queryExecution);
    }

    @Override
    public HealthBlock create(HealthBlock healthBlock) {
        return healthBlockDataService.create(healthBlock);
    }

    @Override
    public HealthBlock update(HealthBlock healthBlock) {
        return healthBlockDataService.update(healthBlock);
    }

    @Override
    @Transactional
    public Long createUpdateHealthBlocks(final List<Map<String, Object>> healthBlocks, final Map<String, State> stateHashMap, final Map<String, District> districtHashMap, final Map<String, Taluka> talukaHashMap) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String healthBlockValues = healthBlockQuerySet(healthBlocks, stateHashMap, districtHashMap, talukaHashMap);
                String query = "";
                if(!healthBlockValues.isEmpty()) {
                    query = "INSERT into nms_health_blocks (`code`, `name`, `state_id_OID`, `district_id_OID`, `taluka_id_OID`, " +
                            " `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                            healthBlockValues + " ON DUPLICATE KEY UPDATE " +
                            "name = VALUES(name), modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                }
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }



            @Override
            public Long execute(Query query) {
                query.setClass(HealthBlock.class);
                return (Long) query.execute();
            }
        };

        Long createdHealthBlocks = 0L;
        if (!districtHashMap.isEmpty() && !talukaHashMap.isEmpty() && !queryExecution.getSqlQuery().isEmpty()) {
            createdHealthBlocks = healthBlockDataService.executeSQLQuery(queryExecution);
        }

        return createdHealthBlocks;
    }

    @Override
    public Map<String, HealthBlock> fillHealthBlockIds(List<Map<String, Object>> recordList, final Map<String, District> districtHashMap) {
        final Set<String> healthBlockKeys = new HashSet<>();
        for(Map<String, Object> record : recordList) {
            if (record.get(LocationConstants.CSV_STATE_ID) != null && record.get(LocationConstants.DISTRICT_ID) != null
                    && record.get(LocationConstants.HEALTHBLOCK_ID) != null) {
                healthBlockKeys.add(record.get(LocationConstants.CSV_STATE_ID).toString() + "_" + record.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        record.get(LocationConstants.HEALTHBLOCK_ID).toString());
            }
        }
        Map<String, HealthBlock> healthBlockHashMap = new HashMap<>();

        Map<Long, String> districtIdMap = new HashMap<>();
        for (String districtKey : districtHashMap.keySet()) {
            districtIdMap.put(districtHashMap.get(districtKey).getId(), districtKey);
        }

        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<HealthBlock>> queryExecution = new SqlQueryExecution<List<HealthBlock>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_health_blocks where";
                int count = healthBlockKeys.size();
                for (String healthBlockString : healthBlockKeys) {
                    String[] ids = healthBlockString.split("_");
                    District district = districtHashMap.get(ids[0] + "_" + ids[1]);
                    if (district != null && district.getId() != null) {
                        if (count != healthBlockKeys.size()) {
                            query += LocationConstants.OR_SQL_STRING;
                        }
                        query += "(code = " + ids[2] + " and district_id_OID = " + district.getId() + ")";
                        count--;
                    }
                }

                LOGGER.debug("HEALTHBLOCK Query: {}", query);
                return query;
            }

            @Override
            public List<HealthBlock> execute(Query query) {
                query.setClass(HealthBlock.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<HealthBlock> healthBlocks;
                if (fqr.isEmpty()) {
                    return null;
                }
                healthBlocks = (List<HealthBlock>) fqr;
                return healthBlocks;
            }
        };

        List<HealthBlock> healthBlocks = null;
        if (!districtHashMap.isEmpty() && !healthBlockKeys.isEmpty()) {
            healthBlocks = healthBlockDataService.executeSQLQuery(queryExecution);
        }
        LOGGER.debug("HEALTHBLOCK Query time: {}", queryTimer.time());
        if(healthBlocks != null && !healthBlocks.isEmpty()) {
            for (HealthBlock healthBlock : healthBlocks) {
                    String districtKey = districtIdMap.get(healthBlock.getDistrict().getId());
                    healthBlockHashMap.put(districtKey + "_" + healthBlock.getCode(), healthBlock);
            }
        }
        return healthBlockHashMap;
    }

    private String healthBlockQuerySet(List<Map<String, Object>> healthBlocks, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap) { //NO CHECKSTYLE Cyclomatic Complexity
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (Map<String, Object> healthBlock : healthBlocks) {
            if (healthBlock.get(LocationConstants.CSV_STATE_ID) != null && healthBlock.get(LocationConstants.DISTRICT_ID) != null &&
                    healthBlock.get(LocationConstants.TALUKA_ID) != null) {
                State state = stateHashMap.get(healthBlock.get(LocationConstants.CSV_STATE_ID).toString());
                District district = districtHashMap.get(healthBlock.get(LocationConstants.CSV_STATE_ID).toString() + "_" + healthBlock.get(LocationConstants.DISTRICT_ID).toString());
                Taluka taluka = talukaHashMap.get(healthBlock.get(LocationConstants.CSV_STATE_ID).toString() + "_" + healthBlock.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        healthBlock.get(LocationConstants.TALUKA_ID).toString().trim());
                Long healthBlockCode = (Long) healthBlock.get(LocationConstants.HEALTHBLOCK_ID);
                if (district != null && taluka != null && healthBlockCode != null && !((Long) (0L)).equals(healthBlockCode)) {
                    if (i != 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append("(");
                    stringBuilder.append(healthBlockCode + ", ");
                    stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(healthBlock.get(LocationConstants.HEALTHBLOCK_NAME) == null ?
                            "" : healthBlock.get(LocationConstants.HEALTHBLOCK_NAME).toString()) + QUOTATION_COMMA);
                    stringBuilder.append(state.getId()+ ", ");
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
        }

        return stringBuilder.toString();
    }

    @Override
    @Transactional
    public Long createUpdateTalukaHealthBlock(final List<Map<String, Object>> recordList) {
        Timer queryTimer = new Timer();
        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query1 = "INSERT IGNORE into nms_taluka_healthblock (taluka_id, healthBlock_id, creationDate, modificationDate) ";
                int count = recordList.size();
                String query2 = "";
                for (Map<String, Object> record : recordList) {
                    if (record.get(LocationConstants.TALUKA_ID) != null && record.get(LocationConstants.CSV_STATE_ID) != null
                            && record.get(LocationConstants.HEALTHBLOCK_ID) != null) {
                        if (count != recordList.size()) {
                            query2 += " UNION ";
                        }
                        query2 += " select t.id, h.id, now(), now() from nms_states s " +
                                " join nms_districts d on  d.state_id_OID = s.id " +
                                " join nms_talukas t on t.district_id_OID = d.id and t.code = " +
                                record.get(LocationConstants.TALUKA_ID).toString().trim() +
                                " join nms_health_blocks h on h.district_id_OID = t.district_id_OID and h.code = " +
                                record.get(LocationConstants.HEALTHBLOCK_ID).toString() +
                                " where s.code = " + record.get(LocationConstants.CSV_STATE_ID).toString();
                        count--;
                    }
                }

                if (query2.isEmpty()) {
                    LOGGER.debug("Taluka_HEALTHBLOCK Query: {}", query2);
                    return query2;
                }

                LOGGER.debug("Taluka_HEALTHBLOCK Query: {}", query1 + query2);
                return query1 + query2;
            }

            @Override
            public Long execute(Query query) {
                return (Long) query.execute();
            }
        };

        Long healthBlockTalukaCount = 0L;
        if (!queryExecution.getSqlQuery().isEmpty()) {
            healthBlockTalukaCount = healthBlockDataService.executeSQLQuery(queryExecution);
        }
        LOGGER.debug("Taluka_HEALTHBLOCKs inserted : {}", healthBlockTalukaCount);
        LOGGER.debug("Taluka_HEALTHBLOCKs INSERT Query time: {}", queryTimer.time());
        return healthBlockTalukaCount;
    }
}
