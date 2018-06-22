package org.motechproject.nms.mcts.service.impl;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.joda.time.LocalDate;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.contract.AnmAshaRecord;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MothersDataSet;
import org.motechproject.nms.mcts.domain.MctsUserType;
import org.motechproject.nms.mcts.exception.MctsInvalidResponseStructureException;
import org.motechproject.nms.mcts.exception.MctsWebServiceException;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.soap.DS_GetChildDataResponseDS_GetChildDataResult;
import org.motechproject.nms.mcts.soap.DS_GetMotherDataResponseDS_GetMotherDataResult;
import org.motechproject.nms.mcts.soap.DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult;
import org.motechproject.nms.mcts.soap.IMctsService;
import org.motechproject.nms.mcts.soap.MctsServiceLocator;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.mcts.utils.MarshallUtils;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToChild;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToMother;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchMother;

@Service("mctsWebServiceFacade")
public class MctsWebServiceFacadeImpl implements MctsWebServiceFacade {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String LOC_UPDATE_DIR = "mcts.loc_update_dir";
    private static final String NULL = "NULL";

    @Autowired
    @Qualifier("mctsSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    @Qualifier("mctsServiceLocator")
    private MctsServiceLocator mctsServiceLocator;

    @Autowired
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    @Autowired
    private MctsWsImportService mctsWsImportService;

    @Autowired
    private LocationService locationService;


    @Autowired
    private MctsBeneficiaryImportReaderService mctsBeneficiaryImportReaderService;

    @Autowired
    private FrontLineWorkerImportService frontLineWorkerImportService;




    private static final Logger LOGGER = LoggerFactory.getLogger(MctsWebServiceFacadeImpl.class);

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
        for (MultipartFile mctsImportFile : mctsImportFiles) {
            try (InputStream in = mctsImportFile.getInputStream()) {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                Map<String, CellProcessor> cellProcessorMapper;

                if (mctsUserType == MctsUserType.MOTHER) {
                    cellProcessorMapper = mctsBeneficiaryImportService.getMotherProcessorMapping();
                    List<Map<String, Object>> recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    motherLocUpdateFromCsv(recordList, stateId, mctsUserType);
                } else if (mctsUserType == MctsUserType.CHILD) {
                    cellProcessorMapper = mctsBeneficiaryImportReaderService.getRchChildProcessorMapping();
                    List<Map<String, Object>> recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    childLocUpdateFromCsv(recordList, stateId, mctsUserType);
                } else if (mctsUserType == MctsUserType.ASHA) {
                    cellProcessorMapper = mctsBeneficiaryImportService.getRchAshaProcessorMapping();
                    List<Map<String, Object>> recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    ashaLocUpdateFromCsv(recordList, stateId, mctsUserType);
                }

            }
        }
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

        ArrayList<MultipartFile> csvFilesByStateIdAndMctsUserType = new ArrayList<>();
        String locUpdateDir = settingsFacade.getProperty(Constants.REMOTE_RESPONSE_DIR_CSV);
        File file = new File(locUpdateDir);

        File[] files = file.listFiles();
        if (files != null) {
            for(File f: files){
                String[] fileNameSplitter =  f.getName().split("_");
                if(Objects.equals(fileNameSplitter[2], stateId.toString()) && fileNameSplitter[3].equalsIgnoreCase(mctsUserType.toString())){
                    try {
                        DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false,
                                f.getName(), (int) f.length() , f.getParentFile());
                        fileItem.getOutputStream();
                        MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
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

            for (Map<String, Object> record : result){
                MotherRecord motherRecord =  convertMapToMother(record);
                Map<String, Object> locMap = new HashMap<>();
                mctsWsImportService.toMapLocMother(locMap, motherRecord);
                locMap.put(KilkariConstants.BENEFICIARY_ID, motherRecord.getIdNo());
                locArrList.add(locMap);
            }
            updateLocInMap(locArrList, stateId, mctsUserType);

        } catch (NullPointerException e) {
            LOGGER.error("No files present e : ", e);
        } catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
        }
    }

    private void childLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, MctsUserType mctsUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();

            for (Map<String, Object> record : result){
                ChildRecord childRecord =  convertMapToChild(record);
                Map<String, Object> locMap = new HashMap<>();
                mctsWsImportService.toMapLocChild(locMap, childRecord);
                locMap.put(KilkariConstants.BENEFICIARY_ID, childRecord.getIdNo());
                locArrList.add(locMap);
            }
            updateLocInMap(locArrList, stateId, mctsUserType);

        } catch (NullPointerException e) {
            LOGGER.error("No files present e : ", e);
        } catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
        }
    }

    private void ashaLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, MctsUserType mctsUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();

            for (Map<String, Object> record : result){
                AnmAshaRecord anmAshaRecord =  frontLineWorkerImportService.convertMapToAsha(record);
                Map<String, Object> locMap = new HashMap<>();
                toMapLocAsha(locMap, anmAshaRecord);
                locMap.put("Flw_Id", anmAshaRecord.getId());
                locArrList.add(locMap);
            }
            updateLocInMap(locArrList, stateId, mctsUserType);

        } catch (NullPointerException e) {
            LOGGER.error("No files present e : ", e);
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

        csvWriter(updatedLocArrList, stateId, mctsUserType);
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

    private void csvWriter(List<Map<String, Object>> locArrList, Long stateId, MctsUserType mctsUserType) throws IOException { //NO CHECKSTYLE Cyclomatic Complexity
        String locUpdateDir = settingsFacade.getProperty(LOC_UPDATE_DIR);
        String fileName = locUpdateDir + "location_update_state" + "_" + stateId + "_" + mctsUserType + "_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ".csv";
        File csvFile = new File(fileName);
        if (!csvFile.exists()){
            csvFile.createNewFile();
        } else {
            LOGGER.debug("File already exists");
        }

        if (!locArrList.isEmpty()) {
            FileWriter writer;
            writer = new FileWriter(csvFile, true);

            for (Map<String, Object> map : locArrList
                    ) {

                writer.write(map.get(KilkariConstants.BENEFICIARY_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.STATE_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.DISTRICT_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.DISTRICT_NAME).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.TALUKA_ID) == null ? NULL : map.get(KilkariConstants.TALUKA_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.TALUKA_NAME) == null ? NULL : map.get(KilkariConstants.TALUKA_NAME).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.HEALTH_BLOCK_ID) == null ? NULL : map.get(KilkariConstants.HEALTH_BLOCK_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.HEALTH_BLOCK_NAME) == null ? NULL : map.get(KilkariConstants.HEALTH_BLOCK_NAME).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.PHC_ID) == null ? NULL : map.get(KilkariConstants.PHC_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.PHC_NAME) == null ? NULL : map.get(KilkariConstants.PHC_NAME).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.SUB_CENTRE_ID) == null ? NULL : map.get(KilkariConstants.SUB_CENTRE_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.SUB_CENTRE_NAME) == null ? NULL : map.get(KilkariConstants.SUB_CENTRE_NAME).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.CENSUS_VILLAGE_ID) == null ? NULL : map.get(KilkariConstants.CENSUS_VILLAGE_ID).toString());
                writer.write(",");
                writer.write(map.get(KilkariConstants.VILLAGE_NAME) == null ? NULL : map.get(KilkariConstants.VILLAGE_NAME).toString());
                writer.write("\r\n");
            }

            writer.close();
        }
    }
}
