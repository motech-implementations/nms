package org.motechproject.nms.rch.service.impl;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.ser.BeanSerializer;
import org.apache.axis.server.AxisServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.Order;
import org.motechproject.nms.flw.exception.FlwExistingRecordException;
import org.motechproject.nms.flw.exception.FlwImportException;
import org.motechproject.nms.flw.utils.FlwConstants;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.rch.contract.RchAnmAshaDataSet;
import org.motechproject.nms.rch.contract.RchAnmAshaRecord;
import org.motechproject.nms.rch.contract.RchChildRecord;
import org.motechproject.nms.rch.contract.RchChildrenDataSet;
import org.motechproject.nms.rch.contract.RchMotherRecord;
import org.motechproject.nms.rch.contract.RchMothersDataSet;
import org.motechproject.nms.rch.domain.RchImportAudit;
import org.motechproject.nms.rch.domain.RchImportFailRecord;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.ExecutionException;
import org.motechproject.nms.rch.exception.RchFileManipulationException;
import org.motechproject.nms.rch.exception.RchInvalidResponseStructureException;
import org.motechproject.nms.rch.exception.RchWebServiceException;
import org.motechproject.nms.rch.repository.RchImportAuditDataService;
import org.motechproject.nms.rch.repository.RchImportFailRecordDataService;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.soap.DS_DataResponseDS_DataResult;
import org.motechproject.nms.rch.soap.Irchwebservices;
import org.motechproject.nms.rch.soap.RchwebservicesLocator;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.rch.utils.ExecutionHelper;
import org.motechproject.nms.rch.utils.MarshallUtils;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;


import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.io.StringWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

@Service("rchWebServiceFacade")
public class RchWebServiceFacadeImpl implements RchWebServiceFacade {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String LOCAL_RESPONSE_DIR = "rch.local_response_dir";
    private static final String REMOTE_RESPONSE_DIR = "rch.remote_response_dir";
    private static final String READ_MOTHER_RESPONSE_FILE_EVENT = "nms.rch.read_mother_response_file";
    private static final String READ_CHILD_RESPONSE_FILE_EVENT = "nms.rch.read_child_response_file";
    private static final String READ_ASHA_RESPONSE_FILE_EVENT = "nms.rch.read_asha_response_file";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("dd-MM-yyyy");
    private static final String SCP_TIMEOUT_SETTING = "rch.scp_timeout";
    private static final Long SCP_TIME_OUT = 60000L;
    private static final String RCH_WEB_SERVICE = "RCH Web Service";
    private static final double THOUSAND = 1000d;
    private static final String FILENAME = "fileName";

    @Autowired
    @Qualifier("rchSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    @Qualifier("rchServiceLocator")
    private RchwebservicesLocator rchServiceLocator;

    @Autowired
    private MotechSchedulerService schedulerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RchWebServiceFacadeImpl.class);

    @Autowired
    private RchImportAuditDataService rchImportAuditDataService;

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

    @Override
    public boolean getMothersData(LocalDate from, LocalDate to, URL endpoint, Long stateId) {
        DS_DataResponseDS_DataResult result;
        Irchwebservices dataService = getService(endpoint);
        boolean status = false;

        try {
            result = dataService.DS_Data(settingsFacade.getProperty(Constants.RCH_PROJECT_ID), settingsFacade.getProperty(Constants.RCH_USER_ID),
                    settingsFacade.getProperty(Constants.RCH_PASSWORD), from.toString(DATE_FORMAT), to.toString(DATE_FORMAT), stateId.toString(),
                    settingsFacade.getProperty(Constants.RCH_MOTHER_USER), settingsFacade.getProperty(Constants.RCH_DTID));
        } catch (RemoteException e) {
            throw new RchWebServiceException("Remote Server Error. Could Not Read Mother Data.", e);
        }

        LOGGER.debug("writing response to file");
        File responseFile = generateResponseFile(result, RchUserType.MOTHER, stateId);
        if (responseFile != null) {
            LOGGER.info("response successfully written to file. Copying to remote directory.");
            try {
                scpResponseToRemote(responseFile.getName());
                LOGGER.info("response file successfully copied to remote server");

                Map<String, Object> eventParams = new HashMap<>();
                eventParams.put(Constants.START_DATE_PARAM, from);
                eventParams.put(Constants.END_DATE_PARAM, to);
                eventParams.put(Constants.STATE_ID_PARAM, stateId);
                eventParams.put(FILENAME, responseFile.getName());
                eventParams.put("userType", RchUserType.MOTHER);
                MotechEvent event = new MotechEvent(READ_MOTHER_RESPONSE_FILE_EVENT, eventParams);
                String cronExpression = Constants.DEFAULT_RCH_IMPORT_CRON_EXPRESSION;
                CronSchedulableJob job = new CronSchedulableJob(event, cronExpression);
                schedulerService.safeScheduleJob(job);
                status = true;

            } catch (ExecutionException e) {
                LOGGER.error("error copying file to remote server.");
            }
        } else {
            LOGGER.error("Error writing response to file.");
        }
        return status;
    }

    @MotechListener(subjects = { READ_MOTHER_RESPONSE_FILE_EVENT })
    public void readMotherResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.debug(event.toString());

        LOGGER.info("Copying mother response file from remote server to local directory.");
        File localResponseFile;
        try {
            localResponseFile = scpResponseToLocal((String) event.getParameters().get(FILENAME));

            if (localResponseFile != null) {
               LOGGER.info("Mother response file successfully copied from remote server to local directory.");
            }
            DS_DataResponseDS_DataResult result = readResponses(localResponseFile);
            Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
            State state = stateDataService.findByCode(stateId);

            String stateName = state.getName() != null ? state.getName() : " ";
            Long stateCode = state.getCode() != null ? state.getCode() : 1L;

            LocalDate startDate = (LocalDate) event.getParameters().get(Constants.START_DATE_PARAM);
            LocalDate endDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);

            try {
                validMothersDataResponse(result, stateId);
                List motherResultFeed = result.get_any()[1].getChildren();

                RchMothersDataSet mothersDataSet = (motherResultFeed == null) ?
                        null :
                        (RchMothersDataSet) MarshallUtils.unmarshall(motherResultFeed.get(0).toString(), RchMothersDataSet.class);

                LOGGER.info("Starting mother import");
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (mothersDataSet == null || mothersDataSet.getRecords() == null) {
                    String warning = String.format("No mother data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, warning));
                    return;
                }
                LOGGER.info("Received {} mother records from MCTS for {} state", sizeNullSafe(mothersDataSet.getRecords()), stateName);

                RchImportAudit audit = saveImportedMothersData(mothersDataSet, stateName, stateCode, startDate, endDate);
                rchImportAuditDataService.create(audit);
                stopWatch.stop();
                double seconds = stopWatch.getTime() / THOUSAND;
                LOGGER.info("Finished mother import dispatch in {} seconds. Accepted {} mothers, Rejected {} mothers",
                        seconds, audit.getAccepted(), audit.getRejected());

                deleteRchImportFailRecords(startDate, endDate, RchUserType.MOTHER, stateId);

            } catch (JAXBException e) {
                throw new RchInvalidResponseStructureException(String.format("Cannot deserialize mother data from %s location.", stateId), e);
            } catch (RchInvalidResponseStructureException e) {
                String error = String.format("Cannot read mothers data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
                LOGGER.error(error, e);
                alertService.create(RCH_WEB_SERVICE, "RCH Web Service Mother Import", e
                        .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
                rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.MOTHER, stateId));
            }
        } catch (ExecutionException e) {
            LOGGER.error("Failed to copy file from remote server to local directory." + e);
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
            throw new RchWebServiceException("Remote Server Error. Could Not Read Children Data.", e);
        }

        LOGGER.debug("writing response to file");
        File responseFile = generateResponseFile(result, RchUserType.CHILD, stateId);
        if (responseFile != null) {
            LOGGER.info("response successfully written to file. Copying to remote directory.");
            try {
                scpResponseToRemote(responseFile.getName());
                LOGGER.info("response file successfully copied to remote server");

                Map<String, Object> eventParams = new HashMap<>();
                eventParams.put(Constants.STATE_ID_PARAM, stateId);
                eventParams.put(FILENAME, responseFile.getName());
                eventParams.put("userType", RchUserType.CHILD);
                MotechEvent event = new MotechEvent(READ_CHILD_RESPONSE_FILE_EVENT, eventParams);
                String cronExpression = Constants.DEFAULT_RCH_IMPORT_CRON_EXPRESSION;
                CronSchedulableJob job = new CronSchedulableJob(event, cronExpression);
                schedulerService.safeScheduleJob(job);
                status = true;
            } catch (ExecutionException e) {
                LOGGER.error("error copying file to remote server.");
            }

        } else {
            LOGGER.error("Error writing response to file.");
        }

        return status;
    }

    @MotechListener(subjects = { READ_CHILD_RESPONSE_FILE_EVENT })
    public void readChildResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.debug(event.toString());

        LOGGER.info("Copying child response file from remote server to local directory.");
        File localResponseFile;
        try {
            localResponseFile = scpResponseToLocal((String) event.getParameters().get(FILENAME));

            DS_DataResponseDS_DataResult result = readResponses(localResponseFile);
            Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
            State state = stateDataService.findByCode(stateId);

            String stateName = state.getName();
            Long stateCode = state.getCode();

            LocalDate startReferenceDate = (LocalDate) event.getParameters().get(Constants.START_DATE_PARAM);
            LocalDate endReferenceDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);

            try {
                validChildrenDataResponse(result, stateId);
                List childResultFeed = result.get_any()[1].getChildren();
                RchChildrenDataSet childrenDataSet = (childResultFeed == null) ?
                        null :
                        (RchChildrenDataSet) MarshallUtils.unmarshall(childResultFeed.get(0).toString(), RchChildrenDataSet.class);

                LOGGER.info("Starting children import for stateId: {}", stateId);
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (childrenDataSet == null || childrenDataSet.getRecords() == null) {
                    String warning = String.format("No child data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, warning));
                    return;
                }
                LOGGER.info("Received {} children records from RCH for {} state", sizeNullSafe(childrenDataSet.getRecords()), stateName);

                RchImportAudit audit = saveImportedChildrenData(childrenDataSet, stateName, stateCode, startReferenceDate, endReferenceDate);
                rchImportAuditDataService.create(audit);
                stopWatch.stop();
                double seconds = stopWatch.getTime() / THOUSAND;
                LOGGER.info("Finished children import dispatch in {} seconds. Accepted {} children, Rejected {} children",
                        seconds, audit.getAccepted(), audit.getRejected());

                // Delete RchImportFailRecords once import is successful
                deleteRchImportFailRecords(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateId);
            } catch (JAXBException e) {
                throw new RchInvalidResponseStructureException(String.format("Cannot deserialize children data from %s location.", stateId), e);
            } catch (RchInvalidResponseStructureException e) {
                String error = String.format("Cannot read children data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
                LOGGER.error(error, e);
                alertService.create(RCH_WEB_SERVICE, "RCH Web Service Child Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
                rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
            }
        } catch (ExecutionException e) {
            LOGGER.error("Failed to copy response file from remote server to local directory.");
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
            throw new RchWebServiceException("Remote Server Error. Could Not Read FLW Data.", e);
        }

        LOGGER.debug("writing response to file");
        File responseFile = generateResponseFile(result, RchUserType.ASHA, stateId);
        if (responseFile != null) {
            LOGGER.info("response successfully written to file. Copying to remote directory.");
            try {
                scpResponseToRemote(responseFile.getName());
                LOGGER.info("response file successfully copied to remote server");

                Map<String, Object> eventParams = new HashMap<>();
                eventParams.put(Constants.STATE_ID_PARAM, stateId);
                eventParams.put(FILENAME, responseFile.getName());
                eventParams.put("userType", RchUserType.ASHA);
                MotechEvent event = new MotechEvent(READ_ASHA_RESPONSE_FILE_EVENT, eventParams);
                readAshaResponseFromFile(event);
                status = true;
            } catch (ExecutionException e) {
                LOGGER.error("error copying file to remote server.");
            } catch (RchFileManipulationException e) {
                LOGGER.error("file error.");
            }

        } else {
            LOGGER.error("Error writing response to file.");
        }

        return status;
    }

    @MotechListener(subjects = { READ_ASHA_RESPONSE_FILE_EVENT })
    public void readAshaResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.debug(event.toString());
        LOGGER.info("Asha file import entry point");
        LOGGER.info("Copying Asha response file from remote server to local directory.");
        File localResponseFile;
        try {
            localResponseFile = scpResponseToLocal((String) event.getParameters().get(FILENAME));

            DS_DataResponseDS_DataResult result = readResponses(localResponseFile);
            Long stateId = (Long) event.getParameters().get(Constants.STATE_ID_PARAM);
            State importState = stateDataService.findByCode(stateId);

            String stateName = importState.getName();
            Long stateCode = importState.getCode();

            LocalDate startReferenceDate = (LocalDate) event.getParameters().get(Constants.START_DATE_PARAM);
            LocalDate endReferenceDate = (LocalDate) event.getParameters().get(Constants.END_DATE_PARAM);

            try {
                validAnmAshaDataResponse(result, stateId);
                List ashaResultFeed = result.get_any()[1].getChildren();
                RchAnmAshaDataSet ashaDataSet = (ashaResultFeed == null) ?
                        null :
                        (RchAnmAshaDataSet) MarshallUtils.unmarshall(ashaResultFeed.get(0).toString(), RchAnmAshaDataSet.class);

                LOGGER.info("Starting FLW import for stateId: {}", stateId);
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                if (ashaDataSet == null || ashaDataSet.getRecords() == null) {
                    String warning = String.format("No FLW data set received from RCH for %s state", stateName);
                    LOGGER.warn(warning);
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, warning));
                    return;
                }
                LOGGER.info("Received {} FLW records from RCH for {} state", sizeNullSafe(ashaDataSet.getRecords()), stateName);

                RchImportAudit audit = saveImportedAshaData(ashaDataSet, stateName, stateCode, startReferenceDate, endReferenceDate);
                rchImportAuditDataService.create(audit);
                stopWatch.stop();
                double seconds = stopWatch.getTime() / THOUSAND;
                LOGGER.info("Finished FLW import dispatch in {} seconds. Accepted {} Ashas, Rejected {} Ashas",
                        seconds, audit.getAccepted(), audit.getRejected());

                // Delete RchImportFailRecords once import is successful
                deleteRchImportFailRecords(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateId);
            } catch (JAXBException e) {
                throw new RchInvalidResponseStructureException(String.format("Cannot deserialize FLW data from %s location.", stateId), e);
            } catch (RchInvalidResponseStructureException e) {
                String error = String.format("Cannot read FLW data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
                LOGGER.error(error, e);
                alertService.create(RCH_WEB_SERVICE, "RCH Web Service FLW Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, error));
                rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
            }
        } catch (ExecutionException e) {
            LOGGER.error("Failed to copy response file from remote server to local directory.");
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
        if (userType.equals(RchUserType.MOTHER)) {
            return String.format("RCH_StateID_%d_Mother_Response_%s.xml", stateId, timeStamp);
        } else if (userType.equals(RchUserType.CHILD)) {
            return String.format("RCH_StateID_%d_Child_Response_%s.xml", stateId, timeStamp);
        } else {
            return String.format("RCH_StateID_%d_Asha_Response_%s.xml", stateId, timeStamp);
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

    private RchImportAudit saveImportedMothersData(RchMothersDataSet mothersDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        LOGGER.info("Starting mother import for state {}", stateName);

        int saved = 0;
        int rejected = 0;
        Map<Long, Set<Long>> hpdMap = getHpdFilters();
        for (RchMotherRecord record : mothersDataSet.getRecords()) {
            try {
                // get user property map
                Map<String, Object> recordMap = toMap(record);

                // validate if user needs to be hpd filtered (true if user can be added)
                boolean hpdValidation = validateHpdUser(hpdMap,
                        (long) recordMap.get(KilkariConstants.STATE_ID),
                        (long) recordMap.get(KilkariConstants.DISTRICT_ID));
                if (hpdValidation && mctsBeneficiaryImportService.importMotherRecord(recordMap, SubscriptionOrigin.RCH_IMPORT)) {
                    saved++;
                } else {
                    rejected++;
                }
            } catch (RuntimeException e) {
                LOGGER.error("Mother import Error. Cannot import Mother with ID: {} for state ID: {}",
                        record.getRegistrationNo(), stateCode, e);
                rejected++;
            }
            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("{} state, Progress: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("{} state, Total: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, saved, rejected, null);
    }

    private RchImportAudit saveImportedChildrenData(RchChildrenDataSet childrenDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        LOGGER.info("Starting children import for state {}", stateName);

        int saved = 0;
        int rejected = 0;
        Map<Long, Set<Long>> hpdMap = getHpdFilters();

        for (RchChildRecord record : childrenDataSet.getRecords()) {
            try {
                // get user property map
                Map<String, Object> recordMap = toMap(record);

                // validate if user needs to be hpd filtered (true if user can be added)
                boolean hpdValidation = validateHpdUser(hpdMap,
                        (long) recordMap.get(KilkariConstants.STATE_ID),
                        (long) recordMap.get(KilkariConstants.DISTRICT_ID));

                if (hpdValidation && mctsBeneficiaryImportService.importChildRecord(toMap(record), SubscriptionOrigin.RCH_IMPORT)) {
                    saved++;
                } else {
                    rejected++;
                }

            } catch (RuntimeException e) {
                LOGGER.error("Child import Error. Cannot import Child with ID: {} for state:{} with state ID: {}",
                        record.getRegistrationNo(), stateName, stateCode, e);
                rejected++;
            }

            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("{} state, Progress: {} children imported, {} children rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("{} state, Total: {} children imported, {} children rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, saved, rejected, null);
    }

    private RchImportAudit saveImportedAshaData(RchAnmAshaDataSet anmAshaDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        LOGGER.info("Starting ASHA import for state {}", stateName);

        int saved = 0;
        int rejected = 0;
        State state = stateDataService.findByCode(stateCode);

        for (RchAnmAshaRecord record : anmAshaDataSet.getRecords()) {
            String designation = record.getGfType();
            designation = (designation != null) ? designation.trim() : designation;
            if (!(FlwConstants.ASHA_TYPE.equalsIgnoreCase(designation))) {
                rejected++;
            } else {
                try {
                    // get user property map
                    Map<String, Object> recordMap = record.toFlwRecordMap();    // temp var used for debugging
                    frontLineWorkerImportService.importRchFrontLineWorker(recordMap, state);
                    saved++;
                } catch (InvalidLocationException e) {
                    LOGGER.warn("Invalid location for FLW: ", e);
                    rejected++;
                } catch (FlwImportException e) {
                    LOGGER.error("Existing FLW with same MSISDN but different RCH ID", e);
                    rejected++;
                } catch (FlwExistingRecordException e) {
                    LOGGER.error("Cannot import FLW with ID: {}, and MSISDN (Mobile_No): {}", record.getGfId(), record.getMobileNo(), e);
                    rejected++;
                } catch (Exception e) {
                    LOGGER.error("Flw import Error. Cannot import FLW with ID: {}, and MSISDN (Mobile_No): {}",
                            record.getGfId(), record.getMobileNo(), e);
                    rejected++;
                }
            }
            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("{} state, Progress: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("{} state, Total: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, saved, rejected, null);
    }

    private Map<String, Object> toMap(RchMotherRecord motherRecord) {
        Map<String, Object> map = new HashMap<>();
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

        map.put(KilkariConstants.MCTS_ID, motherRecord.getMctsIdNo());
        map.put(KilkariConstants.RCH_ID, motherRecord.getRegistrationNo());
        map.put(KilkariConstants.BENEFICIARY_NAME, motherRecord.getName());
        map.put(KilkariConstants.MOBILE_NO, mctsBeneficiaryValueProcessor.getMsisdnByString(motherRecord.getMobileNo()));
        map.put(KilkariConstants.LMP, mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getLmpDate()));
        map.put(KilkariConstants.MOTHER_DOB, mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getBirthDate()));
        map.put(KilkariConstants.ABORTION_TYPE, mctsBeneficiaryValueProcessor.getAbortionDataFromString(motherRecord.getAbortionType()));
        map.put(KilkariConstants.DELIVERY_OUTCOMES, mctsBeneficiaryValueProcessor.getStillBirthFromString(String.valueOf(motherRecord.getDeliveryOutcomes())));
        map.put(KilkariConstants.DEATH, mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(motherRecord.getEntryType())));
        map.put(KilkariConstants.EXECUTION_DATE, "".equals(motherRecord.getExecDate()) ? null : LocalDate.parse(motherRecord.getExecDate(), DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
        map.put(KilkariConstants.CASE_NO, mctsBeneficiaryValueProcessor.getCaseNoByString(motherRecord.getCaseNo().toString()));

        return map;
    }

    private Map<String, Object> toMap(RchChildRecord childRecord) {
        Map<String, Object> map = new HashMap<>();

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

        map.put(KilkariConstants.BENEFICIARY_NAME, childRecord.getName());

        map.put(KilkariConstants.MOBILE_NO, mctsBeneficiaryValueProcessor.getMsisdnByString(childRecord.getMobileNo()));
        map.put(KilkariConstants.DOB, mctsBeneficiaryValueProcessor.getDateByString(childRecord.getBirthdate()));

        map.put(KilkariConstants.MCTS_ID, childRecord.getMctsId());
        map.put(KilkariConstants.MCTS_MOTHER_ID,
                mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(childRecord.getMctsMotherIdNo()));
        map.put(KilkariConstants.RCH_ID, childRecord.getRegistrationNo());
        map.put(KilkariConstants.RCH_MOTHER_ID, childRecord.getMotherRegistrationNo());
        map.put(KilkariConstants.DEATH,
                mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(childRecord.getEntryType())));
        map.put(KilkariConstants.EXECUTION_DATE, "".equals(childRecord.getExecDate()) ? null : LocalDate.parse(childRecord.getExecDate(), DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));

        return map;
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

    private void deleteRchImportFailRecords(final LocalDate startReferenceDate, final LocalDate endReferenceDate, final RchUserType mctsUserType, final Long stateId) {

        LOGGER.debug("Deleting nms_rch_failures records which are successfully imported");
        if (startReferenceDate.equals(endReferenceDate)) {
            LOGGER.debug("No failed imports in the past 7days ");
        } else {
            QueryParams queryParams = new QueryParams(new Order("importDate", Order.Direction.ASC));
            List<RchImportFailRecord> failedImports = rchImportFailRecordDataService.getByStateAndImportdateAndUsertype(stateId, startReferenceDate, mctsUserType, queryParams);
            int counter = 0;
            for (RchImportFailRecord eachFailedImport: failedImports) {
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
        String command = "cp " + localResponseFile(fileName) + " " + remoteDir;
        ExecutionHelper execHelper = new ExecutionHelper();
        execHelper.exec(command, getScpTimeout());
    }

    private File scpResponseToLocal(String fileName) {
        String localDir = settingsFacade.getProperty(LOCAL_RESPONSE_DIR);

        String command = "cp " + remoteResponseFile(fileName) + " " + localDir;
        ExecutionHelper execHelper = new ExecutionHelper();
        execHelper.exec(command, getScpTimeout());
        return new File(localResponseFile(fileName));
    }

    public String localResponseFile(String file) {
        String localFile = settingsFacade.getProperty(LOCAL_RESPONSE_DIR);
        localFile += localFile.endsWith("/") ? "" : "/";
        localFile += file;
        return localFile;
    }

    public String remoteResponseFile(String file) {
        String remoteFile = settingsFacade.getProperty(REMOTE_RESPONSE_DIR);
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
                    new Class[] {});
            final TypeDesc typeDesc = (TypeDesc) methodGetTypeDesc.invoke(obj,
                    new Object[] {});
            return (typeDesc);
        } catch (final Exception e) {
            throw new Exception("Unable to get Axis TypeDesc for "
                    + objClass.getName(), e); //NOPMD
        }
    }
}
