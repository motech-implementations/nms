package org.motechproject.nms.rejectionhandler.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.nms.rejectionhandler.repository.MotherRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.MotherRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.motechproject.nms.tracking.utils.TrackChangeUtils.LOGGER;

/**
 * Created by beehyv on 17/7/17.
 */
@Service("motherRejectionService")
public class MotherRejectionServiceImpl implements MotherRejectionService {

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String MOTHER_LOG_STRING = "List of mother rejects in {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private MotherRejectionDataService motherRejectionDataService;

    @Override
    public MotherImportRejection findByMotherId(String idNo, String registrationNo) {
        return motherRejectionDataService.findRejectedMother(idNo, registrationNo);
    }

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public void createOrUpdateMother(MotherImportRejection motherImportRejection) {
        if (motherImportRejection.getIdNo() != null || motherImportRejection.getRegistrationNo() != null) {
            MotherImportRejection motherImportRejection1 = motherRejectionDataService.findRejectedMother(motherImportRejection.getIdNo(), motherImportRejection.getRegistrationNo());

            if (motherImportRejection1 == null && !motherImportRejection.getAccepted()) {
                motherRejectionDataService.create(motherImportRejection);
            } else if (motherImportRejection1 == null && motherImportRejection.getAccepted()) {
                LOGGER.debug(String.format("There is no mother rejection data for mctsId %s and rchId %s", motherImportRejection.getIdNo(), motherImportRejection.getRegistrationNo()));
            } else if (motherImportRejection1 != null && !motherImportRejection1.getAccepted()) {
                motherImportRejection1 = setNewData1(motherImportRejection, motherImportRejection1);
                motherRejectionDataService.update(motherImportRejection1);
            } else if (motherImportRejection1 != null && motherImportRejection1.getAccepted()) {
                motherImportRejection1 = setNewData1(motherImportRejection, motherImportRejection1);
                motherRejectionDataService.update(motherImportRejection1);
            }
        }
    }

    @Override
    public Map<String, Object> findMotherRejectionByRchId(final Set<String> rchIds) {
        if (rchIds == null || rchIds.isEmpty()) {
            return new HashMap<>();
        }
        Timer queryTimer = new Timer();

        LOGGER.debug("size of rchId: {}", rchIds.size());
        if (!rchIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            SqlQueryExecution<Map<String, Object>> queryExecution = new SqlQueryExecution<Map<String, Object>>() {

                @Override
                public String getSqlQuery() {
                    String query = "SELECT id, registrationNo, creationDate FROM nms_mother_rejects WHERE registrationNo IN " + queryIdList(rchIds);
                    LOGGER.debug(SQL_QUERY_LOG, query);
                    return query;
                }

                @Override
                public Map<String, Object> execute(Query query) {

                    query.setClass(MotherImportRejection.class);
                    ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                    Map<String, Object> resultMap = new HashMap<>();
                    for (MotherImportRejection motherReject : (List<MotherImportRejection>) fqr) {
                        resultMap.put(motherReject.getRegistrationNo(), motherReject);
                    }
                    return resultMap;
                }
            };

            Map<String, Object> resultMap = motherRejectionDataService.executeSQLQuery(queryExecution);
            LOGGER.debug(MOTHER_LOG_STRING, queryTimer.time());
            return resultMap;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> findMotherRejectionByMctsId(final Set<String> mctsIds) {
        if (mctsIds == null || mctsIds.isEmpty()) {
            return new HashMap<>();
        }
        Timer queryTimer = new Timer();

        if (!mctsIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            SqlQueryExecution<Map<String, Object>> queryExecution = new SqlQueryExecution<Map<String, Object>>() {

                @Override
                public String getSqlQuery() {
                    String query = "SELECT id, idNo, creationDate FROM nms_mother_rejects WHERE idNo IN " + queryIdList(mctsIds);
                    LOGGER.debug(SQL_QUERY_LOG, query);
                    return query;
                }

                @Override
                public Map<String, Object> execute(Query query) {

                    query.setClass(MotherImportRejection.class);
                    ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                    Map<String, Object> resultMap = new HashMap<>();
                    for (MotherImportRejection motherReject : (List<MotherImportRejection>) fqr) {
                        resultMap.put(motherReject.getIdNo(), motherReject);
                    }
                    return resultMap;
                }
            };

            Map<String, Object> resultMap = motherRejectionDataService.executeSQLQuery(queryExecution);
            LOGGER.debug(MOTHER_LOG_STRING, queryTimer.time());
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
    public Long mctsBulkInsert(final List<MotherImportRejection> createObjects) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mother_rejects (stateId, districtId, districtName, talukaId, talukaName," +
                        " healthBlockId, healthBlockName, phcId, phcName, subcentreId, subcentreName, villageId," +
                        " villageName, yr, gPVillage, address, idNo, name, husbandName, phoneNumberWhom, mobileNo, birthDate," +
                        " jSYBeneficiary, caste, subcenterName1, aNMName, aNMPhone, ashaName, ashaPhone, deliveryLnkFacility," +
                        " facilityName, lmpDate, aNC1Date, aNC2Date, aNC3Date, aNC4Date, tT1Date, tT2Date, tTBoosterDate," +
                        " iFA100GivenDate, anemia, aNCComplication, rTISTI, dlyDate, dlyPlaceHomeType, dlyPlacePublic," +
                        " dlyPlacePrivate, dlyType, dlyComplication, dischargeDate, jSYPaidDate, abortion, pNCHomeVisit," +
                        " pNCComplication, pPCMethod, pNCCheckup, outcomeNos, child1Name, child1Sex, child1Wt, child1Brestfeeding," +
                        " child2Name, child2Sex, child2Wt, child2Brestfeeding, child3Name, child3Sex, child3Wt, child3Brestfeeding," +
                        " child4Name, child4Sex, child4Wt, child4Brestfeeding, age, mTHRREGDATE, lastUpdateDate, remarks, aNMID," +
                        " aSHAID, callAns, noCallReason, noPhoneReason, createdBy, updatedBy, aadharNo, bPLAPL, eID, eIDTime," +
                        " entryType, source, accepted, rejectionReason, action, creator, modifiedBy, creationDate," +
                        " modificationDate) values " +
                        mctsMotherToQuerySet(createObjects);

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {
                query.setClass(MotherImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Mother Rejection Bulk Insert", e);
                    return 0L;
                }
            }
        };

        Long insertedNo = motherRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(MOTHER_LOG_STRING, queryTimer.time());
        return insertedNo;
    }

    @Override
    public Long mctsBulkUpdate(final List<MotherImportRejection> updateObjects) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mother_rejects (id, stateId, districtId, districtName, talukaId, talukaName," +
                        " healthBlockId, healthBlockName, phcId, phcName, subcentreId, subcentreName, villageId, villageName, yr," +
                        " gPVillage, address, idNo, name, husbandName, phoneNumberWhom, mobileNo, birthDate, jSYBeneficiary, caste," +
                        " subcenterName1, aNMName, aNMPhone, ashaName, ashaPhone, deliveryLnkFacility, facilityName, lmpDate, aNC1Date," +
                        " aNC2Date, aNC3Date, aNC4Date, tT1Date, tT2Date, tTBoosterDate, iFA100GivenDate, anemia, aNCComplication," +
                        " rTISTI, dlyDate, dlyPlaceHomeType, dlyPlacePublic, dlyPlacePrivate, dlyType, dlyComplication, dischargeDate," +
                        " jSYPaidDate, abortion, pNCHomeVisit, pNCComplication, pPCMethod, pNCCheckup, outcomeNos, child1Name," +
                        " child1Sex, child1Wt, child1Brestfeeding, child2Name, child2Sex, child2Wt, child2Brestfeeding, child3Name," +
                        " child3Sex, child3Wt, child3Brestfeeding, child4Name, child4Sex, child4Wt, child4Brestfeeding, age," +
                        " mTHRREGDATE, lastUpdateDate, remarks, aNMID, aSHAID, callAns, noCallReason, noPhoneReason, createdBy," +
                        " updatedBy, aadharNo, bPLAPL, eID, eIDTime, entryType, source, accepted, rejectionReason, action, creator," +
                        " modifiedBy, creationDate, modificationDate) values " +
                        mctsMotherUpdateQuerySet(updateObjects) +
                        " ON DUPLICATE KEY UPDATE " +
                        " stateId = VALUES(stateId),  districtId = VALUES(districtId),  districtName = VALUES(districtName)," +
                        " talukaId = VALUES(talukaId),  talukaName = VALUES(talukaName),  healthBlockId = VALUES(healthBlockId)," +
                        " healthBlockName = VALUES(healthBlockName),  phcId = VALUES(phcId),  phcName = VALUES(phcName)," +
                        " subcentreId = VALUES(subcentreId),  subcentreName = VALUES(subcentreName), villageId = VALUES(villageId)," +
                        " villageName = VALUES(villageName),  yr = VALUES(yr), gPVillage = VALUES(gPVillage), address = VALUES(address)," +
                        " idNo = VALUES(idNo), name = VALUES(name),  husbandName = VALUES(husbandName)," +
                        " phoneNumberWhom = VALUES(phoneNumberWhom), mobileNo = VALUES(mobileNo),  birthDate = VALUES(birthDate)," +
                        " jSYBeneficiary = VALUES(jSYBeneficiary), caste = VALUES(caste),  subcenterName1 = VALUES(subcenterName1)," +
                        " aNMName = VALUES(aNMName), aNMPhone = VALUES(aNMPhone),  ashaName = VALUES(ashaName)," +
                        " ashaPhone = VALUES(ashaPhone), deliveryLnkFacility = VALUES(deliveryLnkFacility)," +
                        " facilityName = VALUES(facilityName),  lmpDate = VALUES(lmpDate), aNC1Date = VALUES(aNC1Date)," +
                        " aNC2Date = VALUES(aNC2Date),  aNC3Date = VALUES(aNC3Date), aNC4Date = VALUES(aNC4Date)," +
                        " tT1Date = VALUES(tT1Date),  tT2Date = VALUES(tT2Date), tTBoosterDate = VALUES(tTBoosterDate)," +
                        " iFA100GivenDate = VALUES(iFA100GivenDate),  anemia = VALUES(anemia), aNCComplication = VALUES(aNCComplication)," +
                        " rTISTI = VALUES(rTISTI),  dlyDate = VALUES(dlyDate), dlyPlaceHomeType = VALUES(dlyPlaceHomeType)," +
                        " dlyPlacePublic = VALUES(dlyPlacePublic),  dlyPlacePrivate = VALUES(dlyPlacePrivate), dlyType = VALUES(dlyType)," +
                        " dlyComplication = VALUES(dlyComplication),  dischargeDate = VALUES(dischargeDate), jSYPaidDate = VALUES(jSYPaidDate)," +
                        " abortion = VALUES(abortion),  pNCHomeVisit = VALUES(pNCHomeVisit), pNCComplication = VALUES(pNCComplication)," +
                        " pPCMethod = VALUES(pPCMethod),  pNCCheckup = VALUES(pNCCheckup), outcomeNos = VALUES(outcomeNos)," +
                        " child1Name = VALUES(child1Name),  child1Sex = VALUES(child1Sex), child1Wt = VALUES(child1Wt)," +
                        " child1Brestfeeding = VALUES(child1Brestfeeding), child2Name = VALUES(child2Name)," +
                        " child2Sex = VALUES(child2Sex), child2Wt = VALUES(child2Wt), child2Brestfeeding = VALUES(child2Brestfeeding)," +
                        " child3Name = VALUES(child3Name),  child3Sex = VALUES(child3Sex), child3Wt = VALUES(child3Wt)," +
                        " child3Brestfeeding = VALUES(child3Brestfeeding), child4Name = VALUES(child4Name)," +
                        " child4Sex = VALUES(child4Sex), child4Wt = VALUES(child4Wt), child4Brestfeeding = VALUES(child4Brestfeeding)," +
                        " age = VALUES(age), mTHRREGDATE = VALUES(mTHRREGDATE), lastUpdateDate = VALUES(lastUpdateDate)," +
                        " remarks = VALUES(remarks), aNMID = VALUES(aNMID), aSHAID = VALUES(aSHAID), callAns = VALUES(callAns)," +
                        " noCallReason = VALUES(noCallReason), noPhoneReason = VALUES(noPhoneReason), createdBy = VALUES(createdBy)," +
                        " updatedBy = VALUES(updatedBy), aadharNo = VALUES(aadharNo), bPLAPL = VALUES(bPLAPL), eID = VALUES(eID)," +
                        " eIDTime = VALUES(eIDTime), entryType = VALUES(entryType), source = VALUES(source)," +
                        " accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason), action = VALUES(action)," +
                        " creator = VALUES(creator), modifiedBy = VALUES(modifiedBy), creationDate = VALUES(creationDate)," +
                        " modificationDate = VALUES(modificationDate)";
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(MotherImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Mother Rejection Bulk Update", e);
                    return 0L;
                }
            }
        };

        Long updatedNo = motherRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(MOTHER_LOG_STRING, queryTimer.time());
        return updatedNo;
    }


    @Override
    public Long rchBulkInsert(final List<MotherImportRejection> createObjects) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mother_rejects (stateId, districtId, districtName, talukaId, talukaName," +
                        " healthBlockId, healthBlockName, phcId, phcName, subcentreId, subcentreName, villageId," +
                        " villageName, idNo, registrationNo, caseNo, name, mobileNo, registrationDate, lmpDate, birthDate, abortionType," +
                        " deliveryOutcomes, entryType, execDate, source, accepted, rejectionReason, action, creator," +
                        " modifiedBy, creationDate, modificationDate) " +
                        "values  " +
                        rchMotherToQuerySet(createObjects);

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {
                query.setClass(MotherImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Mother Rejection Bulk Insert", e);
                    return 0L;
                }
            }
        };

        Long insertedNo = motherRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(MOTHER_LOG_STRING, queryTimer.time());
        return insertedNo;
    }

    @Override
    public Long rchBulkUpdate(final List<MotherImportRejection> updateObjects) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mother_rejects (id, stateId, districtId, districtName, talukaId," +
                        " talukaName, healthBlockId, healthBlockName, phcId, phcName, subcentreId, subcentreName," +
                        " villageId, villageName, idNo, registrationNo, caseNo, name, mobileNo, registrationDate, lmpDate, birthDate," +
                        " abortionType, deliveryOutcomes, entryType, execDate, source, accepted, rejectionReason, action," +
                        " creator, modifiedBy, creationDate, modificationDate)  " +
                        "values  " +
                        rchMotherUpdateQuerySet(updateObjects) +
                        " ON DUPLICATE KEY UPDATE " +
                        "stateId = VALUES(stateId), districtId = VALUES(districtId), districtName = VALUES(districtName)," +
                        " talukaId = VALUES(talukaId), talukaName = VALUES(talukaName)," +
                        " healthBlockId = VALUES(healthBlockId), healthBlockName = VALUES(healthBlockName), phcId = VALUES(phcId)," +
                        " phcName = VALUES(phcName), subcentreId = VALUES(subcentreId), subcentreName = VALUES(subcentreName)," +
                        " villageId = VALUES(villageId), villageName = VALUES(villageName), idNo = VALUES(idNo)," +
                        " registrationNo = VALUES(registrationNo), caseNo = VALUES(caseNo), name = VALUES(name)," +
                        " mobileNo = VALUES(mobileNo), registrationDate = VALUES(registrationDate) ,lmpDate = VALUES(lmpDate), birthDate = VALUES(birthDate)," +
                        " abortionType = VALUES(abortionType), deliveryOutcomes = VALUES(deliveryOutcomes)," +
                        " entryType = VALUES(entryType), execDate = VALUES(execDate), source = VALUES(source)," +
                        " accepted = VALUES(accepted), rejectionReason = VALUES(rejectionReason), action = VALUES(action)," +
                        " creator = VALUES(creator), modifiedBy = VALUES(modifiedBy), creationDate = VALUES(creationDate)," +
                        " modificationDate = VALUES(modificationDate)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(MotherImportRejection.class);
                try {
                    return (Long) query.execute();
                } catch (Exception e) {
                    LOGGER.debug("Error while running Mother Rejection Bulk Update", e);
                    return 0L;
                }
            }
        };

        Long updatedNo = motherRejectionDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(MOTHER_LOG_STRING, queryTimer.time());
        return updatedNo;
    }

    private String rchMotherToQuerySet(List<MotherImportRejection> createObjects) {
        StringBuilder stringBuilder = new StringBuilder();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        DateTime dateTimeNow = new DateTime();

        int i = 0;
        for (MotherImportRejection mother: createObjects) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("(");
            stringBuilder = rchQueryHelper(stringBuilder, mother);
            stringBuilder.append(MOTECH_STRING);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }

    private String mctsMotherToQuerySet(List<MotherImportRejection> createObjects) {
        StringBuilder stringBuilder = new StringBuilder();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        DateTime dateTimeNow = new DateTime();

        int i = 0;
        for (MotherImportRejection mother: createObjects) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("(");
            stringBuilder = mctsQueryHelper(stringBuilder, mother);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }

    private String rchMotherUpdateQuerySet(List<MotherImportRejection> updateObjects) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (MotherImportRejection mother: updateObjects) {
            Long id = null;
            String creationTime = "";
            try {
                Method method = mother.getClass().getMethod("getCreationDate");
                DateTime dateTime = (DateTime) method.invoke(mother);
                method = mother.getClass().getMethod("getId");
                creationTime = dateTimeFormatter.print(dateTime);

                id = (Long) method.invoke(mother);
            } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
                    InvocationTargetException e) {
                LOGGER.error("Ignoring creation date and setting as now");
            }
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("(");
            stringBuilder.append(id + ", ");
            stringBuilder = rchQueryHelper(stringBuilder, mother);
            stringBuilder.append(MOTECH_STRING);
            stringBuilder.append(QUOTATION + creationTime + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }

    private String mctsMotherUpdateQuerySet(List<MotherImportRejection> updateObjects) {
        StringBuilder stringBuilder = new StringBuilder();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        DateTime dateTimeNow = new DateTime();
        int i = 0;
        for (MotherImportRejection mother: updateObjects) {
            String creationTime = "";
            Long id = null;
            if (i != 0) {
                stringBuilder.append(", ");
            }
            try {
                Method method = mother.getClass().getMethod("getCreationDate");
                DateTime dateTime = (DateTime) method.invoke(mother);
                creationTime = dateTimeFormatter.print(dateTime);

                method = mother.getClass().getMethod("getId");
                id = (Long) method.invoke(mother);
            } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
                    InvocationTargetException e) {
                LOGGER.error("Ignoring creation date and setting as now");
            }
            stringBuilder.append("(");
            stringBuilder.append(id + ", ");
            stringBuilder = mctsQueryHelper(stringBuilder, mother);
            stringBuilder.append(QUOTATION + creationTime + QUOTATION_COMMA);
            stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }

    private StringBuilder mctsQueryHelper(StringBuilder stringBuilder1, MotherImportRejection mother) { //NOPMD NcssMethodCount
        StringBuilder stringBuilder = addLocations(stringBuilder1, mother);
        stringBuilder.append(mother.getYr() + ", ");
        stringBuilder.append(QUOTATION + mother.getgPVillage() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getAddress() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getIdNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getHusbandName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getPhoneNumberWhom() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getMobileNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getBirthDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getjSYBeneficiary() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getCaste() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getSubcenterName1() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getaNMName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getaNMPhone() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getAshaName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getAshaPhone() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDeliveryLnkFacility() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getFacilityName() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getLmpDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getaNC1Date() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getaNC2Date() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getaNC3Date() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getaNC4Date() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.gettT1Date() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.gettT2Date() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.gettTBoosterDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getiFA100GivenDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getAnemia() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getaNCComplication() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getrTISTI() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDlyDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDlyPlaceHomeType() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDlyPlacePublic() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDlyPlacePrivate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDlyType() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDlyComplication() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDischargeDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getjSYPaidDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getAbortion() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getpNCHomeVisit() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getpNCComplication() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getpPCMethod() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getpNCCheckup() + QUOTATION_COMMA);
        stringBuilder.append(mother.getOutcomeNos() + ", ");
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getChild1Name()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getChild1Sex() + QUOTATION_COMMA);
        stringBuilder.append(mother.getChild1Wt() + ", ");
        stringBuilder.append(QUOTATION + mother.getChild1Brestfeeding() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getChild2Name()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getChild2Sex() + QUOTATION_COMMA);
        stringBuilder.append(mother.getChild2Wt() + ", ");
        stringBuilder.append(QUOTATION + mother.getChild2Brestfeeding() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getChild3Name()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getChild3Sex() + QUOTATION_COMMA);
        stringBuilder.append(mother.getChild3Wt() + ", ");
        stringBuilder.append(QUOTATION + mother.getChild3Brestfeeding() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + StringEscapeUtils.escapeSql(mother.getChild4Name()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getChild4Sex() + QUOTATION_COMMA);
        stringBuilder.append(mother.getChild4Wt() + ", ");
        stringBuilder.append(QUOTATION + mother.getChild4Brestfeeding() + QUOTATION_COMMA);
        stringBuilder.append(mother.getAge() + ", ");
        stringBuilder.append(QUOTATION + mother.getmTHRREGDATE() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getLastUpdateDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getRemarks() + QUOTATION_COMMA);
        stringBuilder.append(mother.getaNMID() + ", ");
        stringBuilder.append(mother.getaSHAID() + ", ");
        stringBuilder.append(mother.getCallAns() + ", ");
        stringBuilder.append(mother.getNoCallReason() + ", ");
        stringBuilder.append(mother.getNoPhoneReason() + ", ");
        stringBuilder.append(mother.getCreatedBy() + ", ");
        stringBuilder.append(mother.getUpdatedBy() + ", ");
        stringBuilder.append(mother.getAadharNo() + ", ");
        stringBuilder.append(mother.getbPLAPL() + ", ");
        stringBuilder.append(mother.geteID() + ", ");
        stringBuilder.append(QUOTATION + mother.geteIDTime() + QUOTATION_COMMA);
        stringBuilder.append(mother.getEntryType() + ", ");
        stringBuilder.append(QUOTATION + mother.getSource() + QUOTATION_COMMA);
        stringBuilder.append(mother.getAccepted() + ", ");
        stringBuilder.append(QUOTATION + mother.getRejectionReason() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getAction() + QUOTATION_COMMA);
        stringBuilder.append(MOTECH_STRING);
        stringBuilder.append(MOTECH_STRING);
        return stringBuilder;
    }


    private StringBuilder rchQueryHelper(StringBuilder stringBuilder1, MotherImportRejection mother) {
        StringBuilder stringBuilder = addLocations(stringBuilder1, mother);
        stringBuilder.append(QUOTATION + mother.getIdNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getRegistrationNo() + QUOTATION_COMMA);
        stringBuilder.append(mother.getCaseNo() + ", ");
        stringBuilder.append(QUOTATION + validateName(mother.getName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getMobileNo() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getRegistrationDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getLmpDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getBirthDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getAbortionType() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getDeliveryOutcomes() + QUOTATION_COMMA);
        stringBuilder.append(mother.getEntryType() + ", ");
        stringBuilder.append(QUOTATION + mother.getExecDate() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getSource() + QUOTATION_COMMA);
        stringBuilder.append(mother.getAccepted() + ", ");
        stringBuilder.append(QUOTATION + mother.getRejectionReason() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getAction() + QUOTATION_COMMA);
        stringBuilder.append(MOTECH_STRING);
        return stringBuilder;
    }

    private static String validateName (String name){
        if(name == null) return null;
        name = name.replace("?" , "").replace("\\" , "\\\\");
        return StringEscapeUtils.escapeSql(name);
    }

    private StringBuilder addLocations(StringBuilder stringBuilder, MotherImportRejection mother) {
        stringBuilder.append(mother.getStateId() + ", ");
        stringBuilder.append(mother.getDistrictId() + ", ");
        stringBuilder.append(QUOTATION + validateName(mother.getDistrictName()) + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + mother.getTalukaId() + QUOTATION_COMMA);
        stringBuilder.append(QUOTATION + validateName(mother.getTalukaName()) + QUOTATION_COMMA);
        stringBuilder.append(mother.getHealthBlockId() + ", ");
        stringBuilder.append(QUOTATION + validateName(mother.getHealthBlockName()) + QUOTATION_COMMA);
        stringBuilder.append(mother.getPhcId() + ", ");
        stringBuilder.append(QUOTATION + validateName(mother.getPhcName()) + QUOTATION_COMMA);
        stringBuilder.append(mother.getSubcentreId() + ", ");
        stringBuilder.append(QUOTATION + validateName(mother.getSubcentreName()) + QUOTATION_COMMA);
        stringBuilder.append(mother.getVillageId() + ", ");
        stringBuilder.append(QUOTATION + validateName(mother.getVillageName()) + QUOTATION_COMMA);
        return stringBuilder;
    }


    private static MotherImportRejection setNewData1(MotherImportRejection motherImportRejection, MotherImportRejection motherImportRejection1) {
        motherImportRejection1.setStateId(motherImportRejection.getStateId());
        motherImportRejection1.setDistrictId(motherImportRejection.getDistrictId());
        motherImportRejection1.setDistrictName(motherImportRejection.getDistrictName());
        motherImportRejection1.setTalukaId(motherImportRejection.getTalukaId());
        motherImportRejection1.setTalukaName(motherImportRejection.getTalukaName());
        motherImportRejection1.setHealthBlockId(motherImportRejection.getHealthBlockId());
        motherImportRejection1.setHealthBlockName(motherImportRejection.getHealthBlockName());
        motherImportRejection1.setPhcId(motherImportRejection.getPhcId());
        motherImportRejection1.setPhcName(motherImportRejection.getPhcName());
        motherImportRejection1.setSubcentreId(motherImportRejection.getSubcentreId());
        motherImportRejection1.setSubcentreName(motherImportRejection.getSubcentreName());
        motherImportRejection1.setVillageId(motherImportRejection.getVillageId());
        motherImportRejection1.setVillageName(motherImportRejection.getVillageName());
        motherImportRejection1.setYr(motherImportRejection.getYr());
        motherImportRejection1.setgPVillage(motherImportRejection.getgPVillage());
        motherImportRejection1.setAddress(motherImportRejection.getAddress());
        motherImportRejection1.setIdNo(motherImportRejection.getIdNo());
        motherImportRejection1.setName(motherImportRejection.getName());
        motherImportRejection1.setHusbandName(motherImportRejection.getHusbandName());
        motherImportRejection1.setPhoneNumberWhom(motherImportRejection.getPhoneNumberWhom());
        motherImportRejection1.setBirthDate(motherImportRejection.getBirthDate());
        motherImportRejection1.setjSYBeneficiary(motherImportRejection.getjSYBeneficiary());
        motherImportRejection1.setCaste(motherImportRejection.getCaste());
        motherImportRejection1.setSubcenterName1(motherImportRejection.getSubcenterName1());
        motherImportRejection1.setaNMName(motherImportRejection.getaNMName());
        setNewData2(motherImportRejection, motherImportRejection1);
        setNewData3(motherImportRejection, motherImportRejection1);
        return motherImportRejection1;
    }

    private static void setNewData2(MotherImportRejection motherImportRejection, MotherImportRejection motherImportRejection1) {
        motherImportRejection1.setaNMPhone(motherImportRejection.getaNMPhone());
        motherImportRejection1.setAshaName(motherImportRejection.getAshaName());
        motherImportRejection1.setAshaPhone(motherImportRejection.getAshaPhone());
        motherImportRejection1.setDeliveryLnkFacility(motherImportRejection.getDeliveryLnkFacility());
        motherImportRejection1.setFacilityName(motherImportRejection.getFacilityName());
        motherImportRejection1.setLmpDate(motherImportRejection.getLmpDate());
        motherImportRejection1.setaNC1Date(motherImportRejection.getaNC1Date());
        motherImportRejection1.setaNC2Date(motherImportRejection.getaNC2Date());
        motherImportRejection1.setaNC3Date(motherImportRejection.getaNC3Date());
        motherImportRejection1.setaNC4Date(motherImportRejection.getaNC4Date());
        motherImportRejection1.settT1Date(motherImportRejection.gettT1Date());
        motherImportRejection1.settT2Date(motherImportRejection.gettT2Date());
        motherImportRejection1.settTBoosterDate(motherImportRejection.gettTBoosterDate());
        motherImportRejection1.setiFA100GivenDate(motherImportRejection.getiFA100GivenDate());
        motherImportRejection1.setAnemia(motherImportRejection.getAnemia());
        motherImportRejection1.setaNCComplication(motherImportRejection.getaNCComplication());
        motherImportRejection1.setrTISTI(motherImportRejection.getrTISTI());
        motherImportRejection1.setDlyDate(motherImportRejection.getDlyDate());
        motherImportRejection1.setDlyPlaceHomeType(motherImportRejection.getDlyPlaceHomeType());
        motherImportRejection1.setDlyPlacePublic(motherImportRejection.getDlyPlacePublic());
        motherImportRejection1.setDlyPlacePrivate(motherImportRejection.getDlyPlacePrivate());
        motherImportRejection1.setDlyType(motherImportRejection.getDlyType());
        motherImportRejection1.setDlyComplication(motherImportRejection.getDlyComplication());
        motherImportRejection1.setDischargeDate(motherImportRejection.getDischargeDate());
        motherImportRejection1.setjSYPaidDate(motherImportRejection.getjSYPaidDate());
        motherImportRejection1.setAbortion(motherImportRejection.getAbortion());
    }

    private static void setNewData3(MotherImportRejection motherImportRejection, MotherImportRejection motherImportRejection1) {
        motherImportRejection1.setpNCHomeVisit(motherImportRejection.getpNCHomeVisit());
        motherImportRejection1.setpNCComplication(motherImportRejection.getpNCComplication());
        motherImportRejection1.setpPCMethod(motherImportRejection.getpPCMethod());
        motherImportRejection1.setpNCCheckup(motherImportRejection.getpNCCheckup());
        motherImportRejection1.setOutcomeNos(motherImportRejection.getOutcomeNos());
        motherImportRejection1.setChild1Name(motherImportRejection.getChild1Name());
        motherImportRejection1.setChild1Sex(motherImportRejection.getChild1Sex());
        motherImportRejection1.setChild1Wt(motherImportRejection.getChild1Wt());
        motherImportRejection1.setChild1Brestfeeding(motherImportRejection.getChild1Brestfeeding());
        motherImportRejection1.setChild2Name(motherImportRejection.getChild2Name());
        motherImportRejection1.setChild2Sex(motherImportRejection.getChild2Sex());
        motherImportRejection1.setChild2Wt(motherImportRejection.getChild2Wt());
        motherImportRejection1.setChild2Brestfeeding(motherImportRejection.getChild2Brestfeeding());
        motherImportRejection1.setChild3Name(motherImportRejection.getChild3Name());
        motherImportRejection1.setChild3Sex(motherImportRejection.getChild3Sex());
        motherImportRejection1.setChild3Wt(motherImportRejection.getChild3Wt());
        motherImportRejection1.setChild3Brestfeeding(motherImportRejection.getChild3Brestfeeding());
        motherImportRejection1.setChild4Name(motherImportRejection.getChild4Name());
        motherImportRejection1.setChild4Sex(motherImportRejection.getChild4Sex());
        motherImportRejection1.setChild4Wt(motherImportRejection.getChild4Wt());
        motherImportRejection1.setChild4Brestfeeding(motherImportRejection.getChild4Brestfeeding());
        motherImportRejection1.setAge(motherImportRejection.getAge());
        motherImportRejection1.setmTHRREGDATE(motherImportRejection.getmTHRREGDATE());
        motherImportRejection1.setLastUpdateDate(motherImportRejection.getLastUpdateDate());
        motherImportRejection1.setRemarks(motherImportRejection.getRemarks());
        motherImportRejection1.setaNMID(motherImportRejection.getaNMID());
        motherImportRejection1.setaSHAID(motherImportRejection.getaSHAID());
        motherImportRejection1.setCallAns(motherImportRejection.getCallAns());
        motherImportRejection1.setNoCallReason(motherImportRejection.getNoCallReason());
        motherImportRejection1.setNoPhoneReason(motherImportRejection.getNoPhoneReason());
        motherImportRejection1.setCreatedBy(motherImportRejection.getCreatedBy());
        motherImportRejection1.setUpdatedBy(motherImportRejection.getUpdatedBy());
        motherImportRejection1.setAadharNo(motherImportRejection.getAadharNo());
        motherImportRejection1.setbPLAPL(motherImportRejection.getbPLAPL());
        motherImportRejection1.seteID(motherImportRejection.geteID());
        motherImportRejection1.seteIDTime(motherImportRejection.geteIDTime());
        motherImportRejection1.setEntryType(motherImportRejection.getEntryType());
        motherImportRejection1.setRegistrationNo(motherImportRejection.getRegistrationNo());
        motherImportRejection1.setCaseNo(motherImportRejection.getCaseNo());
        motherImportRejection1.setMobileNo(motherImportRejection.getMobileNo());
        motherImportRejection1.setAbortionType(motherImportRejection.getAbortionType());
        motherImportRejection1.setDeliveryOutcomes(motherImportRejection.getDeliveryOutcomes());
        motherImportRejection1.setExecDate(motherImportRejection.getExecDate());
        motherImportRejection1.setAccepted(motherImportRejection.getAccepted());
        motherImportRejection1.setRejectionReason(motherImportRejection.getRejectionReason());
        motherImportRejection1.setSource(motherImportRejection.getSource());
        motherImportRejection1.setAction(motherImportRejection.getAction());
    }

}
