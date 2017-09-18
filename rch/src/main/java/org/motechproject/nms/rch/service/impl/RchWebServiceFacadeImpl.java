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
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.exception.FlwExistingRecordException;
import org.motechproject.nms.flw.exception.FlwImportException;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.utils.FlwConstants;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.rch.contract.RchAnmAshaDataSet;
import org.motechproject.nms.kilkari.contract.RchAnmAshaRecord;
import org.motechproject.nms.kilkari.contract.RchChildRecord;
import org.motechproject.nms.rch.contract.RchChildrenDataSet;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;
import org.motechproject.nms.rch.contract.RchMothersDataSet;
import org.motechproject.nms.rch.domain.RchImportAudit;
import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.domain.RchImportFailRecord;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.ExecutionException;
import org.motechproject.nms.rch.exception.RchFileManipulationException;
import org.motechproject.nms.rch.exception.RchInvalidResponseStructureException;
import org.motechproject.nms.rch.exception.RchWebServiceException;
import org.motechproject.nms.rch.repository.RchImportAuditDataService;
import org.motechproject.nms.rch.repository.RchImportFailRecordDataService;
import org.motechproject.nms.rch.service.RchImportFacilitatorService;
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
import org.motechproject.nms.kilkari.service.ActionFinderService;
import org.motechproject.nms.rejectionhandler.service.ChildRejectionService;
import org.motechproject.nms.rejectionhandler.service.MotherRejectionService;
import org.motechproject.nms.rejectionhandler.service.FlwRejectionService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanRchMotherRecords;
import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanRchChildRecords;
import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanRchFlwRecords;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.motherRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.childRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.flwRejectionRch;

@Service("rchWebServiceFacade")
public class RchWebServiceFacadeImpl implements RchWebServiceFacade {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String LOCAL_RESPONSE_DIR = "rch.local_response_dir";
    private static final String REMOTE_RESPONSE_DIR = "rch.remote_response_dir";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("dd-MM-yyyy");
    private static final String SCP_TIMEOUT_SETTING = "rch.scp_timeout";
    private static final Long SCP_TIME_OUT = 60000L;
    private static final String RCH_WEB_SERVICE = "RCH Web Service";
    private static final double THOUSAND = 1000d;

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
    private FlwRejectionService flwRejectionService;

    @Autowired
    private MotherRejectionService motherRejectionService;

    @Autowired
    private ChildRejectionService childRejectionService;

    @Autowired
    private ActionFinderService actionFinderService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

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
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH Mother Data.", e);
        }

        LOGGER.debug("writing RCH mother response to file");
        File responseFile = generateResponseFile(result, RchUserType.MOTHER, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH mother response successfully written to file. Copying to remote directory.");
            try {
                scpResponseToRemote(responseFile.getName());
                LOGGER.info("RCH mother response file successfully copied to remote server");

                RchImportFacilitator rchImportFacilitator = new RchImportFacilitator(responseFile.getName(), from, to, stateId, RchUserType.MOTHER, LocalDate.now());
                rchImportFacilitatorService.createImportFileAudit(rchImportFacilitator);
                status = true;

            } catch (ExecutionException e) {
                LOGGER.error("error copying file to remote server.");
            } catch (RchFileManipulationException e) {
                LOGGER.error("invalid file name");
            }
        } else {
            LOGGER.error("Error writing response to file.");
        }
        return status;
    }

    @MotechListener(subjects = Constants.RCH_MOTHER_READ_SUBJECT) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void readMotherResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.info("Copying RCH mother response file from remote server to local directory.");
        try {
            List<RchImportFacilitator> rchImportFacilitatorsMother = rchImportFacilitatorService.findByImportDateAndRchUserType(LocalDate.now(), RchUserType.MOTHER);
            LOGGER.info("Files imported today for mothers= " + rchImportFacilitatorsMother.size());
            for (RchImportFacilitator rchImportFacilitatorMother : rchImportFacilitatorsMother
                    ) {
                File localResponseFile = scpResponseToLocal(rchImportFacilitatorMother.getFileName());
                if (localResponseFile != null) {
                    LOGGER.info("RCH Mother response file successfully copied from remote server to local directory.");
                    DS_DataResponseDS_DataResult result = readResponses(localResponseFile);
                    Long stateId = rchImportFacilitatorMother.getStateId();
                    State state = stateDataService.findByCode(stateId);

                    String stateName = state.getName() != null ? state.getName() : " ";
                    Long stateCode = state.getCode() != null ? state.getCode() : 1L;

                    LocalDate startDate = rchImportFacilitatorMother.getStartDate();
                    LocalDate endDate = rchImportFacilitatorMother.getEndDate();

                    try {
                        validMothersDataResponse(result, stateId);
                        List motherResultFeed = result.get_any()[1].getChildren();

                        RchMothersDataSet mothersDataSet = (motherResultFeed == null) ?
                                null :
                                (RchMothersDataSet) MarshallUtils.unmarshall(motherResultFeed.get(0).toString(), RchMothersDataSet.class);

                        LOGGER.info("Starting RCH mother import");
                        StopWatch stopWatch = new StopWatch();
                        stopWatch.start();

                        if (mothersDataSet == null || mothersDataSet.getRecords() == null) {
                            String warning = String.format("No mother data set received from RCH for %s state", stateName);
                            LOGGER.warn(warning);
                            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, warning));
                        } else {
                            LOGGER.info("Received {} mother records from RCH for {} state", sizeNullSafe(mothersDataSet.getRecords()), stateName);

                            RchImportAudit audit = saveImportedMothersData(mothersDataSet, stateName, stateCode, startDate, endDate);
                            rchImportAuditDataService.create(audit);
                            stopWatch.stop();
                            double seconds = stopWatch.getTime() / THOUSAND;
                            LOGGER.info("Finished RCH mother import dispatch in {} seconds. Accepted {} mothers, Rejected {} mothers",
                                    seconds, audit.getAccepted(), audit.getRejected());

                            deleteRchImportFailRecords(startDate, endDate, RchUserType.MOTHER, stateId);
                        }
                    } catch (JAXBException e) {
                        throw new RchInvalidResponseStructureException(String.format("Cannot deserialize RCH mother data from %s location.", stateId), e);
                    } catch (RchInvalidResponseStructureException e) {
                        String error = String.format("Cannot read RCH mothers data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
                        LOGGER.error(error, e);
                        alertService.create(RCH_WEB_SERVICE, "RCH Web Service Mother Import", e
                                .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                        rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
                        rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.MOTHER, stateId));
                    } catch (NullPointerException e) {
                        LOGGER.error("No files saved : ", e);
                    }
                }
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
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH Children Data.", e);
        }

        LOGGER.debug("writing RCH children response to file");
        File responseFile = generateResponseFile(result, RchUserType.CHILD, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH children response successfully written to file. Copying to remote directory.");
            try {
                scpResponseToRemote(responseFile.getName());
                LOGGER.info("RCH children response file successfully copied to remote server");

                RchImportFacilitator rchImportFacilitator = new RchImportFacilitator(responseFile.getName(), from, to, stateId, RchUserType.CHILD, LocalDate.now());
                rchImportFacilitatorService.createImportFileAudit(rchImportFacilitator);
                status = true;
            } catch (ExecutionException e) {
                LOGGER.error("error copying file to remote server.");
            } catch (RchFileManipulationException e) {
                LOGGER.error("invalid file error");
            }

        } else {
            LOGGER.error("Error writing response to file.");
        }

        return status;
    }

    @MotechListener(subjects = Constants.RCH_CHILD_READ_SUBJECT)
    @Transactional
    public void readChildResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.info("Copying RCH child response file from remote server to local directory.");
        try {
            List<RchImportFacilitator> rchImportFacilitatorsChild = rchImportFacilitatorService.findByImportDateAndRchUserType(LocalDate.now(), RchUserType.CHILD);
            LOGGER.info("Files imported today for children= " + rchImportFacilitatorsChild.size());
            for (RchImportFacilitator rchImportFacilitatorChild : rchImportFacilitatorsChild
                    ) {
                File localResponseFile = scpResponseToLocal(rchImportFacilitatorChild.getFileName());
                DS_DataResponseDS_DataResult result = readResponses(localResponseFile);
                Long stateId = rchImportFacilitatorChild.getStateId();
                State state = stateDataService.findByCode(stateId);

                String stateName = state.getName();
                Long stateCode = state.getCode();

                LocalDate startReferenceDate = rchImportFacilitatorChild.getStartDate();
                LocalDate endReferenceDate = rchImportFacilitatorChild.getEndDate();
                try {
                    validChildrenDataResponse(result, stateId);
                    List childResultFeed = result.get_any()[1].getChildren();
                    RchChildrenDataSet childrenDataSet = (childResultFeed == null) ?
                            null :
                            (RchChildrenDataSet) MarshallUtils.unmarshall(childResultFeed.get(0).toString(), RchChildrenDataSet.class);

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
                } catch (JAXBException e) {
                    throw new RchInvalidResponseStructureException(String.format("Cannot deserialize RCH children data from %s location.", stateId), e);
                } catch (RchInvalidResponseStructureException e) {
                    String error = String.format("Cannot read RCH children data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
                    LOGGER.error(error, e);
                    alertService.create(RCH_WEB_SERVICE, "RCH Web Service Child Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
                    rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
                } catch (NullPointerException e) {
                    LOGGER.error("No files saved : ", e);
                }
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
            throw new RchWebServiceException("Remote Server Error. Could Not Read RCH FLW Data.", e);
        }

        LOGGER.debug("writing RCH Asha response to file");
        File responseFile = generateResponseFile(result, RchUserType.ASHA, stateId);
        if (responseFile != null) {
            LOGGER.info("RCH asha response successfully written to file. Copying to remote directory.");
            try {
                scpResponseToRemote(responseFile.getName());
                LOGGER.info("RCH asha response file successfully copied to remote server");

                RchImportFacilitator rchImportFacilitator = new RchImportFacilitator(responseFile.getName(), from, to, stateId, RchUserType.ASHA, LocalDate.now());
                rchImportFacilitatorService.createImportFileAudit(rchImportFacilitator);
                status = true;
            } catch (ExecutionException e) {
                LOGGER.error("error copying file to remote server.");
            } catch (RchFileManipulationException e) {
                LOGGER.error("invalid file error");
            }
        } else {
            LOGGER.error("Error writing response to file.");
        }

        return status;
    }

    @MotechListener(subjects = Constants.RCH_ASHA_READ_SUBJECT)
    @Transactional
    public void readAshaResponseFromFile(MotechEvent event) throws RchFileManipulationException {
        LOGGER.info("RCH Asha file import entry point");
        LOGGER.info("Copying RCH Asha response file from remote server to local directory.");

        try {
            List<RchImportFacilitator> rchImportFacilitatorsAsha = rchImportFacilitatorService.findByImportDateAndRchUserType(LocalDate.now(), RchUserType.ASHA);
            LOGGER.info("Files imported today for ashas= " + rchImportFacilitatorsAsha.size());
            for (RchImportFacilitator rchImportFacilitatorAsha : rchImportFacilitatorsAsha
                    ) {
                File localResponseFile = scpResponseToLocal(rchImportFacilitatorAsha.getFileName());
                DS_DataResponseDS_DataResult result = readResponses(localResponseFile);
                Long stateId = rchImportFacilitatorAsha.getStateId();
                State importState = stateDataService.findByCode(stateId);

                String stateName = importState.getName();
                Long stateCode = importState.getCode();

                LocalDate startReferenceDate = rchImportFacilitatorAsha.getStartDate();
                LocalDate endReferenceDate = rchImportFacilitatorAsha.getEndDate();
                try {
                    validAnmAshaDataResponse(result, stateId);
                    List ashaResultFeed = result.get_any()[1].getChildren();
                    RchAnmAshaDataSet ashaDataSet = (ashaResultFeed == null) ?
                            null :
                            (RchAnmAshaDataSet) MarshallUtils.unmarshall(ashaResultFeed.get(0).toString(), RchAnmAshaDataSet.class);

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
                } catch (JAXBException e) {
                    throw new RchInvalidResponseStructureException(String.format("Cannot deserialize RCH FLW data from %s location.", stateId), e);
                } catch (RchInvalidResponseStructureException e) {
                    String error = String.format("Cannot read RCH FLW data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
                    LOGGER.error(error, e);
                    alertService.create(RCH_WEB_SERVICE, "RCH Web Service FLW Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, error));
                    rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
                } catch (NullPointerException e) {
                    LOGGER.error("No files saved : ", e);
                }
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
        LOGGER.info("Starting RCH mother import for state {}", stateName);
        List<List<RchMotherRecord>> rchMotherRecordsSet = cleanRchMotherRecords(mothersDataSet.getRecords());
        List<RchMotherRecord> rejectedRchMothers = rchMotherRecordsSet.get(0);
        String action = "";
        int saved = 0;
        int rejected = 0;
        for (RchMotherRecord record : rejectedRchMothers) {
            action = actionFinderService.rchMotherActionFinder(record);
            LOGGER.error("Existing Mother Record with same MSISDN in the data set");
            motherRejectionService.createOrUpdateMother(motherRejectionRch(record, false, RejectionReasons.DUPLICATE_MSISDN_IN_DATASET.toString(), action));
            rejected++;
        }
        List<RchMotherRecord> acceptedRchMothers = rchMotherRecordsSet.get(1);
        Map<Long, Set<Long>> hpdMap = getHpdFilters();
        for (RchMotherRecord record : acceptedRchMothers) {
            action = actionFinderService.rchMotherActionFinder(record);
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
                LOGGER.error("RCH Mother import Error. Cannot import Mother with ID: {} for state ID: {}",
                        record.getRegistrationNo(), stateCode, e);
                rejected++;
            }
            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("RCH import: {} state, Progress: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("RCH import: {} state, Total: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, saved, rejected, null);
    }

    private RchImportAudit saveImportedChildrenData(RchChildrenDataSet childrenDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        LOGGER.info("Starting RCH children import for state {}", stateName);
        List<List<RchChildRecord>> rchChildRecordsSet = cleanRchChildRecords(childrenDataSet.getRecords());
        List<RchChildRecord> rejectedRchChildren = rchChildRecordsSet.get(0);
        String action = "";
        int saved = 0;
        int rejected = 0;
        for (RchChildRecord record : rejectedRchChildren) {
            action = actionFinderService.rchChildActionFinder(record);
            LOGGER.error("Existing Child Record with same MSISDN in the data set");
            childRejectionService.createOrUpdateChild(childRejectionRch(record, false, RejectionReasons.DUPLICATE_MSISDN_IN_DATASET.toString(), action));
            rejected++;
        }
        List<RchChildRecord> acceptedRchChildren = rchChildRecordsSet.get(1);

        Map<Long, Set<Long>> hpdMap = getHpdFilters();

        for (RchChildRecord record : acceptedRchChildren) {
            action = actionFinderService.rchChildActionFinder(record);
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
                LOGGER.error("RCH Child import Error. Cannot import Child with ID: {} for state:{} with state ID: {}",
                        record.getRegistrationNo(), stateName, stateCode, e);
                rejected++;
            }

            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("RCH import: {} state, Progress: {} children imported, {} children rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("RCH import: {} state, Total: {} children imported, {} children rejected", stateName, saved, rejected);
        return new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, saved, rejected, null);
    }

    private RchImportAudit saveImportedAshaData(RchAnmAshaDataSet anmAshaDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) { //NOPMD NcssMethodCount // NO CHECKSTYLE Cyclomatic Complexity
        LOGGER.info("Starting RCH ASHA import for state {}", stateName);
        List<List<RchAnmAshaRecord>> rchAshaRecordsSet = cleanRchFlwRecords(anmAshaDataSet.getRecords());
        List<RchAnmAshaRecord> rejectedRchAshas = rchAshaRecordsSet.get(0);
        String action = "";
        for (RchAnmAshaRecord record : rejectedRchAshas) {
            action = this.rchFlwActionFinder(record);
            LOGGER.error("Existing Asha Record with same MSISDN in the data set");
            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.DUPLICATE_MSISDN_IN_DATASET.toString(), action));
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
                if ((flw != null && (!flwId.equals(flw.getMctsFlwId()) || state != flw.getState()))  && flw.getStatus() != FrontLineWorkerStatus.ANONYMOUS) {
                    LOGGER.error("Existing FLW with same MSISDN but different MCTS ID");
                    flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.MSISDN_ALREADY_IN_USE.toString(), action));
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
                        } catch (FlwImportException e) {
                            LOGGER.error("Existing FLW with same MSISDN but different RCH ID", e);
                            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.MSISDN_ALREADY_IN_USE.toString(), action));
                            rejected++;
                        } catch (FlwExistingRecordException e) {
                            LOGGER.error("Cannot import FLW with ID: {}, and MSISDN (Mobile_No): {}", record.getGfId(), record.getMobileNo(), e);
                            flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.RECORD_ALREADY_EXISTS.toString(), action));
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
                flwRejectionService.createUpdate(flwRejectionRch(record, false, RejectionReasons.NUMBER_FORMAT_ERROR.toString(), action));
            }
        }
        LOGGER.info("RCH import: {} state, Total: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
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

    private File scpResponseToLocal(String fileName) {
        String localDir = settingsFacade.getProperty(LOCAL_RESPONSE_DIR);

        String command = "scp " + remoteResponseFile(fileName) + " " + localDir;
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
            return "CREATE";
        } else {
            return "UPDATE";
        }
    }
}
