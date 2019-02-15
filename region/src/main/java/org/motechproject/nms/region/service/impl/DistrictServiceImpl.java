package org.motechproject.nms.region.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.service.DistrictService;
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

@Service("districtService")
public class DistrictServiceImpl implements DistrictService {

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    private static final Logger LOGGER = LoggerFactory.getLogger(DistrictServiceImpl.class);

    private DistrictDataService districtDataService;

    @Autowired
    public DistrictServiceImpl(DistrictDataService districtDataService) {
        this.districtDataService = districtDataService;
    }

    @Override
    public Set<District> getAllForLanguage(final Language language) {

        QueryExecution<Set<District>> stateQueryExecution = new QueryExecution<Set<District>>() {
            @Override
            public Set<District> execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("language == _language");
                query.declareParameters("org.motechproject.nms.region.domain.Language _language");

                Set<District> districts = new HashSet<>();
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(language);
                for (Object o : fqr) {
                    districts.add((District) o);
                }
                return districts;
            }
        };

        return districtDataService.executeQuery(stateQueryExecution);
    }

    @Override
    public District findByStateAndCode(final State state, final Long code) {

        if (state == null) { return null; }

        SqlQueryExecution<District> queryExecution = new SqlQueryExecution<District>() {

            @Override
            public String getSqlQuery() {
                return "select * from nms_districts where state_id_oid = ? and code = ?";
            }

            @Override
            public District execute(Query query) {
                query.setClass(District.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(state.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (District) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return districtDataService.executeSQLQuery(queryExecution);

    }

    @Override
    public District findByStateAndName(final State state, final String name) {

        if (state == null) { return null; }

        SqlQueryExecution<District> queryExecution = new SqlQueryExecution<District>() {

            @Override
            public String getSqlQuery() {
                return "select * from nms_districts where state_id_oid = ? and name = ?";
            }

            @Override
            public District execute(Query query) {
                query.setClass(District.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(state.getId(), name);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (District) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return districtDataService.executeSQLQuery(queryExecution);

    }

    @Override
    public District create(District district) {
        return districtDataService.create(district);
    }

    @Override
    public District update(District district) {
        return districtDataService.update(district);
    }

    @Override
    public Object getDetachedField(District district, String fieldName) {
        return districtDataService.getDetachedField(district, fieldName);
    }

    @Override
    @Transactional
    public Long createUpdateDistricts(final List<Map<String, Object>> districts, final Map<String, State> stateHashMap) {
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String districtValues = districtQuerySet(districts, stateHashMap);
                String query = "";
                if(!districtValues.isEmpty()) {
                    query = "INSERT into nms_districts (`code`, `name`, `regionalName`, `state_id_OID`, " +
                            " `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                            districtValues +
                            " ON DUPLICATE KEY UPDATE " +
                            "name = VALUES(name), modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                }
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }



            @Override
            public Long execute(Query query) {
                query.setClass(District.class);
                return (Long) query.execute();
            }
        };

        Long createdDistricts = 0L;
        if (!stateHashMap.isEmpty() && !queryExecution.getSqlQuery().isEmpty()) {
            createdDistricts = districtDataService.executeSQLQuery(queryExecution);
        }

        return createdDistricts;
    }

    private String districtQuerySet(List<Map<String, Object>> districts, Map<String, State> stateHashMap) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (Map<String, Object> district : districts) {
            if (district.get(LocationConstants.CSV_STATE_ID) != null) {
                State state = stateHashMap.get(district.get(LocationConstants.CSV_STATE_ID).toString());
                Long districtCode = (Long) district.get(LocationConstants.DISTRICT_ID);
                String districtName = (String) district.get(LocationConstants.DISTRICT_NAME);
                if (state != null && districtCode != null && (districtName != null && !districtName.trim().isEmpty()) && !((Long) (0L)).equals(districtCode)) {
                    if (i != 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append("(");
                    stringBuilder.append(districtCode + ", ");
                    stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(district.get(LocationConstants.DISTRICT_NAME) == null ?
                            "" : district.get(LocationConstants.DISTRICT_NAME).toString()) + QUOTATION_COMMA);
                    stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(district.get(LocationConstants.DISTRICT_NAME) == null ?
                            "" : district.get(LocationConstants.DISTRICT_NAME).toString()) + QUOTATION_COMMA);
                    stringBuilder.append(state.getId() + ", ");
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
    public Map<String, District> fillDistrictIds(List<Map<String, Object>> recordList, final Map<String, State> stateHashMap) {
        final Set<String> districtKeys = new HashSet<>();
        for(Map<String, Object> record : recordList) {
            if (record.get(LocationConstants.CSV_STATE_ID) != null && record.get(LocationConstants.DISTRICT_ID) != null) {
                districtKeys.add(record.get(LocationConstants.CSV_STATE_ID).toString() + "_" + record.get(LocationConstants.DISTRICT_ID).toString());
            }
        }
        Map<String, District> districtHashMap = new HashMap<>();

        Map<Long, String> stateIdMap = new HashMap<>();
        for (String stateKey : stateHashMap.keySet()) {
            stateIdMap.put(stateHashMap.get(stateKey).getId(), stateKey);
        }

        Timer queryTimer = new Timer();


        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<District>> queryExecution = new SqlQueryExecution<List<District>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_districts where";
                int count = districtKeys.size();
                for (String districtString : districtKeys) {
                    String[] ids = districtString.split("_");
                    State state = stateHashMap.get(ids[0]);
                    if (state != null) {
                        if (count != districtKeys.size()) {
                            query += LocationConstants.OR_SQL_STRING;
                        }
                        query += LocationConstants.CODE_SQL_STRING + ids[1] + " and state_id_oid = " + state.getId() + ")";
                        count--;
                    }
                }

                LOGGER.debug("DISTRICT Query: {}", query);
                return query;
            }

            @Override
            public List<District> execute(Query query) {
                query.setClass(District.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<District> districts;
                if (fqr.isEmpty()) {
                    return null;
                }
                districts = (List<District>) fqr;
                return districts;
            }
        };

        List<District> districts = null;
        if (!stateHashMap.isEmpty() && !districtKeys.isEmpty()) {
            districts = districtDataService.executeSQLQuery(queryExecution);
        }
        LOGGER.debug("DISTRICT Query time: {}", queryTimer.time());
        if (districts != null && !districts.isEmpty()) {
            for (District district : districts) {
                String stateKey = stateIdMap.get(district.getState().getId());
                districtHashMap.put(stateKey + "_" + district.getCode(), district);
            }
        }

        return districtHashMap;
    }
}
