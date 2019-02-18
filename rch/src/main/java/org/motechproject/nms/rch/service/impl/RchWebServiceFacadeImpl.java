package org.motechproject.nms.rch.service.impl;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.ser.BeanSerializer;
import org.apache.axis.server.AxisServer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.mds.util.Order;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.exception.FlwExistingRecordException;
import org.motechproject.nms.flw.exception.FlwImportException;
import org.motechproject.nms.flw.exception.GfStatusInactiveException;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.contract.RchAnmAshaRecord;
import org.motechproject.nms.kilkari.contract.RchChildRecord;
import org.motechproject.nms.kilkari.contract.RchDistrictRecord;
import org.motechproject.nms.kilkari.contract.RchHealthBlockRecord;
import org.motechproject.nms.kilkari.contract.RchHealthFacilityRecord;
import org.motechproject.nms.kilkari.contract.RchHealthSubFacilityRecord;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;
import org.motechproject.nms.kilkari.contract.RchTalukaHealthBlockRecord;
import org.motechproject.nms.kilkari.contract.RchTalukaRecord;
import org.motechproject.nms.kilkari.contract.RchVillageHealthSubFacilityRecord;
import org.motechproject.nms.kilkari.contract.RchVillageRecord;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.ThreadProcessorObject;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
import org.motechproject.nms.kilkari.service.ChildCsvThreadProcessor;
import org.motechproject.nms.kilkari.service.MotherCsvThreadProcessor;
import org.motechproject.nms.kilkari.utils.FlwConstants;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.rch.contract.RchAnmAshaDataSet;
import org.motechproject.nms.rch.contract.RchChildrenDataSet;
import org.motechproject.nms.rch.contract.RchDistrictDataSet;
import org.motechproject.nms.rch.contract.RchHealthBlockDataSet;
import org.motechproject.nms.rch.contract.RchHealthFacilityDataSet;
import org.motechproject.nms.rch.contract.RchHealthSubFacilityDataSet;
import org.motechproject.nms.rch.contract.RchMothersDataSet;
import org.motechproject.nms.rch.contract.RchTalukaDataSet;
import org.motechproject.nms.rch.contract.RchTalukaHealthBlockDataSet;
import org.motechproject.nms.rch.contract.RchVillageDataSet;
import org.motechproject.nms.rch.contract.RchVillageHealthSubFacilityDataSet;
import org.motechproject.nms.rch.domain.RchImportAudit;
import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.domain.RchImportFailRecord;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.*;
import org.motechproject.nms.rch.repository.RchImportAuditDataService;
import org.motechproject.nms.rch.repository.RchImportFacilitatorDataService;
import org.motechproject.nms.rch.repository.RchImportFailRecordDataService;
import org.motechproject.nms.rch.service.RchImportFacilitatorService;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.soap.DS_DataResponseDS_DataResult;
import org.motechproject.nms.rch.soap.Irchwebservices;
import org.motechproject.nms.rch.soap.RchwebservicesLocator;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.rch.utils.ExecutionHelper;
import org.motechproject.nms.rch.utils.MarshallUtils;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.LocationEnum;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.nms.rejectionhandler.service.FlwRejectionService;
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
import org.xml.sax.helpers.AttributesImpl;

import javax.jdo.Query;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanRchMotherRecords;
import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanRchChildRecords;
import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanRchFlwRecords;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.childRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchChild;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchMother;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.motherRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.flwRejectionRch;

@Service("rchWebServiceFacade")
public class RchWebServiceFacadeImpl implements RchWebServiceFacade {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String LOCAL_RESPONSE_DIR = "rch.local_response_dir";
    private static final String REMOTE_RESPONSE_DIR = "rch.remote_response_dir";
    private static final String REMOTE_RESPONSE_DIR_CSV = "rch.remote_response_dir_csv";
    private static final String REMOTE_RESPONSE_DIR_XML = "rch.remote_response_dir_xml";
    private static final String LOC_UPDATE_DIR_RCH = "rch.loc_update_dir";
    private static final String REMOTE_RESPONSE_DIR_LOCATION = "rch.remote_response_dir_locations";
    private static final String NEXT_LINE = "\r\n";
    private static final String TAB = "\t";
    private static final Integer LOCATION_PART_SIZE = 5000;
    private static final String RECORDS = "Records";

    private static final String QUOTATION = "'";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String FROM_DATE_LOG = "fromdate {}";
    private static final String SCP_ERROR_REMOTE = "error copying file to remote server.";
    private static final String SCP_ERROR_LOCAL = "error copying file to local server.";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("dd-MM-yyyy");
    private static final String SCP_TIMEOUT_SETTING = "rch.scp_timeout";
    private static final Long SCP_TIME_OUT = 60000L;
    private static final String RCH_WEB_SERVICE = "RCH Web Service";
    private static final String BULK_REJECTION_ERROR_MESSAGE = "Error while bulk updating rejection records";
    private static final double THOUSAND = 1000d;
    private static final String FILES_MISSING_ON_A = "No files saved a due to ";
    private static final String FILES_MISSING_ON_B = "No files saved b due to ";
    private static final String FILES_MISSING_ON_C = "No files saved c due to ";
    private static final String SCP_LOCAL_TO_REMOTE_REMOTELOCATION = "empty";


    @Autowired
    @Qualifier("rchSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    @Qualifier("rchServiceLocator")
    private RchwebservicesLocator rchServiceLocator;

    private static final Logger LOGGER = LoggerFactory.getLogger(RchWebServiceFacadeImpl.class);

    @Autowired
    private RchImportAuditDataService rchImportAuditDataService;

    @Autowired
    private RchImportFacilitatorDataService rchImportFacilitatorDataService;

    @Autowired
    private RchImportFacilitatorService rchImportFacilitatorService;

    @Autowired
    private StateDataService stateDataService;

    @Autowired
    private FrontLineWorkerImportService frontLineWorkerImportService;

    @Autowired
    private RchImportFailRecordDataService rchImportFailRecordDataService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;

    @Autowired
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    @Autowired
    private MctsBeneficiaryImportReaderService mctsBeneficiaryImportReaderService;


    @Autowired
    private FlwRejectionService flwRejectionService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private EventRelay eventRelay;

    private String createErrorMessage(String message, Exception e) {
        final int START = 0;
        final int END = 99;
        return message + StringUtils.substring(e.getMessage(), START, END);
    }

    class ScpRunnable implements Runnable {
        String name;
        String remoteLocation;
        LocalDate from;
        LocalDate to;
        Long stateId;
        RchUserType userType;
        int trialCount;

        public ScpRunnable(String name, String remoteLocation, LocalDate from, LocalDate to, Long stateId, RchUserType userType, int trialCount) {
            this.name=name;
            this.remoteLocation = remoteLocation;
            this.from  = from;
            this.to= to;
            this.stateId = stateId;
            this.userType  = userType;
            this.trialCount = trialCount;
        }

        public void run() {
            try {
                Thread.sleep(120000L);
                trialCount++;
                LOGGER.error("Error in SCP for {} time for state {} for type {}", trialCount, stateId, userType);
                if (trialCount <= 3) {
                    if (SCP_LOCAL_TO_REMOTE_REMOTELOCATION.equals(remoteLocation)) {
                        retryScpAndAudit(name, from, to, stateId, userType, trialCount);
                    } else {
                        retryScpFromRemoteToLocal(name, remoteLocation, from, to, stateId, userType, trialCount);
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }
    private boolean retryScpAndAudit(String name, LocalDate from, LocalDate to, Long stateId, RchUserType userType, int trialCount) {
        try {
            scpResponseToRemote(name);
            LOGGER.info("RCH mother response file successfully copied to remote server");

            RchImportFacilitator rchImportFacilitator = new RchImportFacilitator(name, from, to, stateId, userType, LocalDate.now());
            rchImportFacilitatorService.createImportFileAudit(rchImportFacilitator);
            return true;

        } catch (ExecutionException e) {
            if(trialCount < 3) {
            Runnable scpRunnable = new ScpRunnable(name, SCP_LOCAL_TO_REMOTE_REMOTELOCATION, from, to, stateId, userType, trialCount);
            scpRunnable.run();
            } else {
                LOGGER.error(SCP_ERROR_REMOTE, e);
            }
         } catch (RchFileManipulationException e) {
            LOGGER.error("invalid file name", e);
        }
        return false;
    }

    private void retryScpFromRemoteToLocal(String name, String remoteLocation, LocalDate from, LocalDate to, Long stateId, RchUserType userType, int trialCount) {
        try {
            List<RchImportFacilitator> rchImportFacilitatorTypes = rchImportFacilitatorService.findByImportDateStateIdAndRchUserType(stateId, LocalDate.now(), userType);
            File localResponseFile ;
            if (rchImportFacilitatorTypes.isEmpty() && name != null) {
                localResponseFile = scpResponseToLocal(name, remoteLocation);
                String result = readResponsesFromXml(localResponseFile);
                State importState = stateDataService.findByCode(stateId);
                String stateName = importState.getName();
                Long stateCode = importState.getCode();

                LocalDate startReferenceDate = to;
                LocalDate endReferenceDate = from;
                processByUserType(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate, userType);
            } else {
                for (RchImportFacilitator rchImportFacilitatorType: rchImportFacilitatorTypes
                ) {
                    if (name == null || rchImportFacilitatorType.getFileName() != null) {
                        localResponseFile = scpResponseToLocal(rchImportFacilitatorType.getFileName(), remoteLocation);
                    } else {
                        localResponseFile = scpResponseToLocal(name, remoteLocation);
                    }
                    if (localResponseFile != null) {
                        String result = readResponsesFromXml(localResponseFile);
                        State importState = stateDataService.findByCode(stateId);
                        String stateName = importState.getName();
                        Long stateCode = importState.getCode();
                        LocalDate startReferenceDate = rchImportFacilitatorType.getStartDate();
                        LocalDate endReferenceDate = rchImportFacilitatorType.getEndDate();
                        processByUserType(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate, userType);
                    }
                }
            }
        } catch (ExecutionException e) {
            if(trialCount < 3) {
                Runnable scpRunnable = new ScpRunnable(name, remoteLocation, from, to, stateId, userType, trialCount);
                scpRunnable.run();
            } else {
                LOGGER.error(SCP_ERROR_LOCAL, e);
            }
        } catch (RchFileManipulationException e) {

        } finally {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(Constants.STATE_ID_PARAM, stateId);
            eventParams.put(Constants.REMOTE_LOCATION, null);
            eventParams.put(Constants.FILE_NAME, null);
            eventByUserType(eventParams, userType);
            }
        }

    private void eventByUserType(Map<String, Object> eventParams, RchUserType userType) {
        switch (userType) {
            case DISTRICT:
                eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_TALUKA_READ_SUBJECT, eventParams));
                break;
            case TALUKA:
                eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_HEALTHBLOCK_READ_SUBJECT, eventParams));
                break;
            case HEALTHBLOCK:
                eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_TALUKA_HEALTHBLOCK_READ_SUBJECT, eventParams));
                break;
            case TALUKAHEALTHBLOCK:
                eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_HEALTHFACILITY_READ_SUBJECT, eventParams));
                break;
            case HEALTHFACILITY:
                eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_HEALTHSUBFACILITY_READ_SUBJECT, eventParams));
                break;
            case HEALTHSUBFACILITY:
                eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_VILLAGE_READ_SUBJECT, eventParams));
                break;
            case VILLAGE:
                eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_VILLAGE_HEALTHSUBFACILITY_READ_SUBJECT, eventParams));
                break;
            default:
                return;
        }
    }

    private void processByUserType(String name, String result, Long stateId, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate, RchUserType rchUserType) {
        switch (rchUserType) {
            case MOTHER:
                motherFileProcess(result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case CHILD:
                childFileProcess(result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case ASHA:
                ashaFileProcess(result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case DISTRICT:
                districtFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case TALUKA:
                talukaFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case HEALTHBLOCK:
                healthblockFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case TALUKAHEALTHBLOCK:
                tHFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case HEALTHFACILITY:
                healthFacilityFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case HEALTHSUBFACILITY:
                healthsubfacilityFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case VILLAGE:
                villageFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
            case VILLAGEHEALTHSUBFACILITY:
                vhsfFileProcess(name, result, stateId, stateName, stateCode, startReferenceDate, endReferenceDate);
                break;
        }
    }

    @Override
    public boolean getMothersData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_MOTHER_USER), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH Mother Data.", e);
        }

        LOGGER.debug("writing RCH mother response to file");
        File responseFile = generateResponseFile(result, RchUserType.MOTHER, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH mother response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.MOTHER, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.MOTHER, stateId);
        }
        return status;
    }

    @Override
    public boolean getDistrictData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_DISTRICT), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH District Data.", e);
        }

        LOGGER.debug("writing RCH District response to file");
        File responseFile = generateResponseFile(result, RchUserType.DISTRICT, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH district response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.DISTRICT, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.DISTRICT, stateId);
        }
        return status;
    }

    @Override
    public boolean getTalukasData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_TALUKA), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH Taluka Data.", e);
        }

        LOGGER.debug("writing RCH taluka response to file");
        File responseFile = generateResponseFile(result, RchUserType.TALUKA, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH taluka response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.TALUKA, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.TALUKA, stateId);
        }
        return status;
    }

    @Override
    public boolean getVillagesData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_VILLAGE), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH Village Data.", e);
        }

        LOGGER.debug("writing RCH Village response to file");
        File responseFile = generateResponseFile(result, RchUserType.VILLAGE, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH Village response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.VILLAGE, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.VILLAGE, stateId);
        }
        return status;
    }

    @MotechListener(subjects = Constants.RCH_LOCATION_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readLocationResponse(MotechEvent event) throws RchFileManipulationException {

        LOGGER.info("Starting location read.");
        List<Long> stateIds = getStateIds();
        for (Long stateId : stateIds
        ) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(Constants.STATE_ID_PARAM, stateId);
            eventParams.put(Constants.REMOTE_LOCATION, null);
            eventParams.put(Constants.FILE_NAME, null);
            eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, eventParams));
        }
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_DISTRICT_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readDistrictResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH District file import entry point");
        LOGGER.info("Copying RCH District response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.DISTRICT, 0);
    }

    private void districtFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {

            ArrayList<Map<String, Object>> districtArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchDistrictDataSet districtDataSet = (result == null) ?
                        null :
                        (RchDistrictDataSet) MarshallUtils.unmarshall(result, RchDistrictDataSet.class);

                LOGGER.info("Starting RCH district import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (districtDataSet == null || districtDataSet.getRecords() == null) {
                    String warning = String.format("No district data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.DISTRICT, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchDistrictRecord> districtRecords = districtDataSet.getRecords();
                    for (RchDistrictRecord record : districtRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapDistrict(locMap, record, stateCode);
                        districtArrList.add(locMap);

                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < districtArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < districtArrList.size()) {
                        recordListPart.add(districtArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.DISTRICT, fileName, partNumber);
                    recordListPart.clear();
                }

                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);

            } else {
                String warning = String.format("No district data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
                //rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.DISTRICT, stateCode, stateName, 0, 0, warning));
               // rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.DISTRICT, stateId));
            }

        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH district data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.DISTRICT, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.DISTRICT, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH districts data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service District Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.DISTRICT, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.DISTRICT, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.DISTRICT, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.DISTRICT, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.DISTRICT, e);
        }
    }


    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_TALUKA_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readTalukaResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.info("Copying RCH taluka response file from remote server to local directory.");
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH Taluka file import entry point");
        LOGGER.info("Copying RCH Taluka response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.TALUKA, 0);
    }

    private void talukaFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {

            ArrayList<Map<String, Object>> talukaArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchTalukaDataSet talukaDataSet = (result == null) ?
                        null :
                        (RchTalukaDataSet) MarshallUtils.unmarshall(result, RchTalukaDataSet.class);

                LOGGER.info("Starting RCH taluka import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (talukaDataSet == null || talukaDataSet.getRecords() == null) {
                    String warning = String.format("No taluka data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKA, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchTalukaRecord> talukaRecords = talukaDataSet.getRecords();
                    for (RchTalukaRecord record : talukaRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapTaluka(locMap, record, stateCode);
                        talukaArrList.add(locMap);

                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < talukaArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < talukaArrList.size()) {
                        recordListPart.add(talukaArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.TALUKA, fileName, partNumber);
                    recordListPart.clear();
                }

                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);

            } else {
                String warning = String.format("No Taluka data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
                //rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKA, stateCode, stateName, 0, 0, warning));
               // rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKA, stateId));
            }

        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH taluka data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKA, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKA, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH taluka data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service taluka Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKA, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKA, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKA, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKA, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.TALUKA, e);
        }
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_VILLAGE_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readVillageResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH village file import entry point");
        LOGGER.info("Copying RCH village response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.VILLAGE, 0);
    }

    private void villageFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {
            ArrayList<Map<String, Object>> villageArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchVillageDataSet villageDataSet = (result == null) ?
                        null :
                        (RchVillageDataSet) MarshallUtils.unmarshall(result, RchVillageDataSet.class);

                LOGGER.info("Starting RCH village import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (villageDataSet == null || villageDataSet.getRecords() == null) {
                    String warning = String.format("No village data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGE, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchVillageRecord> villageRecords = villageDataSet.getRecords();
                    for (RchVillageRecord record : villageRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapVillage(locMap, record, stateCode);
                        villageArrList.add(locMap);

                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < villageArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < villageArrList.size()) {
                        recordListPart.add(villageArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.VILLAGE, fileName, partNumber);
                    recordListPart.clear();
                }

                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);

            } else {
                String warning = String.format("No Village data set received from RCH for %d stateId", stateId);
               // rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGE, stateCode, stateName, 0, 0, warning));
               // rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGE, stateId));
                LOGGER.warn(warning);
            }

        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH Village data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGE, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGE, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH Village data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Village Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGE, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGE, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGE, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGE, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.VILLAGE, e);
        }
    }

    @MotechListener(subjects = Constants.RCH_MOTHER_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readMotherResponse(MotechEvent event) throws RchFileManipulationException {

        LOGGER.info("Starting Mother read.");
        List<Long> stateIds = getStateIds();
        for (Long stateId : stateIds
        ) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(Constants.STATE_ID_PARAM, stateId);
            eventParams.put(Constants.REMOTE_LOCATION, null);
            eventParams.put(Constants.FILE_NAME, null);
            eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_MOTHER_READ, eventParams));
        }
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_MOTHER_READ) //NO CHECKSTYLE Cyclomatic Complexity
    public void readMotherResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH Mother file import entry point");
        LOGGER.info("Copying RCH Mother response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.MOTHER, 0);
    }

    private void motherFileProcess(String result, Long stateId, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        try {
            if (result.contains(RECORDS)) {
                RchMothersDataSet mothersDataSet = (result == null) ?
                        null :
                        (RchMothersDataSet) MarshallUtils.unmarshall(result, RchMothersDataSet.class);

                LOGGER.info("Starting RCH mother import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (mothersDataSet == null || mothersDataSet.getRecords() == null) {
                    String warning = String.format("No mother data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, warning));
                } else {
                    LOGGER.info("Received {} mother records from RCH for {} state", sizeNullSafe(mothersDataSet.getRecords()), stateName);

                    RchImportAudit audit = saveImportedMothersData(mothersDataSet, stateName, stateCode, startReferenceDate, endReferenceDate);
                    rchImportAuditDataService.create(audit);
                    stopWatch.stop();
                    double seconds = stopWatch.getTime() / THOUSAND;
                    LOGGER.info("Finished RCH mother import dispatch in {} seconds. Accepted {} mothers, Rejected {} mothers",
                            seconds, audit.getAccepted(), audit.getRejected());

                    LOGGER.info("fromDate for delete {} {}", startReferenceDate, endReferenceDate);
                    deleteRchImportFailRecords(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateId);
                }
            } else {
                String warning = String.format("No Mother data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
//                rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, warning));
//                rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.MOTHER, stateId));
            }
        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH mother data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.MOTHER, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH mothers data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Mother Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.MOTHER, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.MOTHER, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.MOTHER, e);
        }
    }

    @Override
    public boolean getChildrenData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_CHILD_USER), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH Children Data.", e);
        }

        LOGGER.debug("writing RCH children response to file");
        File responseFile = generateResponseFile(result, RchUserType.CHILD, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH children response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.CHILD, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.CHILD, stateId);
        }

        return status;
    }

    @MotechListener(subjects = Constants.RCH_CHILD_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readChildResponse(MotechEvent event) throws RchFileManipulationException {

        LOGGER.info("Starting Child read.");
        List<Long> stateIds = getStateIds();
        for (Long stateId : stateIds
        ) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(Constants.STATE_ID_PARAM, stateId);
            eventParams.put(Constants.REMOTE_LOCATION, null);
            eventParams.put(Constants.FILE_NAME, null);
            eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_CHILD_READ, eventParams));
        }
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_CHILD_READ)
    public void readChildResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH Child file import entry point");
        LOGGER.info("Copying RCH Child response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.CHILD, 0);
    }

    private void childFileProcess(String result, Long stateId, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        try {
            if (result.contains(RECORDS)) {
                RchChildrenDataSet childrenDataSet = (result == null) ?
                        null :
                        (RchChildrenDataSet) MarshallUtils.unmarshall(result, RchChildrenDataSet.class);

                LOGGER.info("Starting RCH children import for stateId: {}", stateId);
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (childrenDataSet == null || childrenDataSet.getRecords() == null) {
                    String warning = String.format("No child data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, warning));
                } else {
                    LOGGER.info("Received {} children records from RCH for {} state", sizeNullSafe(childrenDataSet.getRecords()), stateName);

                    RchImportAudit audit = saveImportedChildrenData(childrenDataSet, stateName, stateCode, startReferenceDate, endReferenceDate);
                    rchImportAuditDataService.create(audit);
                    stopWatch.stop();
                    double seconds = stopWatch.getTime() / THOUSAND;
                    LOGGER.info("Finished children import dispatch in {} seconds. Accepted {} children, Rejected {} children",
                            seconds, audit.getAccepted(), audit.getRejected());

                    // Delete RchImportFailRecords once import is successful
                    deleteRchImportFailRecords(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateId);
                }
            } else {
                String warning = String.format("No Child data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
//                rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, warning));
//                rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
            }
        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH children data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH children data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Child Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_B, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
            LOGGER.error(FILES_MISSING_ON_B, RchUserType.CHILD, e);
        }
    }


    @Override
    public boolean getAnmAshaData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_ASHA_USER), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH FLW Data.", e);
        }

        LOGGER.debug("writing RCH Asha response to file");
        File responseFile = generateResponseFile(result, RchUserType.ASHA, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH asha response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.ASHA, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.ASHA, stateId);
        }

        return status;
    }

    @MotechListener(subjects = Constants.RCH_ASHA_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readASHAResponse(MotechEvent event) throws RchFileManipulationException {

        LOGGER.info("Starting Asha read.");
        List<Long> stateIds = getStateIds();
        for (Long stateId : stateIds
        ) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(Constants.STATE_ID_PARAM, stateId);
            eventParams.put(Constants.REMOTE_LOCATION, null);
            eventParams.put(Constants.FILE_NAME, null);
            eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_ASHA_READ, eventParams));
        }
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_ASHA_READ)
    public void readAshaResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH Asha file import entry point");
        LOGGER.info("Copying RCH Asha response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.ASHA, 0);
    }

    private void ashaFileProcess(String result, Long stateId, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        try {
            if (result.contains(RECORDS)) {
                RchAnmAshaDataSet ashaDataSet = (result == null) ?
                        null :
                        (RchAnmAshaDataSet) MarshallUtils.unmarshall(result, RchAnmAshaDataSet.class);

                LOGGER.info("Starting RCH FLW import for stateId: {}", stateId);
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (ashaDataSet == null || ashaDataSet.getRecords() == null) {
                    String warning = String.format("No FLW data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, warning));
                } else {
                    LOGGER.info("Received {} FLW records from RCH for {} state", sizeNullSafe(ashaDataSet.getRecords()), stateName);

                    RchImportAudit audit = saveImportedAshaData(ashaDataSet, stateName, stateCode, startReferenceDate, endReferenceDate);
                    rchImportAuditDataService.create(audit);
                    stopWatch.stop();
                    double seconds = stopWatch.getTime() / THOUSAND;
                    LOGGER.info("Finished RCH FLW import dispatch in {} seconds. Accepted {} Ashas, Rejected {} Ashas",
                            seconds, audit.getAccepted(), audit.getRejected());

                    // Delete RchImportFailRecords once import is successful
                    deleteRchImportFailRecords(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateId);
                }
            } else {
                String warning = String.format("No Asha data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
//                rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, warning));
//                rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
            }
        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH FLW data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH FLW data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service FLW Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_C, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
            LOGGER.error(FILES_MISSING_ON_C, RchUserType.ASHA, e);
        }
    }

    @Override
    public boolean getHealthBlockData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_HEALTHBLOCK), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH healthblock Data.", e);
        }

        LOGGER.debug("writing RCH taluka response to file");
        File responseFile = generateResponseFile(result, RchUserType.HEALTHBLOCK, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH healthblock response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.HEALTHBLOCK, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.HEALTHBLOCK, stateId);
        }
        return status;
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_HEALTHBLOCK_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readHealthBlockResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.info("Copying RCH healthblock response file from remote server to local directory.");
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH Healthblock file import entry point");
        LOGGER.info("Copying RCH Healthblock response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.HEALTHBLOCK, 0);
    }

    private void healthblockFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {
            ArrayList<Map<String, Object>> healthBlockArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchHealthBlockDataSet healthBlockDataSet = (result == null) ?
                        null :
                        (RchHealthBlockDataSet) MarshallUtils.unmarshall(result, RchHealthBlockDataSet.class);

                LOGGER.info("Starting RCH healthblock import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (healthBlockDataSet == null || healthBlockDataSet.getRecords() == null) {
                    String warning = String.format("No healthblock data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHBLOCK, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchHealthBlockRecord> rchHealthBlockRecords = healthBlockDataSet.getRecords();
                    for (RchHealthBlockRecord record : rchHealthBlockRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapHealthBlock(locMap, record, stateCode);
                        healthBlockArrList.add(locMap);
                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < healthBlockArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < healthBlockArrList.size()) {
                        recordListPart.add(healthBlockArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.HEALTHBLOCK, fileName, partNumber);
                    recordListPart.clear();
                }
                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);
            } else {
                String warning = String.format("No HealthBlock data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
//                rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHBLOCK, stateCode, stateName, 0, 0, warning));
//                rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHBLOCK, stateId));
            }
        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH mother data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHBLOCK, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHBLOCK, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH mothers data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Mother Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHBLOCK, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHBLOCK, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHBLOCK, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHBLOCK, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.HEALTHBLOCK, e);
        }
    }

    @Override
    public boolean getTalukaHealthBlockData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_TALUKA_HEALTHBLOCK), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH taluka-healthblock Data.", e);
        }

        LOGGER.debug("writing RCH taluka-healthblock response to file");
        File responseFile = generateResponseFile(result, RchUserType.TALUKAHEALTHBLOCK, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH taluka-healthblock response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.TALUKAHEALTHBLOCK, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.TALUKAHEALTHBLOCK, stateId);
        }
        return status;
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_TALUKA_HEALTHBLOCK_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readTalukaHealthBlockResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.info("Copying RCH taluka-healthblock response file from remote server to local directory.");
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH TalukaHealthblock file import entry point");
        LOGGER.info("Copying RCH TalukaHealthblock response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.TALUKAHEALTHBLOCK, 0);
    }

    private void tHFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {
            ArrayList<Map<String, Object>> talukaHealthBlockArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchTalukaHealthBlockDataSet talukaHealthBlockDataSet = (result == null) ?
                        null :
                        (RchTalukaHealthBlockDataSet) MarshallUtils.unmarshall(result, RchTalukaHealthBlockDataSet.class);

                LOGGER.info("Starting RCH taluka-healthBlock import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (talukaHealthBlockDataSet == null || talukaHealthBlockDataSet.getRecords() == null) {
                    String warning = String.format("No taluka-healthBlock data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKAHEALTHBLOCK, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchTalukaHealthBlockRecord> rchTalukaHealthBlockRecords = talukaHealthBlockDataSet.getRecords();
                    for (RchTalukaHealthBlockRecord record : rchTalukaHealthBlockRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapTalukaHealthBlock(locMap, record, stateCode);
                        talukaHealthBlockArrList.add(locMap);
                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < talukaHealthBlockArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < talukaHealthBlockArrList.size()) {
                        recordListPart.add(talukaHealthBlockArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.TALUKAHEALTHBLOCK, fileName, partNumber);
                    recordListPart.clear();
                }

                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);
            } else {
                String warning = String.format("No Taluka-HealthBlock data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
//                rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKAHEALTHBLOCK, stateCode, stateName, 0, 0, warning));
//                rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKAHEALTHBLOCK, stateId));
            }

        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH Taluka-HealthBlock data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKAHEALTHBLOCK, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKAHEALTHBLOCK, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH taluka healthblock data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Taluka-HealthBlock Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKAHEALTHBLOCK, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKAHEALTHBLOCK, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKAHEALTHBLOCK, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKAHEALTHBLOCK, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.TALUKAHEALTHBLOCK, e);
        }
    }
    @Override
    public boolean getHealthFacilityData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_HEALTHFACILITY), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH healthfacility Data.", e);
        }

        LOGGER.debug("writing RCH healthfacility response to file");
        File responseFile = generateResponseFile(result, RchUserType.HEALTHFACILITY, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH healthfacility response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.HEALTHFACILITY, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.HEALTHFACILITY, stateId);
        }
        return status;
    }

    @Override
    public boolean getHealthSubFacilityData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_HEALTHSUBFACILITY), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH healthsubfacility Data.", e);
        }

        LOGGER.debug("writing RCH healthsubfacility response to file");
        File responseFile = generateResponseFile(result, RchUserType.HEALTHSUBFACILITY, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH healthsubfacility response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.HEALTHSUBFACILITY, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.HEALTHSUBFACILITY, stateId);
        }
        return status;
    }

    @Override
    public boolean getVillageHealthSubFacilityData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;
        LOGGER.info(FROM_DATE_LOG, from);

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_LOCATION_VILLAGE_HEALTHSUBFACILITY), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH villagehealthfacility Data.", e);
        }

        LOGGER.debug("writing RCH villagehealthfacility response to file");
        File responseFile = generateResponseFile(result, RchUserType.VILLAGEHEALTHSUBFACILITY, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH villagehealthfacility response successfully written to file. Copying to remote directory.");
            status = retryScpAndAudit(responseFile.getName(), from, to, stateId, RchUserType.VILLAGEHEALTHSUBFACILITY, 0);
        } else {
            LOGGER.error("Error writing {} response to file for state {}" ,  RchUserType.VILLAGEHEALTHSUBFACILITY, stateId);
        }
        return status;
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_HEALTHFACILITY_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readHealthFacilityResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.info("Copying RCH healthfacility response file from remote server to local directory.");
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH Healthfacility file import entry point");
        LOGGER.info("Copying RCH Healthfacility response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.HEALTHFACILITY, 0);
    }

    private void healthFacilityFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {
            ArrayList<Map<String, Object>> healthFacilityArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchHealthFacilityDataSet healthFacilityDataSet = (result == null) ?
                        null :
                        (RchHealthFacilityDataSet) MarshallUtils.unmarshall(result, RchHealthFacilityDataSet.class);

                LOGGER.info("Starting RCH healthfacility import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (healthFacilityDataSet == null || healthFacilityDataSet.getRecords() == null) {
                    String warning = String.format("No healthfacility data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHFACILITY, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchHealthFacilityRecord> rchHealthFacilityRecords = healthFacilityDataSet.getRecords();
                    for (RchHealthFacilityRecord record : rchHealthFacilityRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapHealthFacility(locMap, record, stateCode);
                        healthFacilityArrList.add(locMap);

                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < healthFacilityArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < healthFacilityArrList.size()) {
                        recordListPart.add(healthFacilityArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.HEALTHFACILITY, fileName, partNumber);
                    recordListPart.clear();
                }
                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);
            } else {
                String warning = String.format("No Healthfacility data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
                //rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHFACILITY, stateCode, stateName, 0, 0, warning));
                //rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHFACILITY, stateId));
            }

        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH healthfacility data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHFACILITY, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHFACILITY, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH healthfacility data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service healthfacility Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHFACILITY, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHFACILITY, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHFACILITY, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHFACILITY, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.HEALTHFACILITY, e);
        }
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_HEALTHSUBFACILITY_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readHealthSubFacilityResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate) event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH Healthsubfacility file import entry point");
        LOGGER.info("Copying RCH Healthsubfacility response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.HEALTHSUBFACILITY, 0);
    }

    private void healthsubfacilityFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {
            ArrayList<Map<String, Object>> healthSubFacilityArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchHealthSubFacilityDataSet healthSubFacilityDataSet = (result == null) ?
                        null :
                        (RchHealthSubFacilityDataSet) MarshallUtils.unmarshall(result, RchHealthSubFacilityDataSet.class);

                LOGGER.info("Starting RCH healthsubfacility import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (healthSubFacilityDataSet == null || healthSubFacilityDataSet.getRecords() == null) {
                    String warning = String.format("No healthsubfacility data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHSUBFACILITY, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchHealthSubFacilityRecord> rchHealthFacilityRecords = healthSubFacilityDataSet.getRecords();
                    for (RchHealthSubFacilityRecord record : rchHealthFacilityRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapHealthSubFacility(locMap, record, stateCode);
                        healthSubFacilityArrList.add(locMap);

                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < healthSubFacilityArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < healthSubFacilityArrList.size()) {
                        recordListPart.add(healthSubFacilityArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.HEALTHSUBFACILITY, fileName, partNumber);
                    recordListPart.clear();
                }
                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);
            } else {
                String warning = String.format("No healthsubfacility data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
            }

        } catch (JAXBException e) {
            throw new RchInvalidResponseStructureException(String.format("Cannot deserialize RCH healthsubfacility data from %s location.", stateId), e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH healthsubfacility data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service healthsubfacility Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHSUBFACILITY, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHSUBFACILITY, stateId));
        } catch (NullPointerException e) {
            LOGGER.error("No files saved a : ", e);
        }
    }

    /**
     * The remoteLocation and fileName would be not null only in the case of integration tests.
     * At other times the values will be taken from property files.
     */
    @MotechListener(subjects = Constants.RCH_VILLAGE_HEALTHSUBFACILITY_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readVillageHealthSubFacilityResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
        String remoteLocation = (String) event.getParameters().get(Constants.REMOTE_LOCATION);
        String fileName = (String) event.getParameters().get(Constants.FILE_NAME);
        LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);
        LocalDate startDate = (LocalDate)   event.getParameters().get(Constants.START_DATE_PARAM);
        LOGGER.info("RCH villagehealthsubfacility file import entry point");
        LOGGER.info("Copying RCH villagehealthsubfacility response file from remote server to local directory.");
        retryScpFromRemoteToLocal(fileName, remoteLocation, endDate, startDate, stateId, RchUserType.VILLAGEHEALTHSUBFACILITY, 0);
    }

    private void vhsfFileProcess(String fileName, String result, Long stateId, String stateName, Long stateCode, LocalDate startDate, LocalDate endDate) {
        try {
            ArrayList<Map<String, Object>> villageHealthSubFacilityArrList = new ArrayList<>();
            if (result.contains(RECORDS)) {
                RchVillageHealthSubFacilityDataSet villageHealthSubFacilityDataSet = (result == null) ?
                        null :
                        (RchVillageHealthSubFacilityDataSet) MarshallUtils.unmarshall(result, RchVillageHealthSubFacilityDataSet.class);

                LOGGER.info("Starting RCH villageHealthsubfacility import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (villageHealthSubFacilityDataSet == null || villageHealthSubFacilityDataSet.getRecords() == null) {
                    String warning = String.format("No villageHealthsubfacility data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateCode, stateName, 0, 0, warning));
                } else {
                    List<RchVillageHealthSubFacilityRecord> rchVillageHealthFacilityRecords = villageHealthSubFacilityDataSet.getRecords();
                    for (RchVillageHealthSubFacilityRecord record : rchVillageHealthFacilityRecords) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapVillageHealthSubFacility(locMap, record, stateCode);
                        villageHealthSubFacilityArrList.add(locMap);

                    }
                }
                int count = 0;
                int partNumber = 0;
                Long totalUpdatedRecords = 0L;
                while (count < villageHealthSubFacilityArrList.size()) {
                    List<Map<String, Object>> recordListPart = new ArrayList<>();
                    while (recordListPart.size() < LOCATION_PART_SIZE && count < villageHealthSubFacilityArrList.size()) {
                        recordListPart.add(villageHealthSubFacilityArrList.get(count));
                        count++;
                    }
                    partNumber++;
                    totalUpdatedRecords += locationService.createLocationPart(recordListPart, LocationEnum.VILLAGEHEALTHSUBFACILITY, fileName, partNumber);
                    recordListPart.clear();
                }
                LOGGER.debug("File {} processed. {} records updated", fileName, totalUpdatedRecords);
            } else {
                String warning = String.format("No villageHealthsubfacility data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
                //rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateCode, stateName, 0, 0, warning));
                //rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateId));
            }

        } catch (JAXBException e) {
            String error = String.format("Cannot deserialize RCH villageHealthsubfacility data from %s location.", stateId);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateId));
            throw new RchInvalidResponseStructureException(error, e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH villageHealthsubfacility data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service villageHealthsubfacility Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateId));
        } catch (Exception e) {
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateCode, stateName, 0, 0, createErrorMessage(FILES_MISSING_ON_A, e)));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.VILLAGEHEALTHSUBFACILITY, stateId));
            LOGGER.error(FILES_MISSING_ON_A, RchUserType.VILLAGEHEALTHSUBFACILITY, e);
        }
    }

    private Irchwebservices getService(URL endpoint) {
        try {
            if (endpoint != null) {
                return rchServiceLocator.getBasicHttpBinding_Irchwebservices(endpoint);
            } else {
                return rchServiceLocator.getBasicHttpBinding_Irchwebservices();
            }
        } catch (ServiceException e) {
            throw new RchWebServiceException("Cannot retrieve RCH Service for the endpoint", e);
        }
    }

    private void validMothersDataResponse(DS_DataResponseDS_DataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new RchInvalidResponseStructureException("Invalid mothers data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new RchInvalidResponseStructureException("Invalid mothers data response " + stateId);
        }
    }

    private void validTalukasDataResponse(DS_DataResponseDS_DataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new RchInvalidResponseStructureException("Invalid taluka data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new RchInvalidResponseStructureException("Invalid taluka data response " + stateId);
        }
    }

    private void validHealthBlockDataResponse(DS_DataResponseDS_DataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new RchInvalidResponseStructureException("Invalid healthblock data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new RchInvalidResponseStructureException("Invalid healthblock data response " + stateId);
        }
    }

    private void validTalukaHealthBlockDataResponse(DS_DataResponseDS_DataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new RchInvalidResponseStructureException("Invalid taluka-healthblock data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new RchInvalidResponseStructureException("Invalid taluka-healthblock data response " + stateId);
        }
    }

    private void validChildrenDataResponse(DS_DataResponseDS_DataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new RchInvalidResponseStructureException("Invalid children data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new RchInvalidResponseStructureException("Invalid children data response " + stateId);
        }
    }

    private void validAnmAshaDataResponse(DS_DataResponseDS_DataResult data, Long stateId) {
        if (data.get_any().length != 2) {
            throw new RchInvalidResponseStructureException("Invalid anm asha data response for location " + stateId);
        }

        if (data.get_any()[1].getChildren() != null && data.get_any()[1].getChildren().size() < 1) {
            throw new RchInvalidResponseStructureException("Invalid anm asha data response " + stateId);
        }
    }


    private String targetFileName(String timeStamp, RchUserType userType, Long stateId) {
        switch (userType) {
            case MOTHER: return String.format("RCH_StateID_%d_Mother_Response_%s.xml", stateId, timeStamp);
            case CHILD: return String.format("RCH_StateID_%d_Child_Response_%s.xml", stateId, timeStamp);
            case ASHA: return String.format("RCH_StateID_%d_Asha_Response_%s.xml", stateId, timeStamp);
            case TALUKA: return String.format("RCH_StateID_%d_Taluka_Response_%s.xml", stateId, timeStamp);
            case HEALTHBLOCK: return String.format("RCH_StateID_%d_HealthBlock_Response_%s.xml", stateId, timeStamp);
            case TALUKAHEALTHBLOCK: return String.format("RCH_StateID_%d_Taluka_HealthBlock_Response_%s.xml", stateId, timeStamp);
            case DISTRICT: return String.format("RCH_StateID_%d_District_Response_%s.xml", stateId, timeStamp);
            case VILLAGE: return String.format("RCH_StateID_%d_Village_Response_%s.xml", stateId, timeStamp);
            case HEALTHFACILITY: return String.format("RCH_StateID_%d_HealthFacility_Response_%s.xml", stateId, timeStamp);
            case HEALTHSUBFACILITY: return String.format("RCH_StateID_%d_HealthSubFacility_Response_%s.xml", stateId, timeStamp);
            case VILLAGEHEALTHSUBFACILITY: return String.format("RCH_StateID_%d_Village_HealthSubFacility_Response_%s.xml", stateId, timeStamp);
            default: return "Null";
        }
    }

    private File generateResponseFile(DS_DataResponseDS_DataResult result, RchUserType userType, Long stateId) {
        String targetFileName = targetFileName(TIME_FORMATTER.print(DateTime.now()), userType, stateId);
        File localResponseDir = localResponseDir();
        File localResponseFile = new File(localResponseDir, targetFileName);

        try {
            FileWriter writer = new FileWriter(localResponseFile);
            writer.write(serializeAxisObject(result));

            writer.flush();
            writer.close();

        } catch (Exception e) {
            LOGGER.debug("Failed deserialization", e);
            LOGGER.error((e.toString()));
            return null;
        }
        return localResponseFile;
    }


    private RchImportAudit saveImportedMothersData(RchMothersDataSet mothersDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) { //NOPMD NcssMethodCount
        LOGGER.info("Starting RCH mother import for state {}", stateName);
        List<RchMotherRecord> motherRecords = mothersDataSet.getRecords();
        List<Map<String, Object>> validMotherRecords = new ArrayList<>();
        validMotherRecords = getLMPValidRecords(motherRecords);
        List<List<Map<String, Object>>> rchMotherRecordsSet = cleanRchMotherRecords(validMotherRecords);
        List<Map<String, Object>> rejectedRchMothers = rchMotherRecordsSet.get(0);
        String action = "";
        int saved = 0;
        int rejected = motherRecords.size() - validMotherRecords.size();

        Map<String, Object> rejectedMothers = new HashMap<>();
        Map<String, Object> rejectionStatus = new HashMap<>();
        MotherImportRejection motherImportRejection;

        for (Map<String, Object> record : rejectedRchMothers) {
            action = (String) record.get(KilkariConstants.ACTION);
            LOGGER.debug("Existing Mother Record with same MSISDN in the data set");
            motherImportRejection = motherRejectionRch(convertMapToRchMother(record), false, RejectionReasons.DUPLICATE_MOBILE_NUMBER_IN_DATASET.toString(), action);
            rejectedMothers.put(motherImportRejection.getRegistrationNo(), motherImportRejection);
            rejectionStatus.put(motherImportRejection.getRegistrationNo(), motherImportRejection.getAccepted());
            rejected++;
        }
        List<Map<String, Object>> acceptedRchMothers = rchMotherRecordsSet.get(1);

        Map<Long, Set<Long>> hpdMap = getHpdFilters();
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (Map<String, Object> recordMap : acceptedRchMothers) {
            boolean hpdValidation = validateHpdUser(hpdMap,
                    (long) recordMap.get(KilkariConstants.STATE_ID),
                    (long) recordMap.get(KilkariConstants.DISTRICT_ID));
            if (hpdValidation) {
                recordList.add(recordMap);
            }
        }

        LocationFinder locationFinder = locationService.updateLocations(recordList);
        recordList = mctsBeneficiaryImportReaderService.sortByMobileNumber(recordList, false);

        Timer timer = new Timer("mom", "moms");
        List<List<Map<String, Object>>> recordListArray = mctsBeneficiaryImportReaderService.splitRecords(recordList, KilkariConstants.MOBILE_NO);

        LOGGER.debug("Thread Processing Start");
        Integer recordsProcessed = 0;
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<ThreadProcessorObject>> list = new ArrayList<>();

        for (int i = 0; i < recordListArray.size(); i++) {
            Callable<ThreadProcessorObject> callable = new MotherCsvThreadProcessor(recordListArray.get(i), false, SubscriptionOrigin.RCH_IMPORT, locationFinder,
                    mctsBeneficiaryValueProcessor, mctsBeneficiaryImportService);
            Future<ThreadProcessorObject> future = executor.submit(callable);
            list.add(future);
        }

        for (Future<ThreadProcessorObject> fut : list) {
            try {
                ThreadProcessorObject threadProcessorObject = fut.get();
                Map<String,Object> rejectedBen = threadProcessorObject.getRejectedBeneficiaries();
                rejectedMothers.putAll(rejectedBen);
                int currentRej = rejectedBen.size();
                rejected += currentRej;
                Integer currentRecordsPro = threadProcessorObject.getRecordsProcessed();
                saved += currentRecordsPro - currentRej;
                rejectionStatus.putAll(threadProcessorObject.getRejectionStatus());
                recordsProcessed += currentRecordsPro;
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                LOGGER.error("Error while running thread", e);
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Error while Terminating thread", e);
        }
        LOGGER.debug("Thread Processing End");

        LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(recordsProcessed));


        try {
            mctsBeneficiaryImportService.createOrUpdateRchMotherRejections(rejectedMothers , rejectionStatus);
        } catch (RuntimeException e) {
            LOGGER.error(BULK_REJECTION_ERROR_MESSAGE, e);

        }
        LOGGER.info("RCH import: {} state, Total: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, saved, rejected, null);
    }

    private  List<Map<String, Object>> getLMPValidRecords(List<RchMotherRecord> motherRecords) {
        List<Map<String, Object>> validMotherRecords = new ArrayList<>();
        Map<String, Object> rejectedMothers = new HashMap<>();
        Map<String, Object> rejectionStatus = new HashMap<>();
        MotherImportRejection motherImportRejection;

        for (RchMotherRecord record : motherRecords) {
            Map<String, Object> recordMap = toMap(record);
            MctsMother mother;
            Long msisdn;
            String beneficiaryId;
            String action = KilkariConstants.CREATE;
            beneficiaryId = (String) recordMap.get(KilkariConstants.RCH_ID);
            String mctsId = (String) recordMap.get(KilkariConstants.MCTS_ID);
            msisdn = (Long) recordMap.get(KilkariConstants.MOBILE_NO);
            DateTime lmp = (DateTime) recordMap.get(KilkariConstants.LMP);
            mother = mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance(beneficiaryId, mctsId);
            recordMap.put(KilkariConstants.RCH_MOTHER, mother);

            if (mother == null) {
                motherImportRejection = motherRejectionRch(convertMapToRchMother(recordMap), false, RejectionReasons.DATA_INTEGRITY_ERROR.toString(), action);
                rejectedMothers.put(motherImportRejection.getRegistrationNo(), motherImportRejection);
                rejectionStatus.put(motherImportRejection.getRegistrationNo(), motherImportRejection.getAccepted());
            } else {
                if (!mctsBeneficiaryImportService.validateReferenceDate(lmp, SubscriptionPackType.PREGNANCY, msisdn, beneficiaryId, SubscriptionOrigin.MCTS_IMPORT)) {
                    motherImportRejection = motherRejectionRch(convertMapToRchMother(recordMap), false, RejectionReasons.INVALID_LMP_DATE.toString(), action);
                    rejectedMothers.put(motherImportRejection.getRegistrationNo(), motherImportRejection);
                    rejectionStatus.put(motherImportRejection.getRegistrationNo(), motherImportRejection.getAccepted());
                } else {
                    action = mother.getId() == null ? KilkariConstants.CREATE : KilkariConstants.UPDATE;
                    recordMap.put(KilkariConstants.ACTION, action);
                    validMotherRecords.add(recordMap);
                }
            }
        }

        try {
            mctsBeneficiaryImportService.createOrUpdateRchMotherRejections(rejectedMothers , rejectionStatus);
        } catch (RuntimeException e) {
            LOGGER.error(BULK_REJECTION_ERROR_MESSAGE, e);

        }

        return validMotherRecords;
    }

    private RchImportAudit saveImportedChildrenData(RchChildrenDataSet childrenDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {  //NOPMD NcssMethodCount
        LOGGER.info("Starting RCH children import for state {}", stateName);
        List<RchChildRecord> childRecords = childrenDataSet.getRecords();
        List<Map<String, Object>> validChildRecords = new ArrayList<>();
        validChildRecords = getDOBValidChildRecords(childRecords);
        List<List<Map<String, Object>>> rchChildRecordsSet = cleanRchChildRecords(validChildRecords);
        List<Map<String, Object>> rejectedRchChildren = rchChildRecordsSet.get(0);
        String action = "";

        int saved = 0;
        int rejected = childRecords.size() - validChildRecords.size();

        Map<String, Object> rejectedChilds = new HashMap<>();
        Map<String, Object> rejectionStatus = new HashMap<>();
        ChildImportRejection childImportRejection;

        for (Map<String, Object> record : rejectedRchChildren) {
            action = (String) record.get(KilkariConstants.ACTION);
            LOGGER.debug("Existing Child Record with same MSISDN in the data set");
            childImportRejection = childRejectionRch(convertMapToRchChild(record), false, RejectionReasons.DUPLICATE_MOBILE_NUMBER_IN_DATASET.toString(), action);
            rejectedChilds.put(childImportRejection.getRegistrationNo(), childImportRejection);
            rejectionStatus.put(childImportRejection.getRegistrationNo(), childImportRejection.getAccepted());

            rejected++;
        }
        List<Map<String, Object>> acceptedRchChildren = rchChildRecordsSet.get(1);

        Map<Long, Set<Long>> hpdMap = getHpdFilters();
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (Map<String, Object> recordMap : acceptedRchChildren) {
            boolean hpdValidation = validateHpdUser(hpdMap,
                    (long) recordMap.get(KilkariConstants.STATE_ID),
                    (long) recordMap.get(KilkariConstants.DISTRICT_ID));
            if (hpdValidation) {
                recordList.add(recordMap);
            }
        }

        LocationFinder locationFinder = locationService.updateLocations(recordList);
        recordList = mctsBeneficiaryImportReaderService.sortByMobileNumber(recordList, false);

        Timer timer = new Timer("kid", "kids");

        List<List<Map<String, Object>>> recordListArray = mctsBeneficiaryImportReaderService.splitRecords(recordList, KilkariConstants.MOBILE_NO);

        LOGGER.debug("Thread Processing Start");
        Integer recordsProcessed = 0;
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<ThreadProcessorObject>> list = new ArrayList<>();

        for (int i = 0; i < recordListArray.size(); i++) {
            Callable<ThreadProcessorObject> callable = new ChildCsvThreadProcessor(recordListArray.get(i), false, SubscriptionOrigin.RCH_IMPORT, locationFinder,
                    mctsBeneficiaryValueProcessor, mctsBeneficiaryImportService);
            Future<ThreadProcessorObject> future = executor.submit(callable);
            list.add(future);
        }

        for (Future<ThreadProcessorObject> fut : list) {
            try {
                ThreadProcessorObject threadProcessorObject = fut.get();
                Map<String,Object> currRejBen = threadProcessorObject.getRejectedBeneficiaries();
                Integer currRejBenSize = currRejBen.size();
                rejectedChilds.putAll(currRejBen);
                rejectionStatus.putAll(threadProcessorObject.getRejectionStatus());
                Integer currentRecordsProcessed = threadProcessorObject.getRecordsProcessed();
                recordsProcessed += currentRecordsProcessed;
                rejected += currRejBenSize;
                saved += recordsProcessed - currRejBenSize;

            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                LOGGER.error("Error while running thread", e);
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Error while Terminating thread", e);
        }
        LOGGER.debug("Thread Processing End");

        LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(recordsProcessed));


        try {
            mctsBeneficiaryImportService.createOrUpdateRchChildRejections(rejectedChilds , rejectionStatus);
        } catch (RuntimeException e) {
            LOGGER.error(BULK_REJECTION_ERROR_MESSAGE, e);

        }
        LOGGER.info("RCH import: {} state, Total: {} children imported, {} children rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, saved, rejected, null);
    }

    private  List<Map<String, Object>> getDOBValidChildRecords(List<RchChildRecord> childRecords) {
        List<Map<String, Object>> validChildRecords = new ArrayList<>();
        Map<String, Object> rejectedChilds = new HashMap<>();
        Map<String, Object> rejectionStatus = new HashMap<>();
        ChildImportRejection childImportRejection;

        for (RchChildRecord record : childRecords) {
            Map<String, Object> recordMap = toMap(record);
            MctsChild child;
            Long msisdn;
            String childId;
            String action = KilkariConstants.CREATE;
            childId = (String) recordMap.get(KilkariConstants.RCH_ID);
            String mctsId = (String) recordMap.get(KilkariConstants.MCTS_ID);
            msisdn = (Long) recordMap.get(KilkariConstants.MOBILE_NO);
            DateTime dob = (DateTime) recordMap.get(KilkariConstants.DOB);
            // add child to the record
            child = mctsBeneficiaryValueProcessor.getOrCreateRchChildInstance(childId, mctsId);
            recordMap.put(KilkariConstants.RCH_CHILD, child);


            if (child == null) {
                childImportRejection = childRejectionRch(convertMapToRchChild(recordMap), false, RejectionReasons.DATA_INTEGRITY_ERROR.toString(), action);
                rejectedChilds.put(childImportRejection.getRegistrationNo(), childImportRejection);
                rejectionStatus.put(childImportRejection.getRegistrationNo(), childImportRejection.getAccepted());
            } else {
                if (!mctsBeneficiaryImportService.validateReferenceDate(dob, SubscriptionPackType.CHILD, msisdn, childId, SubscriptionOrigin.RCH_IMPORT)) {
                    childImportRejection = childRejectionRch(convertMapToRchChild(recordMap), false, RejectionReasons.INVALID_DOB.toString(), action);
                    rejectedChilds.put(childImportRejection.getRegistrationNo(), childImportRejection);
                    rejectionStatus.put(childImportRejection.getRegistrationNo(), childImportRejection.getAccepted());
                } else {
                    action = (child.getId() == null) ? KilkariConstants.CREATE : KilkariConstants.UPDATE;
                    recordMap.put(KilkariConstants.ACTION, action);
                    validChildRecords.add(recordMap);
                }
            }
        }

        try {
            mctsBeneficiaryImportService.createOrUpdateRchChildRejections(rejectedChilds , rejectionStatus);
        } catch (RuntimeException e) {
            LOGGER.error(BULK_REJECTION_ERROR_MESSAGE, e);

        }

        return validChildRecords;
    }

    private RchImportAudit saveImportedAshaData(RchAnmAshaDataSet anmAshaDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) { //NOPMD NcssMethodCount // NO CHECKSTYLE Cyclomatic Complexity
        LOGGER.info("Starting RCH ASHA import for state {}", stateName);
        List<List<RchAnmAshaRecord>> rchAshaRecordsSet = cleanRchFlwRecords(anmAshaDataSet.getRecords());
        List<RchAnmAshaRecord> rejectedRchAshas = rchAshaRecordsSet.get(0);
        String action = "";
        for (RchAnmAshaRecord record : rejectedRchAshas) {
            action = this.rchFlwActionFinder(record);
            LOGGER.debug("Existing Asha Record with same MSISDN in the data set");
            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.DUPLICATE_MOBILE_NUMBER_IN_DATASET.toString(), action));
        }
        List<RchAnmAshaRecord> acceptedRchAshas = rchAshaRecordsSet.get(1);

        int saved = 0;
        int rejected = 0;
        State state = stateDataService.findByCode(stateCode);

        for (RchAnmAshaRecord record : acceptedRchAshas) {
            try {
                action = this.rchFlwActionFinder(record);
                String designation = record.getGfType();
                designation = (designation != null ? designation.trim() : designation);
                Long msisdn = Long.parseLong(record.getMobileNo());
                String flwId = record.getGfId().toString();
                FrontLineWorker flw = frontLineWorkerService.getByContactNumber(msisdn);
                if ((flw != null && (!flwId.equals(flw.getMctsFlwId()) || !state.equals(flw.getState())))  && !FrontLineWorkerStatus.ANONYMOUS.equals(flw.getStatus())) {
                    LOGGER.debug("Existing FLW with same MSISDN but different MCTS ID");
                    flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), action));
                    rejected++;
                } else {
                    if (!(FlwConstants.ASHA_TYPE.equalsIgnoreCase(designation))) {
                        flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.FLW_TYPE_NOT_ASHA.toString(), action));
                        rejected++;
                    } else {
                        try {
                            // get user property map
                            Map<String, Object> recordMap = record.toFlwRecordMap();    // temp var used for debugging
                            frontLineWorkerImportService.importRchFrontLineWorker(recordMap, state);
                            flwRejectionService.createUpdate(flwRejectionRch(record, true, null, action));
                            saved++;
                        } catch (InvalidLocationException e) {
                            LOGGER.warn("Invalid location for FLW: ", e);
                            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.INVALID_LOCATION.toString(), action));
                            rejected++;
                        } catch (FlwExistingRecordException e) {
                            LOGGER.debug("Existing FLW with same MSISDN but different RCH ID", e);
                            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), action));
                            rejected++;
                        } catch (GfStatusInactiveException e) {
                            LOGGER.debug("The flw has already resigned.");
                            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.GF_STATUS_INACTIVE.toString(), action));
                            rejected++;
                        } catch (Exception e) {
                            LOGGER.error("RCH Flw import Error. Cannot import FLW with ID: {}, and MSISDN (Mobile_No): {}",
                                    record.getGfId(), record.getMobileNo(), e);
                            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.FLW_IMPORT_ERROR.toString(), action));
                            rejected++;
                        }
                    }
                    if ((saved + rejected) % THOUSAND == 0) {
                        LOGGER.debug("RCH import: {} state, Progress: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Mobile number either not present or is not in number format");
                flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT.toString(), action));
            }
        }
        LOGGER.info("RCH import: {} state, Total: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, saved, rejected, null);
    }

    private Map<String, Object> toMap(RchMotherRecord motherRecord) {
        Map<String, Object> map = new HashMap<>();

        toMapLocMother(map, motherRecord);

        map.put(KilkariConstants.MCTS_ID, motherRecord.getMctsIdNo());
        map.put(KilkariConstants.RCH_ID, motherRecord.getRegistrationNo());
        map.put(KilkariConstants.BENEFICIARY_NAME, motherRecord.getName());
        map.put(KilkariConstants.MOBILE_NO, mctsBeneficiaryValueProcessor.getMsisdnByString(motherRecord.getMobileNo()));
        map.put(KilkariConstants.LMP, mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getLmpDate()));
        map.put(KilkariConstants.MOTHER_DOB, mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getBirthDate()));
        map.put(KilkariConstants.ABORTION_TYPE, mctsBeneficiaryValueProcessor.getAbortionDataFromString(motherRecord.getAbortionType()));
        map.put(KilkariConstants.DELIVERY_OUTCOMES, mctsBeneficiaryValueProcessor.getStillBirthFromString(String.valueOf(motherRecord.getDeliveryOutcomes())));
        map.put(KilkariConstants.DEATH, mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(motherRecord.getEntryType())));
        map.put(KilkariConstants.EXECUTION_DATE, "".equals(motherRecord.getExecDate()) ? null : mctsBeneficiaryValueProcessor.getLocalDateByString(motherRecord.getExecDate()));
        map.put(KilkariConstants.CASE_NO, mctsBeneficiaryValueProcessor.getCaseNoByString(motherRecord.getCaseNo().toString()));

        return map;
    }

    private void toMapLocMother(Map<String, Object> map, RchMotherRecord motherRecord) {
        map.put(KilkariConstants.STATE_ID, motherRecord.getStateId());
        map.put(KilkariConstants.DISTRICT_ID, motherRecord.getDistrictId());
        map.put(KilkariConstants.DISTRICT_NAME, motherRecord.getDistrictName());
        map.put(KilkariConstants.TALUKA_ID, motherRecord.getTalukaId());
        map.put(KilkariConstants.TALUKA_NAME, motherRecord.getTalukaName());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, motherRecord.getHealthBlockId());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, motherRecord.getHealthBlockName());
        map.put(KilkariConstants.PHC_ID, motherRecord.getPhcId());
        map.put(KilkariConstants.PHC_NAME, motherRecord.getPhcName());
        map.put(KilkariConstants.SUB_CENTRE_ID, motherRecord.getSubCentreId());
        map.put(KilkariConstants.SUB_CENTRE_NAME, motherRecord.getSubCentreName());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, motherRecord.getVillageId());
        map.put(KilkariConstants.VILLAGE_NAME, motherRecord.getVillageName());
    }

    private void toMapDistrict(Map<String, Object> map, RchDistrictRecord districtRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, districtRecord.getDistrictCode());
        map.put(KilkariConstants.DISTRICT_NAME, districtRecord.getDistrictName());
        map.put(KilkariConstants.EXEC_DATE, districtRecord.getExecDate());
    }

    private void toMapTaluka(Map<String, Object> map, RchTalukaRecord talukaRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, talukaRecord.getDistrictCode());
        map.put(KilkariConstants.TALUKA_ID, talukaRecord.getTalukaCode());
        map.put(KilkariConstants.TALUKA_NAME, talukaRecord.getTalukaName());
        map.put(KilkariConstants.EXEC_DATE, talukaRecord.getExecDate());
    }

    private void toMapVillage(Map<String, Object> map, RchVillageRecord villageRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, villageRecord.getDistrictCode());
        map.put(KilkariConstants.TALUKA_ID, villageRecord.getTalukaCode());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, villageRecord.getVillageCode());
        map.put(KilkariConstants.VILLAGE_NAME, villageRecord.getVillageName());
        map.put(KilkariConstants.EXEC_DATE, villageRecord.getExecDate());
    }

    private void toMapHealthBlock(Map<String, Object> map, RchHealthBlockRecord healthBlockRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, healthBlockRecord.getDistrictCode());
        map.put(KilkariConstants.TALUKA_ID, healthBlockRecord.getTalukaCode());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, healthBlockRecord.getHealthBlockCode());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, healthBlockRecord.getHealthBlockName());
        map.put(KilkariConstants.EXEC_DATE, healthBlockRecord.getExecDate());
    }

    private void toMapTalukaHealthBlock(Map<String, Object> map, RchTalukaHealthBlockRecord talukaHealthBlockRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.TALUKA_ID, talukaHealthBlockRecord.getTalukaCode());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, talukaHealthBlockRecord.getHealthBlockCode());
        map.put(KilkariConstants.EXEC_DATE, talukaHealthBlockRecord.getExecDate());
    }

    private void toMapHealthFacility(Map<String, Object> map, RchHealthFacilityRecord healthFacilityRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, healthFacilityRecord.getDistrictCode());
        map.put(KilkariConstants.TALUKA_ID, healthFacilityRecord.getTalukaCode());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, healthFacilityRecord.getHealthBlockCode());
        map.put(KilkariConstants.HEALTH_FACILITY_ID, healthFacilityRecord.getHealthFacilityCode());
        map.put(KilkariConstants.HEALTH_FACILITY_NAME, healthFacilityRecord.getHealthFacilityName());
        map.put(KilkariConstants.EXEC_DATE, healthFacilityRecord.getExecDate());
    }

    private void toMapHealthSubFacility(Map<String, Object> map, RchHealthSubFacilityRecord healthSubFacilityRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, healthSubFacilityRecord.getDistrictCode());
        map.put(KilkariConstants.TALUKA_ID, healthSubFacilityRecord.getTalukaCode());
        map.put(KilkariConstants.HEALTH_FACILITY_ID, healthSubFacilityRecord.getHealthFacilityCode());
        map.put(KilkariConstants.HEALTH_SUB_FACILITY_ID, healthSubFacilityRecord.getHealthSubFacilityCode());
        map.put(KilkariConstants.HEALTH_SUB_FACILITY_NAME, healthSubFacilityRecord.getHealthSubFacilityName());
        map.put(KilkariConstants.EXEC_DATE, healthSubFacilityRecord.getExecDate());
    }

    private void toMapVillageHealthSubFacility(Map<String, Object> map, RchVillageHealthSubFacilityRecord villageHealthSubFacilityRecord, Long stateCode) {
        map.put(KilkariConstants.CSV_STATE_ID, stateCode);
        map.put(KilkariConstants.DISTRICT_ID, villageHealthSubFacilityRecord.getDistrictCode());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, villageHealthSubFacilityRecord.getVillageCode());
        map.put(KilkariConstants.HEALTH_SUB_FACILITY_ID, villageHealthSubFacilityRecord.getHealthSubFacilityCode());
        map.put(KilkariConstants.EXEC_DATE, villageHealthSubFacilityRecord.getExecDate());
    }

    private Map<String, Object> toMap(RchChildRecord childRecord) {
        Map<String, Object> map = new HashMap<>();

        toMapLocChild(map, childRecord);

        map.put(KilkariConstants.BENEFICIARY_NAME, childRecord.getName());

        map.put(KilkariConstants.MOBILE_NO, mctsBeneficiaryValueProcessor.getMsisdnByString(childRecord.getMobileNo()));
        map.put(KilkariConstants.DOB, mctsBeneficiaryValueProcessor.getDateByString(childRecord.getBirthdate()));

        map.put(KilkariConstants.MCTS_ID, childRecord.getMctsId());
        map.put(KilkariConstants.MCTS_MOTHER_ID,
                mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(childRecord.getMctsMotherIdNo()) == null ? null : mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(childRecord.getMctsMotherIdNo()).getBeneficiaryId());
        map.put(KilkariConstants.RCH_ID, childRecord.getRegistrationNo());
        map.put(KilkariConstants.RCH_MOTHER_ID, childRecord.getMotherRegistrationNo());
        map.put(KilkariConstants.DEATH,
                mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(childRecord.getEntryType())));
        map.put(KilkariConstants.EXECUTION_DATE, "".equals(childRecord.getExecDate()) ? null : mctsBeneficiaryValueProcessor.getLocalDateByString(childRecord.getExecDate()));

        return map;
    }

    private void toMapLocChild(Map<String, Object> map, RchChildRecord childRecord) {
        map.put(KilkariConstants.STATE_ID, childRecord.getStateId());
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

    private void toMapLoc(Map<String, Object> map, RchAnmAshaRecord anmAshaRecord) {
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

    private Map<Long, Set<Long>> getHpdFilters() {
        Map<Long, Set<Long>> hpdMap = new HashMap<>();
        String locationProp = settingsFacade.getProperty(Constants.HPD_STATES);
        if (StringUtils.isBlank(locationProp)) {
            return hpdMap;
        }

        String[] locationParts = StringUtils.split(locationProp, ',');
        for (String locationPart : locationParts) {
            Long stateId = Long.valueOf(locationPart);
            hpdMap.put(stateId, getHpdForState(stateId));
        }

        return hpdMap;
    }


    private Set<Long> getHpdForState(Long stateId) {

        Set<Long> districtSet = new HashSet<>();
        String hpdProp = settingsFacade.getProperty(Constants.BASE_HPD_CONFIG + stateId);
        if (StringUtils.isBlank(hpdProp)) {
            return districtSet;
        }

        String[] districtParts = StringUtils.split(hpdProp, ',');
        for (String districtPart : districtParts) {
            districtSet.add(Long.valueOf(districtPart));
        }

        return districtSet;
    }

    private boolean validateHpdUser(Map<Long, Set<Long>> hpdFilters, long stateId, long districtId) {

        // if we have the state for hpd filter
        if (hpdFilters.containsKey(stateId)) {
            // if district exists in the hpd filter set
            Set<Long> districtSet = hpdFilters.get(stateId);
            if (districtSet != null) {
                return districtSet.contains(districtId);
            }
        }

        return true;
    }

    @Transactional
    private void deleteRchImportFailRecords(final LocalDate startReferenceDate, final LocalDate endReferenceDate, final RchUserType rchUserType, final Long stateId) {

        LOGGER.debug("Deleting nms_rch_failures records which are successfully imported");
        if (startReferenceDate.equals(endReferenceDate)) {
            LOGGER.debug("No failed imports in the past 7days ");
        } else {
            QueryParams queryParams = new QueryParams(new Order("importDate", Order.Direction.ASC));
            List<RchImportFailRecord> failedImports = rchImportFailRecordDataService.getByStateAndImportdateAndUsertype(stateId, startReferenceDate, rchUserType, queryParams);
            int counter = 0;
            for (RchImportFailRecord eachFailedImport : failedImports) {
                rchImportFailRecordDataService.delete(eachFailedImport);
                counter++;
            }
            LOGGER.debug("Deleted {} rows from nms_rch_failures", counter);
        }
    }

    private int sizeNullSafe(Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    private File localResponseDir() {
        return new File(this.settingsFacade.getProperty(LOCAL_RESPONSE_DIR));
    }

    private void scpResponseToRemote(String fileName) {
        String remoteDir = settingsFacade.getProperty(REMOTE_RESPONSE_DIR);
        String command = "scp " + localResponseFile(fileName) + " " + remoteDir;
        ExecutionHelper execHelper = new ExecutionHelper();
        execHelper.exec(command, getScpTimeout());
    }

    private File scpResponseToLocal(String fileName, String remoteLocation) {
        String localDir = settingsFacade.getProperty(LOCAL_RESPONSE_DIR);

        String command = "scp " + remoteResponseFile(fileName, remoteLocation) + " " + localDir;
        ExecutionHelper execHelper = new ExecutionHelper();
        execHelper.exec(command, getScpTimeout());
        return new File(localResponseFile(fileName));
    }

    private File fileForLocUpdate(String fileName) {
        return new File(remoteResponseFile(fileName, null));
    }

    private File fileForXmlLocUpdate(String fileName) {
        return new File(remoteResponseFileForXml(fileName));
    }

    public String localResponseFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_RESPONSE_DIR);
        localFile += localFile.endsWith("/") ? "" : "/";
        localFile += file;
        return localFile;
    }

    public String remoteResponseFile(String file, String remoteLocation) {
        if (remoteLocation == null) {
            remoteLocation = settingsFacade.getProperty(REMOTE_RESPONSE_DIR);
        }
        remoteLocation += remoteLocation.endsWith("/") ? "" : "/";
        remoteLocation += file;
        return remoteLocation;
    }

    public String remoteResponseFileForXml(String file) {
        String remoteFile = settingsFacade.getProperty(REMOTE_RESPONSE_DIR_XML);
        remoteFile += remoteFile.endsWith("/") ? "" : "/";
        remoteFile += file;
        return remoteFile;
    }

    private Long getScpTimeout() {
        try {
            return Long.parseLong(settingsFacade.getProperty(SCP_TIMEOUT_SETTING));
        } catch (NumberFormatException e) {
            return SCP_TIME_OUT;
        }
    }

    private DS_DataResponseDS_DataResult readResponses(File file) throws RchFileManipulationException {
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String xml = "";
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                xml += currentLine;
            }

            return (DS_DataResponseDS_DataResult) deserializeAxisObject(DS_DataResponseDS_DataResult.class, xml);
        } catch (Exception e) {
            throw new RchFileManipulationException("Failed to read response file."); //NOPMD
        }
    }

    private String readResponsesFromXml(File file) throws RchFileManipulationException {
        try {
            String xmlString;
            String string = FileUtils.readFileToString(file);
            String[] newDataSetArr = string.split("NewDataSet");
            if(newDataSetArr.length > 2) {
                xmlString = "<NewDataSet" + newDataSetArr[3] + "NewDataSet>";
                xmlString = xmlString.replaceAll("\\n", " ");
                xmlString = xmlString.replaceAll("<Records diffgr:id=\"Records[0-9]+\" msdata:rowOrder=\"[0-9]+\">", "<Records >");
                return xmlString;
            }
        } catch (Exception e) {
            throw new RchFileManipulationException("Failed to read response file.", e); //NOPMD
        }
        return "";
    }


    private String serializeAxisObject(Object obj) throws IOException {
        try {

            if (obj == null) {
                return null;
            }
            StringWriter outStr = new StringWriter();
            TypeDesc typeDesc = getAxisTypeDesc(obj);
            QName qname = typeDesc.getXmlType();
            String lname = qname.getLocalPart();
            if (lname.startsWith(">") && lname.length() > 1) {
                lname = lname.substring(1);
            }
            qname = new QName(qname.getNamespaceURI(), lname);
            AxisServer server = new AxisServer();
            BeanSerializer ser = new BeanSerializer(obj.getClass(), qname, typeDesc);
            SerializationContext ctx = new SerializationContext(outStr,
                    new MessageContext(server));
            ctx.setSendDecl(false);
            ctx.setDoMultiRefs(false);
            ctx.setPretty(true);
            try {
                ser.serialize(qname, new AttributesImpl(), obj, ctx);
            } catch (final Exception e) {
                throw new Exception("Unable to serialize object "
                        + obj.getClass().getName(), e);
            }

            String xml = outStr.toString();
            return xml; //NOPMD

        } catch (Exception e) {
            throw new IOException("Serialization failed", e);
        }
    }

    private Object deserializeAxisObject(Class<?> cls, String xml)
            throws IOException {
        //CHECKSTYLE:OFF
        try {
            
            final String SOAP_START = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header /><soapenv:Body>";
            final String SOAP_START_XSI = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soapenv:Header /><soapenv:Body>";
            final String SOAP_END = "</soapenv:Body></soapenv:Envelope>";

            //CHECKSTYLE:ON
            Object result = null;
            try {
                Message message = new Message(SOAP_START + xml + SOAP_END);
                result = message.getSOAPEnvelope().getFirstBody()
                        .getObjectValue(cls);
            } catch (Exception e) {
                try {
                    Message message = new Message(SOAP_START_XSI + xml + SOAP_END);
                    result = message.getSOAPEnvelope().getFirstBody()
                            .getObjectValue(cls);
                } catch (Exception e1) {
                    throw new Exception(e1); //NOPMD
                }
            }
            return result;
        } catch (Exception e) {
            throw new IOException("Deserialization failed", e); //NOPMD
        }
    }

    private TypeDesc getAxisTypeDesc(Object obj) throws Exception { //NOPMD
        final Class<? extends Object> objClass = obj.getClass();
        try {
            final Method methodGetTypeDesc = objClass.getMethod("getTypeDesc",
                    new Class[]{});
            final TypeDesc typeDesc = (TypeDesc) methodGetTypeDesc.invoke(obj,
                    new Object[]{});
            return (typeDesc);
        } catch (final Exception e) {
            throw new Exception("Unable to get Axis TypeDesc for "
                    + objClass.getName(), e); //NOPMD
        }
    }

    private String rchFlwActionFinder(RchAnmAshaRecord record) {
        if (frontLineWorkerService.getByMctsFlwIdAndState(record.getGfId().toString(), stateDataService.findByCode(record.getStateId())) == null) {
            LOGGER.info("create");
            return "CREATE";
        } else {
            LOGGER.info("update");
            return "UPDATE";
        }
    }

    @Transactional
    public void locationUpdateInTable(Long stateId, RchUserType rchUserType) {
        try {
            List<RchImportFacilitator> rchImportFiles = rchImportFacilitatorService.findByStateIdAndRchUserType(stateId, rchUserType);

            Collections.sort(rchImportFiles, new Comparator<RchImportFacilitator>() {
                public int compare(RchImportFacilitator m1, RchImportFacilitator m2) {
                    return m1.getImportDate().compareTo(m2.getImportDate()); //ascending order
                }
            });

            for (RchImportFacilitator rchImportFile : rchImportFiles
            ) {
                File remoteResponseFile = fileForXmlLocUpdate(rchImportFile.getFileName());

                if (remoteResponseFile.exists() && !remoteResponseFile.isDirectory()) {

                    LOGGER.debug("Started reading file {}.", rchImportFile.getFileName());
                    String result = readResponsesFromXml(remoteResponseFile);
                    LOGGER.debug("Completed Reading Responses");
                    if (result.contains(RECORDS)) {
                        if (rchUserType == RchUserType.MOTHER) {
                            motherLocUpdate(result, stateId, rchUserType);
                        } else if (rchUserType == RchUserType.CHILD) {
                            childLocUpdate(result, stateId, rchUserType);
                        } else if (rchUserType == RchUserType.ASHA) {
                            ashaLocUpdate(result, stateId, rchUserType);
                        }
                    } else {
                        String warning = String.format("No mother data set received from RCH for %d stateId", stateId);
                        LOGGER.warn(warning);
                    }

                } else {
                    continue;
                }

            }
        } catch (ExecutionException e) {
            LOGGER.error("Failed to copy file from remote server to local directory." + e);
        } catch (RchFileManipulationException e) {
            LOGGER.error("No files saved d : {}", e);
        }
    }

    @Transactional
    public void locationUpdateInTableFromCsv(Long stateId, RchUserType rchUserType) throws IOException {

        List<MultipartFile> rchImportFiles = findByStateIdAndRchUserType(stateId, rchUserType);

        Collections.sort(rchImportFiles, new Comparator<MultipartFile>() {
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

        for (MultipartFile rchImportFile : rchImportFiles) {
            try (InputStream in = rchImportFile.getInputStream()) {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                Map<String, CellProcessor> cellProcessorMapper;
                List<Map<String, Object>> recordList;
                LOGGER.debug("Started reading file {}.", rchImportFile.getOriginalFilename());
                if (rchUserType == RchUserType.MOTHER) {
                    cellProcessorMapper = mctsBeneficiaryImportService.getRchMotherProcessorMapping();
                    recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    motherLocUpdateFromCsv(recordList, stateId, rchUserType);
                } else if (rchUserType == RchUserType.CHILD) {
                    cellProcessorMapper = mctsBeneficiaryImportReaderService.getRchChildProcessorMapping();
                    recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    childLocUpdateFromCsv(recordList, stateId, rchUserType);
                } else if (rchUserType == RchUserType.ASHA) {
                    cellProcessorMapper = mctsBeneficiaryImportService.getRchAshaProcessorMapping();
                    recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                    ashaLocUpdateFromCsv(recordList, stateId, rchUserType);
                }

            }
        }
    }

    @Override
    public String getLocationFilesDirectory() {
        return settingsFacade.getProperty(REMOTE_RESPONSE_DIR_LOCATION);
    }


    private void motherLocUpdate(String result, Long stateId, RchUserType rchUserType) { // NO CHECKSTYLE Cyclomatic Complexity
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();

            RchMothersDataSet mothersDataSet = (result == null) ?
                    null :
                    (RchMothersDataSet) MarshallUtils.unmarshall(result, RchMothersDataSet.class);

            LOGGER.debug("Unmarshall Completed");
            if (mothersDataSet == null || mothersDataSet.getRecords() == null) {
                String warning = String.format("No mother data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
            } else {
                List<RchMotherRecord> motherRecords = mothersDataSet.getRecords();
                LOGGER.debug("Records read {}", motherRecords.size());
                List<String> existingMotherIds = getDatabaseMothers(motherRecords);
                for (RchMotherRecord record : motherRecords) {
                    if(existingMotherIds.contains(record.getRegistrationNo())) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapLocMother(locMap, record);
                        locMap.put(KilkariConstants.RCH_ID, record.getRegistrationNo());
                        locArrList.add(locMap);
                    }
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, rchUserType);
            }
        } catch (JAXBException e) {
            throw new RchInvalidResponseStructureException(String.format("Cannot deserialize RCH mother data from %d stateId.", stateId), e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH mothers data from stateId: %d. Response Deserialization Error", stateId);
            LOGGER.error(error, e);
        } catch (IOException e) {
            LOGGER.error("Input output exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Invalid location");
        }
    }




    private void motherLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, RchUserType rchUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            List<RchMotherRecord> rchMotherRecords = new ArrayList<>();

            for (Map<String, Object> record : result) {
                RchMotherRecord rchMotherRecord = convertMapToRchMother(record);
                rchMotherRecords.add(rchMotherRecord);
            }
            List<String> existingMotherIds = getDatabaseMothers(rchMotherRecords);
            for(RchMotherRecord rchMotherRecord : rchMotherRecords) {
                if (existingMotherIds.contains(rchMotherRecord.getRegistrationNo())) {
                    Map<String, Object> locMap = new HashMap<>();
                    toMapLocMother(locMap, rchMotherRecord);
                    locMap.put(KilkariConstants.RCH_ID, rchMotherRecord.getRegistrationNo());
                    locArrList.add(locMap);
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, rchUserType);
            }

        }  catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
        }
    }




    private void childLocUpdate(String result, Long stateId, RchUserType rchUserType) { // NO CHECKSTYLE Cyclomatic Complexity
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            RchChildrenDataSet childrenDataSet = (result == null) ?
                    null :
                    (RchChildrenDataSet) MarshallUtils.unmarshall(result, RchChildrenDataSet.class);

            if (childrenDataSet == null || childrenDataSet.getRecords() == null) {
                String warning = String.format("No child data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
            } else {
                List<RchChildRecord> childRecords = childrenDataSet.getRecords();
                LOGGER.debug("Records read {}", childRecords.size());
                List<String> existingChildIds = getDatabaseChild(childRecords);
                for (RchChildRecord record : childRecords) {
                    if(existingChildIds.contains(record.getRegistrationNo())) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapLocChild(locMap, record);
                        locMap.put(KilkariConstants.RCH_ID, record.getRegistrationNo());
                        locArrList.add(locMap);
                    }
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, rchUserType);
            }

        } catch (JAXBException e) {
            throw new RchInvalidResponseStructureException(String.format("Cannot deserialize RCH children data from %d stateId.", stateId), e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH children data from stateId:%d. Response Deserialization Error", stateId);
            LOGGER.error(error, e);
        } catch (IOException e) {
            LOGGER.error("Input output exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Invalid location");
        }
    }

    private void childLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, RchUserType rchUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            List<RchChildRecord> rchChildRecords = new ArrayList<>();

            for (Map<String, Object> record : result) {
                RchChildRecord rchChildRecord = convertMapToRchChild(record);
                rchChildRecords.add(rchChildRecord);

            }
            List<String> existingMotherIds = getDatabaseChild(rchChildRecords);
            for(RchChildRecord rchChildRecord : rchChildRecords) {
                if (existingMotherIds.contains(rchChildRecord.getRegistrationNo())) {
                    Map<String, Object> locMap = new HashMap<>();
                    toMapLocChild(locMap, rchChildRecord);
                    locMap.put(KilkariConstants.RCH_ID, rchChildRecord.getRegistrationNo());
                    locArrList.add(locMap);
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, rchUserType);
            }

        } catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
        }
    }




    private void ashaLocUpdate(String result, Long stateId, RchUserType rchUserType) { // NO CHECKSTYLE Cyclomatic Complexity
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            RchAnmAshaDataSet ashaDataSet = (result == null) ?
                    null :
                    (RchAnmAshaDataSet) MarshallUtils.unmarshall(result, RchAnmAshaDataSet.class);
            if (ashaDataSet == null || ashaDataSet.getRecords() == null) {
                String warning = String.format("No FLW data set received from RCH for %d stateId", stateId);
                LOGGER.warn(warning);
            } else {
                List<RchAnmAshaRecord> anmAshaRecords = ashaDataSet.getRecords();
                LOGGER.debug("Records read {}", anmAshaRecords.size());
                State state = stateDataService.findByCode(stateId);
                List<FrontLineWorker> existingAshas = getDatabaseAsha(anmAshaRecords,state.getId());
                Map<String, Long> existingAshaIds = new HashMap<>();
                List<String> mctsIds = new ArrayList<>();
                for (FrontLineWorker asha : existingAshas) {
                    existingAshaIds.put(asha.getMctsFlwId(), asha.getId());
                    mctsIds.add(asha.getMctsFlwId());
                }
                for (RchAnmAshaRecord record : anmAshaRecords
                ) {
                    if(mctsIds.contains(record.getGfId().toString())) {
                        Map<String, Object> locMap = new HashMap<>();
                        toMapLoc(locMap, record);
                        locMap.put(FlwConstants.ID, existingAshaIds.get(record.getGfId().toString()));
                        locMap.put(FlwConstants.GF_ID, record.getGfId());
                        locArrList.add(locMap);
                    }
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, rchUserType);
            }
        } catch (JAXBException e) {
            throw new RchInvalidResponseStructureException(String.format("Cannot deserialize RCH FLW data from %d stateId.", stateId), e);
        } catch (RchInvalidResponseStructureException e) {
            String error = String.format("Cannot read RCH FLW data from stateId:%d. Response Deserialization Error", stateId);
            LOGGER.error(error, e);
        } catch (IOException e) {
            LOGGER.error("Input output exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Invalid location");
        }

    }

    private void ashaLocUpdateFromCsv(List<Map<String, Object>> result, Long stateId, RchUserType rchUserType) {
        try {
            ArrayList<Map<String, Object>> locArrList = new ArrayList<>();
            List<RchAnmAshaRecord> rchAshaRecords = new ArrayList<>();
            for (Map<String, Object> record : result) {
                RchAnmAshaRecord rchAnmAshaRecord = frontLineWorkerImportService.convertMapToRchAsha(record);
                rchAshaRecords.add(rchAnmAshaRecord);
            }
            State state = stateDataService.findByCode(stateId);
            List<FrontLineWorker> existingAshas = getDatabaseAsha(rchAshaRecords,state.getId());
            Map<String, Long> existingAshaIds = new HashMap<>();
            List<String> mctsIds = new ArrayList<>();
            for (FrontLineWorker asha : existingAshas) {
                existingAshaIds.put(asha.getMctsFlwId(), asha.getId());
                mctsIds.add(asha.getMctsFlwId());
            }
            for(RchAnmAshaRecord rchAnmAshaRecord : rchAshaRecords) {
                if (mctsIds.contains(rchAnmAshaRecord.getGfId().toString())) {
                    Map<String, Object> locMap = new HashMap<>();
                    toMapLoc(locMap, rchAnmAshaRecord);
                    locMap.put(FlwConstants.ID, existingAshaIds.get(rchAnmAshaRecord.getGfId().toString()));
                    locMap.put(FlwConstants.GF_ID, rchAnmAshaRecord.getGfId());
                    locArrList.add(locMap);
                }
            }
            if (!locArrList.isEmpty()) {
                updateLocInMap(locArrList, stateId, rchUserType);
            }

        } catch (IOException e) {
            LOGGER.error("IO exception.");
        } catch (InvalidLocationException e) {
            LOGGER.error("Location Invalid");
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
                Long talukaCode = Long.parseLong(record.get(KilkariConstants.TALUKA_ID) == null ? "0" : record.get(KilkariConstants.TALUKA_ID).toString().trim());
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
                mapKey = record.get(KilkariConstants.STATE_ID).toString() + "_" + districtCode;
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

    private  List<MultipartFile> findByStateIdAndRchUserType(Long stateId, RchUserType rchUserType) throws IOException {

        ArrayList <MultipartFile> csvFilesByStateIdAndRchUserType = new ArrayList<>();
        String locUpdateDir = settingsFacade.getProperty(REMOTE_RESPONSE_DIR_CSV);
        File file = new File(locUpdateDir);

        File[] files = file.listFiles();
        if (files != null) {
            for(File f: files){
                String[] fileNameSplitter =  f.getName().split("_");
                if(Objects.equals(fileNameSplitter[2], stateId.toString()) && fileNameSplitter[3].equalsIgnoreCase(rchUserType.toString())){
                    try {
                        FileItem fileItem = new DiskFileItem("file",  "text/plain", false, f.getName(), (int) f.length(), f.getParentFile());
                        IOUtils.copy(new FileInputStream(f), fileItem.getOutputStream());
                        MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
                        csvFilesByStateIdAndRchUserType.add(multipartFile);
                    }catch(IOException e) {
                        LOGGER.debug("IO Exception", e);
                    }

                }
            }
        }

        return csvFilesByStateIdAndRchUserType;
    }



    private void updateLocInMap(List<Map<String, Object>> locArrList, Long stateId, RchUserType rchUserType) throws InvalidLocationException, IOException {

        ArrayList<Map<String, Object>> updatedLocArrList = new ArrayList<>();

        LocationFinder locationFinder = locationService.updateLocations(locArrList);

        for (Map<String, Object> record : locArrList
        ) {
            Map<String, Object> updatedMap = setLocationFields(locationFinder, record);
            if("asha".equalsIgnoreCase(rchUserType.toString())){
                updatedMap.put(FlwConstants.GF_ID, record.get(FlwConstants.GF_ID));
                updatedMap.put(FlwConstants.ID, record.get(FlwConstants.ID));
            }else {
                updatedMap.put(KilkariConstants.RCH_ID, record.get(KilkariConstants.RCH_ID));
            }
            updatedLocArrList.add(updatedMap);
        }

        if ("asha".equalsIgnoreCase(rchUserType.toString())) {
            csvWriterAsha(updatedLocArrList, stateId, rchUserType);
        }else {
            csvWriterKilkari(updatedLocArrList, stateId, rchUserType);
        }

    }

    @Override
    public String getBeneficiaryLocationUpdateDirectory() {
        return settingsFacade.getProperty(LOC_UPDATE_DIR_RCH);
    }

    private File csvWriter(Long stateId, RchUserType rchUserType) throws IOException {
        String locUpdateDir = settingsFacade.getProperty(LOC_UPDATE_DIR_RCH);
        String fileName = locUpdateDir + "location_update_state" + "_" + stateId + "_" + rchUserType + "_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ".csv";
        File csvFile = new File(fileName);
        if (!csvFile.exists()){
            csvFile.createNewFile();
        } else {
            LOGGER.debug("File already exists");
        }
        return csvFile;

    }

    private void csvWriterKilkari(List<Map<String, Object>> locArrList, Long stateId, RchUserType rchUserType) throws IOException { //NO CHECKSTYLE Cyclomatic Complexity //NOPMD NcssMethodCount

        if (!locArrList.isEmpty()) {
            File csvFile = csvWriter(stateId, rchUserType);
            FileWriter writer;
            writer = new FileWriter(csvFile, true);

            writer.write(KilkariConstants.RCH_ID);
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
                writer.write(map.get(KilkariConstants.RCH_ID).toString());
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

    private void csvWriterAsha(List<Map<String, Object>> locArrList, Long stateId, RchUserType rchUserType) throws IOException { //NO CHECKSTYLE Cyclomatic Complexity //NOPMD NcssMethodCount


        if (!locArrList.isEmpty()) {
            File csvFile = csvWriter(stateId, rchUserType);
            FileWriter writer;
            writer = new FileWriter(csvFile, true);

            writer.write(FlwConstants.ID);
            writer.write(TAB);
            writer.write(FlwConstants.GF_ID);
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
                writer.write(map.get(FlwConstants.GF_ID).toString());
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
                writer.write(map.get(FlwConstants.VILLAGE_NAME) == null ? "" : map.get(FlwConstants.VILLAGE_NAME).toString());
                writer.write(NEXT_LINE);
            }

            writer.close();
        }
    }

    private List<String> getDatabaseMothers(final List<RchMotherRecord> motherRecords) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<String>> queryExecution = new SqlQueryExecution<List<String>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT rchId FROM nms_mcts_mothers WHERE rchId IN " + queryIdList(motherRecords);
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

        List<String> result = (List<String>) rchImportFacilitatorDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Database mothers query time {}", queryTimer.time());
        return result;

    }

    private String queryIdList(List<RchMotherRecord> motherRecords) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append("(");
        for (RchMotherRecord motherRecord: motherRecords) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(QUOTATION + motherRecord.getRegistrationNo() + QUOTATION);
            i++;
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }



    private List<String> getDatabaseChild(final List<RchChildRecord> childRecords) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<String>> queryExecution = new SqlQueryExecution<List<String>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT rchId FROM nms_mcts_children WHERE rchId IN " + queryIdListChildren(childRecords);
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

        List<String> result = (List<String>) rchImportFacilitatorDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Database child query time {}", queryTimer.time());
        return result;

    }

    private String queryIdListChildren(List<RchChildRecord> childRecords) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append("(");
        for (RchChildRecord childRecord: childRecords) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(QUOTATION + childRecord.getRegistrationNo() + QUOTATION);
            i++;
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }


    private List<FrontLineWorker> getDatabaseAsha(final List<RchAnmAshaRecord> ashaRecords, final long stateID) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<FrontLineWorker>> queryExecution = new SqlQueryExecution<List<FrontLineWorker>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * FROM nms_front_line_workers WHERE state_id_OID = " + stateID +
                        " and mctsFlwId IN (SELECT mctsFlwId from nms_front_line_workers WHERE state_id_OID = " + stateID +
                        " group by mctsFlwId having count(*) = 1) " +
                        " and  mctsFlwId IN " + queryIdListAsha(ashaRecords);
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public List<FrontLineWorker> execute(Query query) {
                query.setClass(FrontLineWorker.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                return (List<FrontLineWorker>) fqr;
            }
        };

        List<FrontLineWorker> result = rchImportFacilitatorDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Database asha's query time {}", queryTimer.time());
        return result;

    }

    private String queryIdListAsha(List<RchAnmAshaRecord> ashaRecords) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        stringBuilder.append("(");
        for (RchAnmAshaRecord ashaRecord: ashaRecords) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(QUOTATION + ashaRecord.getGfId() + QUOTATION);
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


    private List<Long> getStateIds() {
        String locationProp = settingsFacade.getProperty(Constants.RCH_LOCATIONS);
        if (StringUtils.isBlank(locationProp)) {

            return Collections.emptyList();
        }

        String[] locationParts = StringUtils.split(locationProp, ',');

        List<Long> stateIds = new ArrayList<>();
        for (String locationPart : locationParts) {
            stateIds.add(Long.valueOf(locationPart));
        }

        return stateIds;
    }



}