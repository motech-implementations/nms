package org.motechproject.nms.region.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.service.TalukaService;
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

@Service("talukaService")
public class TalukaServiceImpl implements TalukaService {

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    private static final Logger LOGGER = LoggerFactory.getLogger(TalukaServiceImpl.class);

    @Autowired
    private TalukaDataService dataService;

    @Override
    public Taluka findByDistrictAndCode(final District district, final String code) {
        if (district == null) { return null; }

        SqlQueryExecution<Taluka> queryExecution = new SqlQueryExecution<Taluka>() {

            @Override
            public String getSqlQuery() {
                return "select * from nms_talukas where district_id_oid = ? and code = ?";
            }

            @Override
            public Taluka execute(Query query) {
                query.setClass(Taluka.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(district.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (Taluka) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return dataService.executeSQLQuery(queryExecution);
    }

    @Override
    public Taluka create(Taluka taluka) {
        return dataService.create(taluka);
    }

    @Override
    public Taluka update(Taluka taluka) {
        return dataService.update(taluka);
    }

    @Override
    @Transactional
    public Long createUpdateTalukas(final List<Map<String, Object>> talukas, final Map<String, District> districtHashMap) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String talukaValues = talukaQuerySet(talukas, districtHashMap);
                String query = "";
                if (!talukaValues.isEmpty()) {
                    query = "INSERT into nms_talukas (`code`, `name`, `district_id_OID`, " +
                            " `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                            talukaValues +
                            " ON DUPLICATE KEY UPDATE " +
                            "name = VALUES(name), modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                }
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }



            @Override
            public Long execute(Query query) {
                query.setClass(Taluka.class);
                return (Long) query.execute();
            }
        };

        Long createdTalukas = 0L;
        if (!districtHashMap.isEmpty()) {
            createdTalukas = dataService.executeSQLQuery(queryExecution);
        }

        return createdTalukas;
    }

    @Override
    public Map<String, Taluka> fillTalukaIds(List<Map<String, Object>> recordList, final Map<String, District> districtHashMap) {
        final Set<String> talukaKeys = new HashSet<>();
        for(Map<String, Object> record : recordList) {
            talukaKeys.add(record.get(LocationConstants.CSV_STATE_ID).toString() + "_" + record.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                    record.get(LocationConstants.TALUKA_ID).toString());
        }
        Map<String, Taluka> talukaHashMap = new HashMap<>();


        Map<Long, String> districtIdMap = new HashMap<>();
        for (String districtKey : districtHashMap.keySet()) {
            districtIdMap.put(districtHashMap.get(districtKey).getId(), districtKey);
        }

        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<Taluka>> queryExecution = new SqlQueryExecution<List<Taluka>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_talukas where";
                int count = talukaKeys.size();
                for (String talukaString : talukaKeys) {
                    count--;
                    String[] ids = talukaString.split("_");
                    District district = districtHashMap.get(ids[0] + "_" + ids[1]);
                    if (district != null && district.getId() != null) {
                        query += LocationConstants.CODE_SQL_STRING + ids[2] + " and district_id_oid = " + district.getId() + ")";
                        if (count > 0) {
                            query += LocationConstants.OR_SQL_STRING;
                        }
                    }
                }

                LOGGER.debug("TALUKA Query: {}", query);
                return query;
            }

            @Override
            public List<Taluka> execute(Query query) {
                query.setClass(Taluka.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<Taluka> talukas;
                if (fqr.isEmpty()) {
                    return null;
                }
                talukas = (List<Taluka>) fqr;
                return talukas;
            }
        };

        List<Taluka> talukas = null;
        if (!districtHashMap.isEmpty()) {
            talukas = dataService.executeSQLQuery(queryExecution);
        }
        LOGGER.debug("TALUKA Query time: {}", queryTimer.time());
        if (talukas != null && !talukas.isEmpty()) {
            for (Taluka taluka : talukas) {
                String districtKey = districtIdMap.get(taluka.getDistrict().getId());
                talukaHashMap.put(districtKey + "_" + taluka.getCode(), taluka);
            }
        }

        return talukaHashMap;
    }

    private String talukaQuerySet(List<Map<String, Object>> talukas, Map<String, District> districtHashMap) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (Map<String, Object> taluka : talukas) {
            District district = districtHashMap.get(taluka.get(LocationConstants.CSV_STATE_ID).toString() + "_" + taluka.get(LocationConstants.DISTRICT_ID).toString());
            if (district != null) {
                if (i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append("(");
                stringBuilder.append(QUOTATION + taluka.get(LocationConstants.TALUKA_ID) + QUOTATION_COMMA);
                stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(taluka.get(LocationConstants.TALUKA_NAME).toString()) + QUOTATION_COMMA);
                stringBuilder.append(district.getId() + ", ");
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
