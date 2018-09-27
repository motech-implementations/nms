package org.motechproject.nms.region.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.service.HealthSubFacilityService;
import org.motechproject.nms.region.utils.LocationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;
import java.util.List;
import java.util.Map;


@Service("healthSubFacilityService")
public class HealthSubFacilityServiceImpl implements HealthSubFacilityService {

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthSubFacilityServiceImpl.class);


    @Autowired
    private HealthSubFacilityDataService dataService;

    @Override
    public HealthSubFacility findByHealthFacilityAndCode(final HealthFacility healthFacility, final Long code) {
        if (healthFacility == null) { return null; }

        SqlQueryExecution<HealthSubFacility> queryExecution = new SqlQueryExecution<HealthSubFacility>() {

            @Override
            public String getSqlQuery() {
                return "select *  from nms_health_sub_facilities where healthFacility_id_oid = ? and code = ?";
            }

            @Override
            public HealthSubFacility execute(Query query) {
                query.setClass(HealthSubFacility.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(healthFacility.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (HealthSubFacility) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return dataService.executeSQLQuery(queryExecution);
    }

    @Override
    public HealthSubFacility create(HealthSubFacility healthSubFacility) {
        return dataService.create(healthSubFacility);
    }

    @Override
    public HealthSubFacility update(HealthSubFacility healthSubFacility) {
        return dataService.update(healthSubFacility);
    }

    @Override
    @Transactional
    public Long createUpdateHealthSubFacilities(final List<Map<String, Object>> healthSubFacilities, final Map<String, State> stateHashMap, final Map<String, District> districtHashMap, final Map<String, Taluka> talukaHashMap, final Map<String, HealthFacility> healthFacilityHashMap) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String healthSubFacilityValues = healthSubFacilityQuerySet(healthSubFacilities, stateHashMap, districtHashMap, talukaHashMap, healthFacilityHashMap);
                String query = "";
                if (!healthSubFacilityValues.isEmpty()) {
                    query = "INSERT into nms_health_sub_facilities (`code`, `name`, `state_id_OID`, `district_id_OID`, `healthFacility_id_OID`, `taluka_id_oid`, " +
                            " `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                            healthSubFacilityValues +
                            " ON DUPLICATE KEY UPDATE " +
                            "name = VALUES(name), modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                }
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }



            @Override
            public Long execute(Query query) {
                query.setClass(HealthSubFacility.class);
                return (Long) query.execute();
            }
        };

        Long createdHealthSubFacilities = 0L;
        if (!healthFacilityHashMap.isEmpty() && !talukaHashMap.isEmpty() && !queryExecution.getSqlQuery().isEmpty()) {
            createdHealthSubFacilities = dataService.executeSQLQuery(queryExecution);
        }

        return createdHealthSubFacilities;
    }

    private String healthSubFacilityQuerySet(List<Map<String, Object>> healthSubFacilities, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap, Map<String, HealthFacility> healthFacilityHashMap) { //NO CHECKSTYLE Cyclomatic Complexity
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (Map<String, Object> healthSubFacility : healthSubFacilities) {
            if (healthSubFacility.get(LocationConstants.CSV_STATE_ID) != null && healthSubFacility.get(LocationConstants.DISTRICT_ID) != null &&
                    healthSubFacility.get(LocationConstants.TALUKA_ID) != null && healthSubFacility.get(LocationConstants.HEALTHFACILITY_ID) != null) {
                State state = stateHashMap.get(healthSubFacility.get(LocationConstants.CSV_STATE_ID).toString());
                District district = districtHashMap.get(healthSubFacility.get(LocationConstants.CSV_STATE_ID).toString() + "_" + healthSubFacility.get(LocationConstants.DISTRICT_ID).toString());
                Taluka taluka = talukaHashMap.get(healthSubFacility.get(LocationConstants.CSV_STATE_ID).toString() + "_" +
                        healthSubFacility.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        healthSubFacility.get(LocationConstants.TALUKA_ID).toString().trim());
                HealthFacility healthFacility = healthFacilityHashMap.get(healthSubFacility.get(LocationConstants.CSV_STATE_ID).toString() + "_" +
                        healthSubFacility.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        healthSubFacility.get(LocationConstants.TALUKA_ID).toString().trim() + "_" +
                        healthSubFacility.get(LocationConstants.HEALTHFACILITY_ID).toString());
                Long healthSubFacilityCode = (Long) healthSubFacility.get(LocationConstants.HEALTHSUBFACILITY_ID);
                if (taluka != null && healthFacility != null && healthSubFacilityCode != null && !((Long) (0L)).equals(healthSubFacilityCode)) {
                    if (i != 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append("(");
                    stringBuilder.append(healthSubFacilityCode + ", ");
                    stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(healthSubFacility.get(LocationConstants.HEALTHSUBFACILITY_NAME) == null ?
                            "" : healthSubFacility.get(LocationConstants.HEALTHSUBFACILITY_NAME).toString()) + QUOTATION_COMMA);
                    stringBuilder.append(state.getId() + ", ");
                    stringBuilder.append(district.getId() + ", ");
                    stringBuilder.append(healthFacility.getId() + ", ");
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
    public Long createUpdateVillageHealthSubFacility(final List<Map<String, Object>> recordList) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT IGNORE into nms_village_healthsubfacility (village_id, healthSubFacility_id, creationDate, modificationDate) ";
                String query2 = "";
                int count = recordList.size();
                for (Map<String, Object> record : recordList) {
                    if (record.get(LocationConstants.CSV_STATE_ID) != null
                            && record.get(LocationConstants.HEALTHSUBFACILITY_ID) != null && record.get(LocationConstants.VILLAGE_ID) != null) {
                        if (count != recordList.size()) {
                            query2 += " UNION ";
                        }
                        query2 += " select v.id as village_Id, hsf.id as healthSubFacility_Id, now(), now() from nms_states s " +
                                " JOIN nms_districts d on s.id=d.state_id_oid " +
                                " JOIN nms_talukas t on t.district_id_oid = d.id " +
                                " JOIN nms_villages v on v.taluka_id_oid = t.id and v.vcode = " +
                                record.get(LocationConstants.VILLAGE_ID).toString() +
                                " and v.svid = 0 " +
                                " JOIN nms_health_sub_facilities hsf on hsf.taluka_id_oid = v.taluka_id_oid and hsf.code =  " +
                                record.get(LocationConstants.HEALTHSUBFACILITY_ID).toString() +
                                " where s.code = " + record.get(LocationConstants.CSV_STATE_ID).toString();
                        count--;
                    }
                }

                if (query2.isEmpty()) {
                    LOGGER.debug("VILLAGE_HEALTHSUBFACILITY Query: {}", query2);
                    return query2;
                }

                LOGGER.debug("VILLAGE_HEALTHSUBFACILITY Query: {}", query + query2);
                return query + query2;
            }

            @Override
            public Long execute(Query query) {
                return (Long) query.execute();
            }
        };

        Long villageHealthSubFacilityCount = 0L;
        if (!queryExecution.getSqlQuery().isEmpty()) {
            villageHealthSubFacilityCount = dataService.executeSQLQuery(queryExecution);
        }
        LOGGER.debug("VILLAGE_HEALTHSUBFACILITYs inserted : {}", villageHealthSubFacilityCount);
        LOGGER.debug("VILLAGE_HEALTHSUBFACILITYs INSERT Query time: {}", queryTimer.time());
        return villageHealthSubFacilityCount;
    }

}
