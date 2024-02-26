package org.motechproject.nms.rejectionhandler.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.repository.ChildRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.ChildRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;

import static org.motechproject.nms.tracking.utils.TrackChangeUtils.LOGGER;

@Service("childRejectionService")
public class ChildRejectionServiceImpl implements ChildRejectionService {

    @Autowired
    private ChildRejectionDataService childRejectionDataService;

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String CHILD_LOG_STRING = "List of child rejects in {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public boolean createOrUpdateChild(ChildImportRejection childImportRejection) {
        if (childImportRejection.getIdNo() != null || childImportRejection.getRegistrationNo() != null) {

            ChildImportRejection childRejectionRecord;
            if ("RCH-Import".equals(childImportRejection.getSource())) {
                childRejectionRecord = childRejectionDataService.findByRegistrationNo(childImportRejection.getRegistrationNo());
            } else {
                childRejectionRecord = childRejectionDataService.findByIdno(childImportRejection.getIdNo());
            }

            if (childRejectionRecord == null && !childImportRejection.getAccepted()) {
                childRejectionDataService.create(childImportRejection);
                return true;
            } else if (childRejectionRecord == null && childImportRejection.getAccepted()) {
                LOGGER.debug(String.format("There is no mother rejection data for mctsId %s and rchId %s", childImportRejection.getIdNo(), childImportRejection.getRegistrationNo()));
            } else {
                // If found one in database, update the record with new data
                childRejectionRecord = setNewData1(childImportRejection, childRejectionRecord);
                childRejectionDataService.update(childRejectionRecord);
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> findChildRejectionByRchId(final Set<String> rchIds) {
        Timer queryTimer = new Timer();
        if (!rchIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            SqlQueryExecution<Map<String, Object>> queryExecution = new SqlQueryExecution<Map<String, Object>>() {

                @Override
                public String getSqlQuery() {
                    String query = "SELECT id, registrationNo, creationDate FROM nms_child_rejects WHERE registrationNo IN " + queryIdList(rchIds);
                    LOGGER.debug(SQL_QUERY_LOG, query);
                    return query;
                }

                @Override
                public Map<String, Object> execute(Query query) {

                    query.setClass(ChildImportRejection.class);
                    ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                    Map<String, Object> resultMap = new HashMap<>();
                    for (ChildImportRejection childReject : (List<ChildImportRejection>) fqr) {
                        resultMap.put(childReject.getRegistrationNo(), childReject);
                    }
                    return resultMap;
                }
            };

            Map<String, Object> resultMap = childRejectionDataService.executeSQLQuery(queryExecution);
            LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
            return resultMap;
        } else {
            return null;
        }
    }

    private String queryIdList(Set<String> idList) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append("(");
        for (String id: idList) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(QUOTATION + id + QUOTATION);
            i++;
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    @Override
    public Map<String, Object> findChildRejectionByMctsId(final Set<String> mctsIds) {
        Timer queryTimer = new Timer();

        if (!mctsIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            SqlQueryExecution<Map<String, Object>> queryExecution = new SqlQueryExecution<Map<String, Object>>() {

                @Override
                public String getSqlQuery() {
                    String query = "SELECT id, idNo, creationDate FROM nms_child_rejects WHERE idNo IN " + queryIdList(mctsIds);
                    LOGGER.debug(SQL_QUERY_LOG, query);
                    return query;
                }

                @Override
                public Map<String, Object> execute(Query query) {

                    query.setClass(ChildImportRejection.class);
                    ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                    Map<String, Object> resultMap = new HashMap<>();
                    for (ChildImportRejection childReject : (List<ChildImportRejection>) fqr) {
                        resultMap.put(childReject.getIdNo(), childReject);
                    }
                    return resultMap;
                }
            };

            Map<String, Object> resultMap = childRejectionDataService.executeSQLQuery(queryExecution);
            LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
            return resultMap;
        } else {
            return null;
        }
    }

    @Override
    public Long rchBulkInsert(final List<ChildImportRejection> createObjects) {

        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "Insert into nms_child_rejects (subcentreId, subcentreName, villageId, villageName, name, mobileNo, stateId, districtId, districtName, talukaId," +
                        " talukaName, healthBlockId, healthBlockName, phcId, phcName, birthDate, registrationDate , registrationNo, entryType, idNo, mCTSMotherIDNo, rCHMotherIDNo, execDate, source," +
                        " accepted, rejectionReason, action, creator, modifiedBy, creationDate, modificationDate) " +
                        "values  " +
                        rchChildToQuerySet(createObjects);

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {
                query.setClass(ChildImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Child Rejection Bulk Insert", e);
                    return 0L;
                }
            }
        };

        Long insertedNo = childRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return insertedNo;
    }

    @Override
    public Long mctsBulkInsert(final List<ChildImportRejection> createObjects) {

        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "Insert into nms_child_rejects (stateId, districtId, districtName, talukaId, talukaName, " +
                        "healthBlockId, healthBlockName, phcId, phcName, subcentreId, subcentreName, villageId, " +
                        "villageName, yr, cityMaholla, gPVillage, address, idNo, name, motherName, mCTSMotherIDNo, " +
                        "phoneNumberWhom, mobileNo, birthDate, registrationDate , placeOfDelivery, bloodGroup, caste, subcenterName1, " +
                        "aNMName, aNMPhone, ashaName, ashaPhone, bCGDt, oPV0Dt, hepatitisB1Dt, dPT1Dt, oPV1Dt, " +
                        "hepatitisB2Dt, dPT2Dt, oPV2Dt, hepatitisB3Dt, dPT3Dt, oPV3Dt, hepatitisB4Dt, measlesDt, " +
                        "vitADose1Dt, mRDt, dPTBoosterDt, oPVBoosterDt, vitADose2Dt, vitADose3Dt, jEDt, vitADose9Dt, " +
                        "dT5Dt, tT10Dt, tT16Dt, cLDRegDATE, sex, vitADose5Dt, vitADose6Dt, vitADose7Dt, vitADose8Dt, " +
                        "lastUpdateDate, remarks, aNMID, ashaID, createdBy, updatedBy, measles2Dt, weightOfChild, " +
                        "childAadhaarNo, childEID, childEIDTime, fatherName, birthCertificateNumber, entryType, source, " +
                        "accepted, rejectionReason, action, creator, modifiedBy, creationDate, modificationDate) values " +
                        mctsChildToQuerySet(createObjects);

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {
                query.setClass(ChildImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Child Rejection Bulk Update", e);
                    return 0L;
                }
            }
        };

        Long insertedNo = childRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return insertedNo;
    }

    private String rchChildToQuerySet(List<ChildImportRejection> childList) {
        StringBuilder stringBuilder = new StringBuilder();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        DateTime dateTimeNow = new DateTime();

        int i = 0;
        for (ChildImportRejection child: childList) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("(");
            stringBuilder = rchQueryHelper(stringBuilder, child);
            stringBuilder.append(MOTECH_STRING);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }
    private String mctsChildToQuerySet(List<ChildImportRejection> childList) { //NOPMD NcssMethodCount
        StringBuilder stringBuilder = new StringBuilder();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        DateTime dateTimeNow = new DateTime();

        int i = 0;
        for (ChildImportRejection child: childList) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("(");
            stringBuilder = mctsQueryHelper(stringBuilder, child);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }

    @Override
    public Long rchBulkUpdate(final List<ChildImportRejection> updateObjects) {

        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "Insert into nms_child_rejects (id, subcentreId, subcentreName, villageId, villageName, name, mobileNo, stateId, districtId, districtName, talukaId," +
                        " talukaName, healthBlockId, healthBlockName, phcId, phcName, birthDate, registrationDate, registrationNo, entryType, idNo, mCTSMotherIDNo, rCHMotherIDNo, execDate, source," +
                        " accepted, rejectionReason, action, modifiedBy, creationDate, modificationDate, creator) " +
                        "values  " +
                        rchChildUpdateQuerySet(updateObjects) +
                        " ON DUPLICATE KEY UPDATE " +
                        "subcentreId = VALUES(subcentreId),  subcentreName = VALUES(subcentreName),  villageId = VALUES(villageId),  villageName = VALUES(villageName),  " +
                        "name = VALUES(name),  mobileNo = VALUES(mobileNo),  stateId = VALUES(stateId),  districtId = VALUES(districtId),  districtName = VALUES(districtName),  talukaId = VALUES(talukaId),  talukaName = VALUES(talukaName),  " +
                        "healthBlockId = VALUES(healthBlockId),  healthBlockName = VALUES(healthBlockName),  phcId = VALUES(phcId),  phcName = VALUES(phcName),  birthDate = VALUES(birthDate), registrationDate = VALUES(registrationDate), registrationNo = VALUES(registrationNo), entryType = VALUES(entryType),  " +
                        "idNo = VALUES(idNo),  mCTSMotherIDNo = VALUES(mCTSMotherIDNo),  rCHMotherIDNo = VALUES(rCHMotherIDNo),  execDate = VALUES(execDate),  source = VALUES(source),  " +
                        "accepted = VALUES(accepted),  rejectionReason = VALUES(rejectionReason),  action = VALUES(action),  modifiedBy = VALUES(modifiedBy), creationDate = VALUES(creationDate), modificationDate = VALUES(modificationDate), creator = VALUES(creator)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(ChildImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Child Rejection Bulk Insert", e);
                    return 0L;
                }
            }
        };

        Long updatedNo = childRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }

    @Override
    public Long mctsBulkUpdate(final List<ChildImportRejection> updateObjects) {

        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "Insert into nms_child_rejects (id, stateId, districtId, districtName, talukaId, talukaName, " +
                        "healthBlockId, healthBlockName, phcId, phcName, subcentreId, subcentreName, villageId, " +
                        "villageName, yr, cityMaholla, gPVillage, address, idNo, name, motherName, mCTSMotherIDNo, " +
                        "phoneNumberWhom, mobileNo, birthDate, registrationDate, placeOfDelivery, bloodGroup, caste, subcenterName1, " +
                        "aNMName, aNMPhone, ashaName, ashaPhone, bCGDt, oPV0Dt, hepatitisB1Dt, dPT1Dt, oPV1Dt, " +
                        "hepatitisB2Dt, dPT2Dt, oPV2Dt, hepatitisB3Dt, dPT3Dt, oPV3Dt, hepatitisB4Dt, measlesDt, " +
                        "vitADose1Dt, mRDt, dPTBoosterDt, oPVBoosterDt, vitADose2Dt, vitADose3Dt, jEDt, vitADose9Dt, " +
                        "dT5Dt, tT10Dt, tT16Dt, cLDRegDATE, sex, vitADose5Dt, vitADose6Dt, vitADose7Dt, vitADose8Dt, " +
                        "lastUpdateDate, remarks, aNMID, ashaID, createdBy, updatedBy, measles2Dt, weightOfChild, " +
                        "childAadhaarNo, childEID, childEIDTime, fatherName, birthCertificateNumber, entryType, source, " +
                        "accepted, rejectionReason, action, creator, modifiedBy, creationDate, modificationDate) values " +
                        mctsChildUpdateQuerySet(updateObjects) +
                        " ON DUPLICATE KEY UPDATE " +
                        "stateId = VALUES(stateId),  districtId = VALUES(districtId),  districtName = VALUES(districtName)," +
                        "  talukaId = VALUES(talukaId),  talukaName = VALUES(talukaName),  healthBlockId = VALUES(healthBlockId)," +
                        "  healthBlockName = VALUES(healthBlockName),  phcId = VALUES(phcId),  phcName = VALUES(phcName)," +
                        "  subcentreId = VALUES(subcentreId),  subcentreName = VALUES(subcentreName)," +
                        "  villageId = VALUES(villageId),  villageName = VALUES(villageName),  yr = VALUES(yr)," +
                        "  cityMaholla = VALUES(cityMaholla),  gPVillage = VALUES(gPVillage),  address = VALUES(address)," +
                        "  idNo = VALUES(idNo),  name = VALUES(name),  motherName = VALUES(motherName)," +
                        "  mCTSMotherIDNo = VALUES(mCTSMotherIDNo),  phoneNumberWhom = VALUES(phoneNumberWhom)," +
                        "  mobileNo = VALUES(mobileNo),  birthDate = VALUES(birthDate)," +
                        "  registrationDate = VALUES(registrationDate)," +
                        "  placeOfDelivery = VALUES(placeOfDelivery),  bloodGroup = VALUES(bloodGroup)," +
                        "  caste = VALUES(caste), subcenterName1 = VALUES(subcenterName1), aNMName = VALUES(aNMName)," +
                        "  aNMPhone = VALUES(aNMPhone),  ashaName = VALUES(ashaName),  ashaPhone = VALUES(ashaPhone)," +
                        "  bCGDt = VALUES(bCGDt), oPV0Dt = VALUES(oPV0Dt), hepatitisB1Dt = VALUES(hepatitisB1Dt)," +
                        "  dPT1Dt = VALUES(dPT1Dt), oPV1Dt = VALUES(oPV1Dt),  hepatitisB2Dt = VALUES(hepatitisB2Dt)," +
                        "  dPT2Dt = VALUES(dPT2Dt),  oPV2Dt = VALUES(oPV2Dt), hepatitisB3Dt = VALUES(hepatitisB3Dt)," +
                        "  dPT3Dt = VALUES(dPT3Dt), oPV3Dt = VALUES(oPV3Dt), hepatitisB4Dt = VALUES(hepatitisB4Dt)," +
                        "  measlesDt = VALUES(measlesDt),  vitADose1Dt = VALUES(vitADose1Dt),  mRDt = VALUES(mRDt)," +
                        "  dPTBoosterDt = VALUES(dPTBoosterDt), oPVBoosterDt = VALUES(oPVBoosterDt)," +
                        "  vitADose2Dt = VALUES(vitADose2Dt), vitADose3Dt = VALUES(vitADose3Dt),  jEDt = VALUES(jEDt)," +
                        "  vitADose9Dt = VALUES(vitADose9Dt),  dT5Dt = VALUES(dT5Dt), tT10Dt = VALUES(tT10Dt)," +
                        "  tT16Dt = VALUES(tT16Dt), cLDRegDATE = VALUES(cLDRegDATE), sex = VALUES(sex)," +
                        "  vitADose5Dt = VALUES(vitADose5Dt),  vitADose6Dt = VALUES(vitADose6Dt)," +
                        "  vitADose7Dt = VALUES(vitADose7Dt), vitADose8Dt = VALUES(vitADose8Dt)," +
                        "  lastUpdateDate = VALUES(lastUpdateDate), remarks = VALUES(remarks), aNMID = VALUES(aNMID)," +
                        "  ashaID = VALUES(ashaID),  createdBy = VALUES(createdBy),  updatedBy = VALUES(updatedBy)," +
                        "  measles2Dt = VALUES(measles2Dt), weightOfChild = VALUES(weightOfChild)," +
                        "  childAadhaarNo = VALUES(childAadhaarNo), childEID = VALUES(childEID)," +
                        "  childEIDTime = VALUES(childEIDTime),  fatherName = VALUES(fatherName)," +
                        "  birthCertificateNumber = VALUES(birthCertificateNumber), entryType = VALUES(entryType)," +
                        "  source = VALUES(source), accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason)," +
                        "  action = VALUES(action),  creator = VALUES(creator),  modifiedBy = VALUES(modifiedBy)," +
                        "  creationDate = VALUES(creationDate), modificationDate = VALUES(modificationDate)";
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(ChildImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Child Rejection Bulk Update", e);
                    return 0L;
                }
            }
        };

        Long updatedNo = childRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }

    private String mctsChildUpdateQuerySet(List<ChildImportRejection> childList) {
        StringBuilder stringBuilder = new StringBuilder();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        DateTime dateTimeNow = new DateTime();
        int i = 0;
        for (ChildImportRejection child: childList) {
            String creationTime = "";
            Long id = null;
            if (i != 0) {
                stringBuilder.append(", ");
            }
            try {
                Method method = child.getClass().getMethod("getCreationDate");
                DateTime dateTime = (DateTime) method.invoke(child);
                creationTime = dateTimeFormatter.print(dateTime);

                method = child.getClass().getMethod("getId");
                id = (Long) method.invoke(child);
            } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
            InvocationTargetException e) {
                LOGGER.error("Ignoring creation date and setting as now");
            }
            stringBuilder.append("(");
            stringBuilder.append(id + ", ");
            stringBuilder = mctsQueryHelper(stringBuilder, child);
            stringBuilder.append(QUOTATION + creationTime + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }

    private String rchChildUpdateQuerySet(List<ChildImportRejection> childList) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (ChildImportRejection child: childList) {
            Long id = null;
            String creationTime = "";
            try {
                Method method = child.getClass().getMethod("getCreationDate");
                DateTime dateTime = (DateTime) method.invoke(child);
                method = child.getClass().getMethod("getId");
                creationTime = dateTimeFormatter.print(dateTime);

                id = (Long) method.invoke(child);
            } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
                    InvocationTargetException e) {
                LOGGER.error("Ignoring creation date and setting as now");
            }
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("(");
            stringBuilder.append(id + ", ");
            stringBuilder = rchQueryHelper(stringBuilder, child);
            stringBuilder.append(QUOTATION + creationTime + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION_COMMA);
            stringBuilder.append("'motech'");
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }


    private StringBuilder mctsQueryHelper(StringBuilder stringBuilder, ChildImportRejection child) { //NOPMD NcssMethodCount
        stringBuilder.append(child.getStateId() + ", ");
        stringBuilder.append(child.getDistrictId() + ", ");
        stringBuilder.append(QUOTATION + child.getDistrictName() + QUOTATION_COMMA);
        stringBuilder.append(child.getTalukaId() + ", ");
        stringBuilder.append(QUOTATION + child.getTalukaName() + QUOTATION_COMMA);
        stringBuilder.append(child.getHealthBlockId() + ", ");
        stringBuilder.append(QUOTATION + child.getHealthBlockName() + QUOTATION_COMMA);
        stringBuilder.append(child.getPhcId() + ", ");
        stringBuilder.append(QUOTATION + child.getPhcName() + QUOTATION_COMMA);
        stringBuilder.append(child.getSubcentreId() + ", ");
        stringBuilder.append(QUOTATION + child.getSubcentreName() + QUOTATION_COMMA);
        stringBuilder.append(child.getVillageId() + ", ");
        stringBuilder.append(QUOTATION + child.getVillageName() + QUOTATION_COMMA);
        stringBuilder.append(child.getYr() + ", ");
        stringBuilder.append(QUOTATION + child.getCityMaholla() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getgPVillage() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getAddress() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getIdNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(child.getName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(child.getMotherName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getMotherId() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getPhoneNumberWhom() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getMobileNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getBirthDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getRegistrationDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getPlaceOfDelivery() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getBloodGroup() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getCaste() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getSubcenterName1() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(child.getaNMName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getaNMPhone() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(child.getAshaName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getAshaPhone() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getbCGDt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getoPV0Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getHepatitisB1Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getdPT1Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getoPV1Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getHepatitisB2Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getdPT2Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getoPV2Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getHepatitisB3Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getdPT3Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getoPV3Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getHepatitisB4Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getMeaslesDt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose1Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getmRDt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getdPTBoosterDt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getoPVBoosterDt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose2Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose3Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getjEDt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose9Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getdT5Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.gettT10Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.gettT16Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getcLDRegDATE() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getSex() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose5Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose6Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose7Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getVitADose8Dt() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getLastUpdateDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getRemarks() + QUOTATION_COMMA);
        stringBuilder.append(child.getaNMID() + ", ");
        stringBuilder.append(child.getAshaID() + ", ");
        stringBuilder.append(child.getCreatedBy() + ", ");
        stringBuilder.append(child.getUpdatedBy() + ", ");
        stringBuilder.append(QUOTATION + child.getMeasles2Dt() + QUOTATION_COMMA);
        stringBuilder.append(child.getWeightOfChild() + ", ");
        stringBuilder.append(child.getChildAadhaarNo() + ", ");
        stringBuilder.append(child.getChildEID() + ", ");
        stringBuilder.append(QUOTATION + child.getChildEIDTime() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getFatherName() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getBirthCertificateNumber() + QUOTATION_COMMA);
        stringBuilder.append(child.getEntryType() + ", ");
        stringBuilder.append(QUOTATION + child.getSource() + QUOTATION_COMMA);
        stringBuilder.append(child.getAccepted() + ", ");
        stringBuilder.append(QUOTATION + child.getRejectionReason() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getAction() + QUOTATION_COMMA);
        stringBuilder.append(MOTECH_STRING);
        stringBuilder.append(MOTECH_STRING);
        return stringBuilder;
    }


    private StringBuilder rchQueryHelper(StringBuilder stringBuilder, ChildImportRejection child) {
        stringBuilder.append(child.getSubcentreId() + ", ");
        stringBuilder.append(QUOTATION + child.getSubcentreName() + QUOTATION_COMMA);
        stringBuilder.append(child.getVillageId() + ", ");
        stringBuilder.append(QUOTATION + child.getVillageName() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(child.getName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getMobileNo() + QUOTATION_COMMA);
        stringBuilder.append(child.getStateId() + ", ");
        stringBuilder.append(child.getDistrictId() + ", ");
        stringBuilder.append(QUOTATION + child.getDistrictName() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getTalukaId() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getTalukaName() + QUOTATION_COMMA);
        stringBuilder.append(child.getHealthBlockId() + ", ");
        stringBuilder.append(QUOTATION + child.getHealthBlockName() + QUOTATION_COMMA);
        stringBuilder.append(child.getPhcId() + ", ");
        stringBuilder.append(QUOTATION + child.getPhcName() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getBirthDate() + QUOTATION_COMMA);
        if(child.getRegistrationDate() == null){
            stringBuilder.append(child.getRegistrationDate() + " , ");
        }
        else{
            stringBuilder.append(QUOTATION + child.getRegistrationDate() + QUOTATION_COMMA);
        }
        stringBuilder.append(QUOTATION + child.getRegistrationNo() + QUOTATION_COMMA);
        stringBuilder.append(child.getEntryType() + ", ");
        stringBuilder.append(QUOTATION + child.getIdNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getmCTSMotherIDNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getMotherId() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getExecDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getSource() + QUOTATION_COMMA);
        stringBuilder.append(child.getAccepted() + ", ");
        stringBuilder.append(QUOTATION + child.getRejectionReason() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + child.getAction() + QUOTATION_COMMA);
        stringBuilder.append(MOTECH_STRING);
        return stringBuilder;
    }

    private static ChildImportRejection setNewData1(ChildImportRejection childImportRejection, ChildImportRejection childImportRejection1) {
        childImportRejection1.setStateId(childImportRejection.getStateId());
        childImportRejection1.setDistrictId(childImportRejection.getDistrictId());
        childImportRejection1.setDistrictName(childImportRejection.getDistrictName());
        childImportRejection1.setTalukaId(childImportRejection.getTalukaId());
        childImportRejection1.setTalukaName(childImportRejection.getTalukaName());
        childImportRejection1.setHealthBlockId(childImportRejection.getHealthBlockId());
        childImportRejection1.setHealthBlockName(childImportRejection.getHealthBlockName());
        childImportRejection1.setPhcId(childImportRejection.getPhcId());
        childImportRejection1.setPhcName(childImportRejection.getPhcName());
        childImportRejection1.setSubcentreId(childImportRejection.getSubcentreId());
        childImportRejection1.setSubcentreName(childImportRejection.getSubcentreName());
        childImportRejection1.setVillageId(childImportRejection.getVillageId());
        childImportRejection1.setVillageName(childImportRejection.getVillageName());
        childImportRejection1.setYr(childImportRejection.getYr());
        childImportRejection1.setCityMaholla(childImportRejection.getCityMaholla());
        childImportRejection1.setgPVillage(childImportRejection.getgPVillage());
        childImportRejection1.setAddress(childImportRejection.getAddress());
        childImportRejection1.setIdNo(childImportRejection.getIdNo());
        childImportRejection1.setName(childImportRejection.getName());
        childImportRejection1.setMobileNo(childImportRejection.getMobileNo());
        childImportRejection1.setMotherName(childImportRejection.getMotherName());
        childImportRejection1.setMotherId(childImportRejection.getMotherId());
        childImportRejection1.setAshaPhone(childImportRejection.getAshaPhone());
        childImportRejection1.setbCGDt(childImportRejection.getbCGDt());
        childImportRejection1.setoPV0Dt(childImportRejection.getoPV0Dt());
        childImportRejection1.setHepatitisB1Dt(childImportRejection.getHepatitisB1Dt());
        childImportRejection1.setdPT1Dt(childImportRejection.getdPT1Dt());
        childImportRejection1.setoPV1Dt(childImportRejection.getoPV1Dt());
        childImportRejection1.setHepatitisB2Dt(childImportRejection.getHepatitisB2Dt());
        childImportRejection1.setPhoneNumberWhom(childImportRejection.getPhoneNumberWhom());
        childImportRejection1.setBirthDate(childImportRejection.getBirthDate());
        childImportRejection1.setPlaceOfDelivery(childImportRejection.getPlaceOfDelivery());
        childImportRejection1.setBloodGroup(childImportRejection.getBloodGroup());
        childImportRejection1.setCaste(childImportRejection.getCaste());
        childImportRejection1.setSubcenterName1(childImportRejection1.getSubcenterName1());
        childImportRejection1.setaNMName(childImportRejection.getaNMName());
        childImportRejection1.setaNMPhone(childImportRejection.getaNMPhone());
        childImportRejection1.setAshaName(childImportRejection.getAshaName());
        childImportRejection1.setdPT2Dt(childImportRejection.getdPT2Dt());
        childImportRejection1.setoPV2Dt(childImportRejection.getoPV2Dt());
        setNewData2(childImportRejection, childImportRejection1);
        return childImportRejection1;
    }

    private static void setNewData2(ChildImportRejection childImportRejection, ChildImportRejection childImportRejection1) {
        childImportRejection1.setHepatitisB3Dt(childImportRejection.getHepatitisB3Dt());
        childImportRejection1.setdPT3Dt(childImportRejection.getdPT3Dt());
        childImportRejection1.setoPV3Dt(childImportRejection.getoPV3Dt());
        childImportRejection1.setHepatitisB4Dt(childImportRejection.getHepatitisB4Dt());
        childImportRejection1.setMeaslesDt(childImportRejection.getMeaslesDt());
        childImportRejection1.setVitADose1Dt(childImportRejection.getVitADose1Dt());
        childImportRejection1.setmRDt(childImportRejection.getmRDt());
        childImportRejection1.setdPTBoosterDt(childImportRejection.getdPTBoosterDt());
        childImportRejection1.setoPVBoosterDt(childImportRejection.getoPVBoosterDt());
        childImportRejection1.setVitADose2Dt(childImportRejection.getVitADose2Dt());
        childImportRejection1.setVitADose3Dt(childImportRejection.getVitADose3Dt());
        childImportRejection1.settT10Dt(childImportRejection.gettT10Dt());
        childImportRejection1.setjEDt(childImportRejection.getjEDt());
        childImportRejection1.setVitADose9Dt(childImportRejection.getVitADose9Dt());
        childImportRejection1.setdT5Dt(childImportRejection.getdT5Dt());
        childImportRejection1.settT16Dt(childImportRejection.gettT16Dt());
        childImportRejection1.setcLDRegDATE(childImportRejection.getcLDRegDATE());
        childImportRejection1.setAshaID(childImportRejection.getAshaID());
        childImportRejection1.setLastUpdateDate(childImportRejection.getLastUpdateDate());
        childImportRejection1.setVitADose6Dt(childImportRejection.getVitADose6Dt());
        childImportRejection1.setRemarks(childImportRejection.getRemarks());
        childImportRejection1.setaNMID(childImportRejection.getaNMID());
        childImportRejection1.setCreatedBy(childImportRejection.getCreatedBy());
        childImportRejection1.setUpdatedBy(childImportRejection.getUpdatedBy());
        childImportRejection1.setMeasles2Dt(childImportRejection.getMeasles2Dt());
        childImportRejection1.setWeightOfChild(childImportRejection.getWeightOfChild());
        childImportRejection1.setChildAadhaarNo(childImportRejection.getChildAadhaarNo());
        childImportRejection1.setChildEID(childImportRejection.getChildEID());
        childImportRejection1.setSex(childImportRejection.getSex());
        childImportRejection1.setVitADose5Dt(childImportRejection.getVitADose5Dt());
        childImportRejection1.setVitADose7Dt(childImportRejection.getVitADose7Dt());
        childImportRejection1.setVitADose8Dt(childImportRejection.getVitADose8Dt());
        childImportRejection1.setChildEIDTime(childImportRejection.getChildEIDTime());
        childImportRejection1.setFatherName(childImportRejection.getFatherName());
        childImportRejection1.setExecDate(childImportRejection.getExecDate());
        childImportRejection1.setAccepted(childImportRejection.getAccepted());
        childImportRejection1.setRejectionReason(childImportRejection.getRejectionReason());
        childImportRejection1.setBirthCertificateNumber(childImportRejection.getBirthCertificateNumber());
        childImportRejection1.setEntryType(childImportRejection.getEntryType());
        childImportRejection1.setSource(childImportRejection.getSource());
        childImportRejection1.setRegistrationNo(childImportRejection.getRegistrationNo());
        childImportRejection1.setmCTSMotherIDNo(childImportRejection.getmCTSMotherIDNo());
        childImportRejection1.setAction(childImportRejection.getAction());
    }

}
