package org.motechproject.nms.mcts.service.impl;

import org.apache.commons.io.IOUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.LocalDate;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.contract.AnmAshaRecord;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.utils.FlwConstants;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MothersDataSet;
import org.motechproject.nms.mcts.domain.MctsUserType;
import org.motechproject.nms.mcts.exception.MctsInvalidResponseStructureException;
import org.motechproject.nms.mcts.exception.MctsWebServiceException;
import org.motechproject.nms.mcts.repository.MctsImportAuditDataService;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.mcts.soap.DS_GetChildDataResponseDS_GetChildDataResult;
import org.motechproject.nms.mcts.soap.DS_GetMotherDataResponseDS_GetMotherDataResult;
import org.motechproject.nms.mcts.soap.DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult;
import org.motechproject.nms.mcts.soap.IMctsService;
import org.motechproject.nms.mcts.soap.MctsServiceLocator;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.mcts.utils.MarshallUtils;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.jdo.Query;
import javax.xml.bind.JAXBException;
import javax.xml.rpc.ServiceException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToChild;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToMother;
import static org.motechproject.nms.mcts.utils.Constants.REMOTE_RESPONSE_DIR_CSV;

@Service("mctsWebServiceFacade")
public class MctsWebServiceFacadeImpl implements MctsWebServiceFacade {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String LOC_UPDATE_DIR = "mcts.loc_update_dir";
    private static final String NULL = "NULL";

    private static final String QUOTATION = "'";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";

    @Autowired
    @Qualifier("mctsSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    @Qualifier("mctsServiceLocator")
    private MctsServiceLocator mctsServiceLocator;

    @Autowired
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private MctsBeneficiaryImportReaderService mctsBeneficiaryImportReaderService;

    @Autowired
    private FrontLineWorkerImportService frontLineWorkerImportService;

    @Autowired
    private MctsImportAuditDataService mctsImportAuditDataService;




    private static final Logger LOGGER = LoggerFactory.getLogger(MctsWebServiceFacadeImpl.class);
    private static final String NEXT_LINE = "\r\n";
    private static final String TAB = "\t";


    @Override
    public ChildrenDataSet getChildrenData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_GetChildDataResponseDS_GetChildDataResult result;
        IMctsService dataService = getService(endpoint);

        try {
            result = dataService.DS_GetChildData(settingsFacade.getProperty(Constants.MCTS_USER_ID), settingsFacade.getProperty(Constants.MCTS_PASSWORD),
                    from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(), settingsFacade.getProperty(Constants.MCTS_DID), settingsFacade.getProperty(Constants.MCTS_PID));
        } catch (RemoteException e) {
            throw new MctsWebServiceException("Remote Server Error. Cannot read Children data.", e);
        }

        try {
            validChildrenDataResponse(result, stateId);
            List childrenResultFeed = result.get_any()[1].getChildren();
            return (childrenResultFeed == null) ?
                null :
                (ChildrenDataSet) MarshallUtils.unmarshall(childrenResultFeed.get(0).toString(), ChildrenDataSet.class);
        } catch (JAXBException e) {
            throw new MctsInvalidResponseStructureException(String.format("Cannot deserialize children data from %s location.", stateId), e);
        }
    }

    @Override
    public MothersDataSet getMothersData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_GetMotherDataResponseDS_GetMotherDataResult result;
        IMctsService dataService = getService(endpoint);

        try {
            result = dataService.DS_GetMotherData(settingsFacade.getProperty(Constants.MCTS_USER_ID), settingsFacade.getProperty(Constants.MCTS_PASSWORD),
                    from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(), settingsFacade.getProperty(Constants.MCTS_DID), settingsFacade.getProperty(Constants.MCTS_PID));
        } catch (RemoteException e) {
            throw new MctsWebServiceException("Remote Server Error. Cannot read Mother data.", e);
        }

        try {
            validMothersDataResponse(result, stateId);
            List motherResultFeed = result.get_any()[1].getChildren();
            return (motherResultFeed == null) ?
                    null :
                    (MothersDataSet) MarshallUtils.unmarshall(motherResultFeed.get(0).toString(), MothersDataSet.class);
        } catch (JAXBException e) {
            throw new MctsInvalidResponseStructureException(String.format("Cannot deserialize mothers data from %s location.", stateId), e);
        }
    }

    @Override
    public AnmAshaDataSet getAnmAshaData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult result;
        IMctsService dataService = getService(endpoint);

        try {
            result = dataService.DS_GetAnmAshaData(settingsFacade.getProperty(Constants.MCTS_USER_ID), settingsFacade.getProperty(Constants.MCTS_PASSWORD),
                    from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(), settingsFacade.getProperty(Constants.MCTS_DID), settingsFacade.getProperty(Constants.MCTS_PID));
        } catch (RemoteException e) {
            throw new MctsWebServiceException("Remote Server Error. Cannot read ANM ASHA data.", e);
        }

        try {
            validAnmAshaDataResponse(result, stateId);
            List ashaResultFeed = result.get_any()[1].getChildren();
            return (ashaResultFeed == null) ?
                    null :
                    (AnmAshaDataSet) MarshallUtils.unmarshall(ashaResultFeed.get(0).toString(), AnmAshaDataSet.class);
        } catch (JAXBException e) {
            throw new MctsInvalidResponseStructureException(String.format("Cannot deserialize ANM ASHA data from %s location.", stateId), e);
        }
    }

    @Override
    @Transactional
    public void locationUpdateInTableFromCsv(Long stateId, MctsUserType mctsUserType) throws IOException {

        List<MultipartFile> mctsImportFiles = findByStateIdAndMctsUserType(stateId, mctsUserType);

        Collections.sort(mctsImportFiles, new Comparator<MultipartFile>() {
            public int compare(MultipartFile m1, MultipartFile m2) {
                Date file1Date;
                Date file2Date;
                int flag = 1;
                try {
                    file1Date = getDateFromFileName(m1.getOriginalFilename());
                    file2Date = getDateFromFileName(m2.getOriginalFilename());
                    flag = file1Date.compareTo(file2Date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return flag; //ascending order
            }
        });

        for (MultipartFile mctsImportFile : mctsImportFiles) {
            try (InputStream in = mctsImportFile.getInputStream()) {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                Map<String, CellProcessor> cellProcessorMapper;
                List<Map<String, Object>> recordList;
                LOGGER.debug("Started reading file {}.", mctsImportFile.getOriginalFilename());
                if (mctsUserType == MctsUserType.MOTHER) {
                    cellProcessorMapper = mctsBeneficiaryImportService.getMotherProcessorMapping();
                    recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    motherLocUpdateFromCsv(recordList, stateId, mctsUserType);
                } else if (mctsUserType == MctsUserType.CHILD) {
                    cellProcessorMapper = mctsBeneficiaryImportReaderService.getRchChildProcessorMapping();
                    recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    childLocUpdateFromCsv(recordList, stateId, mctsUserType);
                } else if (mctsUserType == MctsUserType.ASHA) {
                    cellProcessorMapper = mctsBeneficiaryImportService.getRchAshaProcessorMapping();
                    recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    ashaLocUpdateFromCsv(recordList, stateId, mctsUserType);
                }

            }
        }
    }

    @Override
    public String getBeneficiaryLocationUpdateDirectory() {
        return settingsFacade.getProperty(LOC_UPDATE_DIR);
    }

    private IMctsService getService(URL endpoint) {
        try {
            if (endpoint != null) {
                return mctsServiceLocator.getbasicEndpoint(endpoint);
            } else {
                return mctsServiceLocator.getbasicEndpoint();
            }
        } catch (ServiceException e) {
            throw new MctsWebServiceException("Cannot retrieve MCTS Service for the endpoint", e);
        }
    }

    private void validMothersDataResponse(DS_GetMotherDataResponseDS_GetMotherDataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new MctsInvalidResponseStructureException("Invalid mothers data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new MctsInvalidResponseStructureException("Invalid mothers data response " + stateId);
        }
    }

    private void validChildrenDataResponse(DS_GetChildDataResponseDS_GetChildDataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new MctsInvalidResponseStructureException("Invalid children data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new MctsInvalidResponseStructureException("Invalid children data response " + stateId);
        }
    }

    private void validAnmAshaDataResponse(DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new MctsInvalidResponseStructureException("Invalid anm asha data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new MctsInvalidResponseStructureException("Invalid anm asha data response " + stateId);
        }
    }

    private  List<MultipartFile> findByStateIdAndMctsUserType(Long stateId, MctsUserType mctsUserType) throws IOException {

        ArrayList <MultipartFile> csvFilesByStateIdAndMctsUserType = new ArrayList<>();
        String locUpdateDir = settingsFacade.getProperty(REMOTE_RESPONSE_DIR_CSV);
        File file = new File(locUpdateDir);

        File[] files = file.listFiles();
        if (files != null) {
            for(File f: files){
                String[] fileNameSplitter =  f.getName().split("_");
                if(Objects.equals(fileNameSplitter[2], stateId.toString()) && fileNameSplitter[3].equalsIgnoreCase(mctsUserType.toString())){
                    try {
                        FileInputStream input = new FileInputStream(f);
                        MultipartFile multipartFile = new MockMultipartFile("file",
                                f.getName(), "text/plain", IOUtils.toByteArray(input));
                        csvFilesByStateIdAndMctsUserType.add(multipartFile);
                    }catch(IOException e) {
                        LOGGER.debug("IO Exception", e);
                    }

                }
            }
        }

        return csvFilesByStateIdAndMctsUserType;

    }

    private void motherLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, MctsUserType mctsUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            List<MotherRecord> motherRecords = new ArrayList<>();

            for (Map<String, Object> record : result) {
                MotherRecord motherRecord = convertMapToMother(record);
                motherRecords.add(motherRecord);
            }
            List<String> existingMotherIds = getDatabaseMothers(motherRecords);
            for(MotherRecord motherRecord : motherRecords) {
                if (existingMotherIds.contains(motherRecord.getIdNo())) {
                    Map<String, Object> locMap = new HashMap<>();
                    this.toMapLocMother(locMap, motherRecord);
                    locMap.put(KilkariConstants.BENEFICIARY_ID, motherRecord.getIdNo());
                    locArrList.add(locMap);
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, mctsUserType);
            }

        } catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
        }
    }

    @Override
    public void toMapLocMother(Map<String, Object> map, MotherRecord motherRecord) {
        map.put(KilkariConstants.STATE_ID, motherRecord.getStateId());
        map.put(KilkariConstants.DISTRICT_ID, motherRecord.getDistrictId());
        map.put(KilkariConstants.DISTRICT_NAME, motherRecord.getDistrictName());
        map.put(KilkariConstants.TALUKA_ID, motherRecord.getTalukaId());
        map.put(KilkariConstants.TALUKA_NAME, motherRecord.getTalukaName());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, motherRecord.getHealthBlockId());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, motherRecord.getHealthBlockName());
        map.put(KilkariConstants.PHC_ID, motherRecord.getPhcid());
        map.put(KilkariConstants.PHC_NAME, motherRecord.getPhcName());
        map.put(KilkariConstants.SUB_CENTRE_ID, motherRecord.getSubCentreid());
        map.put(KilkariConstants.SUB_CENTRE_NAME, motherRecord.getSubCentreName());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, motherRecord.getVillageId());
        map.put(KilkariConstants.VILLAGE_NAME, motherRecord.getVillageName());
    }

    @Override
    public void toMapLocChild(Map<String, Object> map, ChildRecord childRecord) {
        map.put(KilkariConstants.STATE_ID, childRecord.getStateID());
        map.put(KilkariConstants.DISTRICT_ID, childRecord.getDistrictId());
        map.put(KilkariConstants.DISTRICT_NAME, childRecord.getDistrictName());
        map.put(KilkariConstants.TALUKA_ID, childRecord.getTalukaId());
        map.put(KilkariConstants.TALUKA_NAME, childRecord.getTalukaName());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, childRecord.getHealthBlockId());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, childRecord.getHealthBlockName());
        map.put(KilkariConstants.PHC_ID, childRecord.getPhcId());
        map.put(KilkariConstants.PHC_NAME, childRecord.getPhcName());
        map.put(KilkariConstants.SUB_CENTRE_ID, childRecord.getSubCentreId());
        map.put(KilkariConstants.SUB_CENTRE_NAME, childRecord.getSubCentreName());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, childRecord.getVillageId());
        map.put(KilkariConstants.VILLAGE_NAME, childRecord.getVillageName());
    }

    private void childLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, MctsUserType mctsUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            List<ChildRecord> childRecords = new ArrayList<>();

            for (Map<String, Object> record : result) {
                ChildRecord childRecord = convertMapToChild(record);
                childRecords.add(childRecord);
            }
            List<String> existinChildIds = getDatabaseChild(childRecords);
            for(ChildRecord childRecord : childRecords) {
                if (existinChildIds.contains(childRecord.getIdNo())) {
                    Map<String, Object> locMap = new HashMap<>();
                    this.toMapLocChild(locMap, childRecord);
                    locMap.put(KilkariConstants.BENEFICIARY_ID, childRecord.getIdNo());
                    locArrList.add(locMap);
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, mctsUserType);
            }

        } catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
        }
    }

    private void ashaLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, MctsUserType mctsUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            List<AnmAshaRecord> anmAshaRecords = new ArrayList<>();

            for (Map<String, Object> record : result) {
                AnmAshaRecord anmAshaRecord = frontLineWorkerImportService.convertMapToAsha(record);
                anmAshaRecords.add(anmAshaRecord);
            }
            List<String> existingAshaIds = getDatabaseAsha(anmAshaRecords);
            for(AnmAshaRecord anmAshaRecord : anmAshaRecords) {
                if (existingAshaIds.contains( anmAshaRecord.getId().toString())) {
                    Map<String, Object> locMap = new HashMap<>();
                    toMapLocAsha(locMap, anmAshaRecord);
                    locMap.put(FlwConstants.ID, anmAshaRecord.getId());
                    locArrList.add(locMap);
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, mctsUserType);
            }

        } catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
        }
    }

    private void updateLocInMap(List<Map<String, Object>> locArrList, Long stateId, MctsUserType mctsUserType) throws InvalidLocationException, IOException {

        ArrayList<Map<String, Object>> updatedLocArrList = new ArrayList<>();

        LocationFinder locationFinder = locationService.updateLocations(locArrList);

        for (Map<String, Object> record : locArrList
                ) {
            Map<String, Object> updatedMap = setLocationFields(locationFinder, record);
            updatedMap.put(KilkariConstants.BENEFICIARY_ID, record.get(KilkariConstants.BENEFICIARY_ID));
            updatedLocArrList.add(updatedMap);
        }

        if ("asha".equalsIgnoreCase(mctsUserType.toString())) {
            csvWriterAsha(updatedLocArrList, stateId, mctsUserType);
        }else {
            csvWriterKilkari(updatedLocArrList, stateId, mctsUserType);
        }
    }

    public Map<String, Object> setLocationFields(LocationFinder locationFinder, Map<String, Object> record) throws InvalidLocationException { //NO CHECKSTYLE Cyclomatic Complexity

        Map<String, Object> updatedLoc = new HashMap<>();
        String mapKey = record.get(KilkariConstants.STATE_ID).toString();
        if (isValidID(record, KilkariConstants.STATE_ID) && (locationFinder.getStateHashMap().get(mapKey) != null)) {
            updatedLoc.put(KilkariConstants.STATE_ID, locationFinder.getStateHashMap().get(mapKey).getId());
            String districtCode = record.get(KilkariConstants.DISTRICT_ID).toString();
            mapKey += "_";
            mapKey += districtCode;

            if (isValidID(record, KilkariConstants.DISTRICT_ID) && (locationFinder.getDistrictHashMap().get(mapKey) != null)) {
                updatedLoc.put(KilkariConstants.DISTRICT_ID, locationFinder.getDistrictHashMap().get(mapKey).getId());
                updatedLoc.put(KilkariConstants.DISTRICT_NAME, locationFinder.getDistrictHashMap().get(mapKey).getName());
                Long talukaCode = Long.parseLong(record.get(KilkariConstants.TALUKA_ID).toString());
                mapKey += "_";
                mapKey += talukaCode;
                Taluka taluka = locationFinder.getTalukaHashMap().get(mapKey);
                updatedLoc.put(KilkariConstants.TALUKA_ID, taluka == null ? null : taluka.getId());
                updatedLoc.put(KilkariConstants.TALUKA_NAME, taluka == null ? null : taluka.getName());

                String villageSvid = record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.NON_CENSUS_VILLAGE_ID).toString();
                String villageCode = record.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? "0" : record.get(KilkariConstants.CENSUS_VILLAGE_ID).toString();
                String healthBlockCode = record.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? "0" : record.get(KilkariConstants.HEALTH_BLOCK_ID).toString();
                String healthFacilityCode = record.get(KilkariConstants.PHC_ID) == null ? "0" : record.get(KilkariConstants.PHC_ID).toString();
                String healthSubFacilityCode = record.get(KilkariConstants.SUB_CENTRE_ID) == null ? "0" : record.get(KilkariConstants.SUB_CENTRE_ID).toString();

                Village village = locationFinder.getVillageHashMap().get(mapKey + "_" + Long.parseLong(villageCode) + "_" + Long.parseLong(villageSvid));
                updatedLoc.put(KilkariConstants.CENSUS_VILLAGE_ID, village == null ? null : village.getId());
                updatedLoc.put(KilkariConstants.VILLAGE_NAME, village == null ? null : village.getName());
                mapKey += "_";
                mapKey += Long.parseLong(healthBlockCode);
                HealthBlock healthBlock = locationFinder.getHealthBlockHashMap().get(mapKey);
                updatedLoc.put(KilkariConstants.HEALTH_BLOCK_ID, healthBlock == null ? null : healthBlock.getId());
                updatedLoc.put(KilkariConstants.HEALTH_BLOCK_NAME, healthBlock == null ? null : healthBlock.getName());
                mapKey += "_";
                mapKey += Long.parseLong(healthFacilityCode);
                HealthFacility healthFacility = locationFinder.getHealthFacilityHashMap().get(mapKey);
                updatedLoc.put(KilkariConstants.PHC_ID, healthFacility == null ? null : healthFacility.getId());
                updatedLoc.put(KilkariConstants.PHC_NAME, healthFacility == null ? null : healthFacility.getName());
                mapKey += "_";
                mapKey += Long.parseLong(healthSubFacilityCode);
                HealthSubFacility healthSubFacility = locationFinder.getHealthSubFacilityHashMap().get(mapKey);
                updatedLoc.put(KilkariConstants.SUB_CENTRE_ID, healthSubFacility == null ? null : healthSubFacility.getId());
                updatedLoc.put(KilkariConstants.SUB_CENTRE_NAME, healthSubFacility == null ? null : healthSubFacility.getName());
                return updatedLoc;
            } else {
                throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.DISTRICT_ID, record.get(KilkariConstants.DISTRICT_ID)));
            }
        } else {
            throw new InvalidLocationException(String.format(KilkariConstants.INVALID_LOCATION, KilkariConstants.STATE_ID, record.get(KilkariConstants.STATE_ID)));
        }
    }

    private boolean isValidID(final Map<String, Object> map, final String key) {
        Object obj = map.get(key);
        if (obj == null || obj.toString().isEmpty() || "NULL".equalsIgnoreCase(obj.toString())) {
            return false;
        }

        if (obj.getClass().equals(Long.class)) {
            return (Long) obj > 0L;
        }

        return !"0".equals(obj);
    }

    private void toMapLocAsha(Map<String, Object> map, AnmAshaRecord anmAshaRecord) {
        map.put(KilkariConstants.STATE_ID, anmAshaRecord.getStateId());
        map.put(KilkariConstants.DISTRICT_ID, anmAshaRecord.getDistrictId());
        map.put(KilkariConstants.DISTRICT_NAME, anmAshaRecord.getDistrictName());
        map.put(KilkariConstants.TALUKA_ID, anmAshaRecord.getTalukaId());
        map.put(KilkariConstants.TALUKA_NAME, anmAshaRecord.getTalukaName());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, anmAshaRecord.getHealthBlockId());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, anmAshaRecord.getHealthBlockName());
        map.put(KilkariConstants.PHC_ID, anmAshaRecord.getPhcId());
        map.put(KilkariConstants.PHC_NAME, anmAshaRecord.getPhcName());
        map.put(KilkariConstants.SUB_CENTRE_ID, anmAshaRecord.getSubCentreId());
        map.put(KilkariConstants.SUB_CENTRE_NAME, anmAshaRecord.getSubCentreName());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, anmAshaRecord.getVillageId());
        map.put(KilkariConstants.VILLAGE_NAME, anmAshaRecord.getVillageName());
    }

    private File csvWriter(Long stateId, MctsUserType mctsUserType) throws IOException {
        String locUpdateDir = settingsFacade.getProperty(LOC_UPDATE_DIR);
        String fileName = locUpdateDir + "location_update_state" + "_" + stateId + "_" + mctsUserType + "_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ".csv";
        File csvFile = new File(fileName);
        if (!csvFile.exists()){
            csvFile.createNewFile();
        } else {
            LOGGER.debug("File already exists");
        }
        return csvFile;

    }

    private void csvWriterKilkari(List<Map<String, Object>> locArrList, Long stateId, MctsUserType mctsUserType) throws IOException { //NO CHECKSTYLE Cyclomatic Complexity //NOPMD NcssMethodCount

        if (!locArrList.isEmpty()) {
            File csvFile = csvWriter(stateId, mctsUserType);
            FileWriter writer;
            writer = new FileWriter(csvFile, true);

            writer.write(KilkariConstants.BENEFICIARY_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.STATE_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.DISTRICT_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.DISTRICT_NAME);
            writer.write(TAB);
            writer.write(KilkariConstants.TALUKA_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.TALUKA_NAME);
            writer.write(TAB);
            writer.write(KilkariConstants.HEALTH_BLOCK_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.HEALTH_BLOCK_NAME);
            writer.write(TAB);
            writer.write(KilkariConstants.PHC_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.PHC_NAME);
            writer.write(TAB);
            writer.write(KilkariConstants.SUB_CENTRE_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.SUB_CENTRE_NAME);
            writer.write(TAB);
            writer.write(KilkariConstants.CENSUS_VILLAGE_ID);
            writer.write(TAB);
            writer.write(KilkariConstants.VILLAGE_NAME);
            writer.write(NEXT_LINE);

            for (Map<String, Object> map : locArrList
                    ) {
                writer.write(map.get(KilkariConstants.BENEFICIARY_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.STATE_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.DISTRICT_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.DISTRICT_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.TALUKA_ID) == null ? "" : map.get(KilkariConstants.TALUKA_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.TALUKA_NAME) == null ? "" : map.get(KilkariConstants.TALUKA_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? "" : map.get(KilkariConstants.HEALTH_BLOCK_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.HEALTH_BLOCK_NAME) == null ? "" : map.get(KilkariConstants.HEALTH_BLOCK_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.PHC_ID) == null ? "" : map.get(KilkariConstants.PHC_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.PHC_NAME) == null ? "" : map.get(KilkariConstants.PHC_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.SUB_CENTRE_ID) == null ? "" : map.get(KilkariConstants.SUB_CENTRE_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.SUB_CENTRE_NAME) == null ? "" : map.get(KilkariConstants.SUB_CENTRE_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? "" : map.get(KilkariConstants.CENSUS_VILLAGE_ID).toString());
                writer.write(TAB);
                writer.write(map.get(KilkariConstants.VILLAGE_NAME) == null ? "" : map.get(KilkariConstants.VILLAGE_NAME).toString());
                writer.write(NEXT_LINE);
            }

            writer.close();
        }
    }

    private void csvWriterAsha(List<Map<String, Object>> locArrList, Long stateId, MctsUserType mctsUserType) throws IOException { //NO CHECKSTYLE Cyclomatic Complexity //NOPMD NcssMethodCount

        if (!locArrList.isEmpty()) {
            File csvFile = csvWriter(stateId, mctsUserType);
            FileWriter writer;
            writer = new FileWriter(csvFile, true);

            writer.write(FlwConstants.ID);
            writer.write(TAB);
            writer.write(FlwConstants.STATE_ID);
            writer.write(TAB);
            writer.write(FlwConstants.DISTRICT_ID);
            writer.write(TAB);
            writer.write(FlwConstants.DISTRICT_NAME);
            writer.write(TAB);
            writer.write(FlwConstants.TALUKA_ID);
            writer.write(TAB);
            writer.write(FlwConstants.TALUKA_NAME);
            writer.write(TAB);
            writer.write(FlwConstants.HEALTH_BLOCK_ID);
            writer.write(TAB);
            writer.write(FlwConstants.HEALTH_BLOCK_NAME);
            writer.write(TAB);
            writer.write(FlwConstants.PHC_ID);
            writer.write(TAB);
            writer.write(FlwConstants.PHC_NAME);
            writer.write(TAB);
            writer.write(FlwConstants.SUB_CENTRE_ID);
            writer.write(TAB);
            writer.write(FlwConstants.SUB_CENTRE_NAME);
            writer.write(TAB);
            writer.write(FlwConstants.CENSUS_VILLAGE_ID);
            writer.write(TAB);
            writer.write(FlwConstants.VILLAGE_NAME);
            writer.write(NEXT_LINE);

            for (Map<String, Object> map : locArrList
                    ) {
                writer.write(map.get(FlwConstants.ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.STATE_ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.DISTRICT_ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.DISTRICT_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.TALUKA_ID) == null ? "" : map.get(FlwConstants.TALUKA_ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.TALUKA_NAME) == null ? "" : map.get(FlwConstants.TALUKA_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.HEALTH_BLOCK_ID) == null ? "" : map.get(FlwConstants.HEALTH_BLOCK_ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.HEALTH_BLOCK_NAME) == null ? "" : map.get(FlwConstants.HEALTH_BLOCK_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.PHC_ID) == null ? "" : map.get(FlwConstants.PHC_ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.PHC_NAME) == null ? "" : map.get(FlwConstants.PHC_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.SUB_CENTRE_ID) == null ? "" : map.get(FlwConstants.SUB_CENTRE_ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.SUB_CENTRE_NAME) == null ? "" : map.get(FlwConstants.SUB_CENTRE_NAME).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.CENSUS_VILLAGE_ID) == null ? "" : map.get(FlwConstants.CENSUS_VILLAGE_ID).toString());
                writer.write(TAB);
                writer.write(map.get(FlwConstants.VILLAGE_NAME) == null ? NULL : map.get(FlwConstants.VILLAGE_NAME).toString());
                writer.write(NEXT_LINE);
            }

            writer.close();
        }
    }


    private List<String> getDatabaseMothers(final List<MotherRecord> motherRecords) {
        org.motechproject.metrics.service.Timer queryTimer = new org.motechproject.metrics.service.Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<String>> queryExecution = new SqlQueryExecution<List<String>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT beneficiaryId FROM nms_mcts_mothers WHERE beneficiaryId IN " + queryIdList(motherRecords);
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public List<String> execute(Query query) {

                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<String> result = new ArrayList<>();
                for (String existingMotherId : (List<String>) fqr) {
                    result.add(existingMotherId);
                }
                return result;
            }
        };

        List<String> result = (List<String>) mctsImportAuditDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Database mothers query time {}", queryTimer.time());
        return result;

    }

    private String queryIdList(List<MotherRecord> motherRecords) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append("(");
        for (MotherRecord motherRecord: motherRecords) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(QUOTATION + motherRecord.getIdNo() + QUOTATION);
            i++;
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }



    private List<String> getDatabaseChild(final List<ChildRecord> childRecords) {
        org.motechproject.metrics.service.Timer queryTimer = new org.motechproject.metrics.service.Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<String>> queryExecution = new SqlQueryExecution<List<String>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT beneficiaryId FROM nms_mcts_children WHERE beneficiaryId IN " + queryIdListChildren(childRecords);
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public List<String> execute(Query query) {

                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<String> result = new ArrayList<>();
                for (String existingChildId : (List<String>) fqr) {
                    result.add(existingChildId);
                }
                return result;
            }
        };

        List<String> result = (List<String>) mctsImportAuditDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Database child query time {}", queryTimer.time());
        return result;

    }

    private String queryIdListChildren(List<ChildRecord> childRecords) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append("(");
        for (ChildRecord childRecord: childRecords) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(QUOTATION + childRecord.getIdNo() + QUOTATION);
            i++;
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }


    private List<String> getDatabaseAsha(final List<AnmAshaRecord> ashaRecords) {
        org.motechproject.metrics.service.Timer queryTimer = new org.motechproject.metrics.service.Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<String>> queryExecution = new SqlQueryExecution<List<String>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT mctsFlwId FROM nms_front_line_workers WHERE mctsFlwId IN " + queryIdListAsha(ashaRecords);
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public List<String> execute(Query query) {

                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<String> result = new ArrayList<>();
                for (String existingAshaId : (List<String>) fqr) {
                    result.add(existingAshaId);
                }
                return result;
            }
        };

        List<String> result = (List<String>) mctsImportAuditDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Database asha's query time {}", queryTimer.time());
        return result;

    }

    private String queryIdListAsha(List<AnmAshaRecord> ashaRecords) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append("(");
        for (AnmAshaRecord ashaRecord: ashaRecords) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(QUOTATION + ashaRecord.getId() + QUOTATION);
            i++;
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    private Date getDateFromFileName(String fileName) throws ParseException {
        String[] names = fileName.split("_");
        String dateString = names[5].split(".csv")[0];
        Date date = new SimpleDateFormat(DATE_FORMAT).parse(dateString);
        return date;
    }

}
