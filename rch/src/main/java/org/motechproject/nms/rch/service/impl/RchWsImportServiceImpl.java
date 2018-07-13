package org.motechproject.nms.rch.service.impl;


import org.joda.time.LocalDate;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.Order;
import org.motechproject.nms.rch.domain.RchImportAudit;
import org.motechproject.nms.rch.domain.RchImportFailRecord;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.RchWebServiceException;
import org.motechproject.nms.rch.repository.RchImportAuditDataService;
import org.motechproject.nms.rch.repository.RchImportFailRecordDataService;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.service.RchWsImportService;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.annotations.Transactional;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("rchWsImportService")
public class RchWsImportServiceImpl implements RchWsImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RchWsImportServiceImpl.class);
    private static final String RCH_WEB_SERVICE = "RCH Web Service";

    @Autowired
    private StateDataService stateDataService;

    @Autowired
    private RchWebServiceFacade rchWebServiceFacade;

    @Autowired
    private AlertService alertService;

    @Autowired
    private RchImportAuditDataService rchImportAuditDataService;

    @Autowired
    private RchImportFailRecordDataService rchImportFailRecordDataService;

    /**
     * Event relay service to handle async notifications
     */
    @Autowired
    private EventRelay eventRelay;

    @Override
    public void startRchImport() {
        eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_IMPORT_EVENT));
    }

    @Override
    public void importFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting import from RCH web service");
        LOGGER.info("Pulling data for {}, for states {}", referenceDate, stateIds);

        if (endpoint == null) {
            LOGGER.debug("Using default service endpoint from WSDL");
        } else {
            LOGGER.debug("Using custom endpoint {}", endpoint);
        }

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.MOTHER, referenceDate, endpoint, Constants.RCH_MOTHER_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.CHILD, referenceDate, endpoint, Constants.RCH_CHILD_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.ASHA, referenceDate, endpoint, Constants.RCH_ASHA_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.TALUKA, referenceDate, endpoint, Constants.RCH_TALUKA_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.HEALTHBLOCK, referenceDate, endpoint, Constants.RCH_HEALTHBLOCK_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.TALUKAHEALTHBLOCK, referenceDate, endpoint, Constants.RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT);
        }

        LOGGER.info("Initiated import workflow from RCH for mothers and children");
    }

    @MotechListener(subjects = { Constants.RCH_MOTHER_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchMothersData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping RCH Mother import for this state", stateId);
            LOGGER.error(error);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.MOTHER, stateId, null, 0, 0, error));
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            if (rchWebServiceFacade.getMothersData(startDate, endDate, endpoint, stateId)) {
                LOGGER.info("RCH Responses for state id {} recorded to file successfully.", stateId);
            }
        } catch (RchWebServiceException e) {
            String error = String.format("Cannot read RCH mothers data from %s state with state id: %d", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Mother Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.MOTHER, stateId));
        }
    }

    @MotechListener(subjects = { Constants.RCH_TALUKA_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchTalukaData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping Taluka import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            if (rchWebServiceFacade.getTalukasData(startDate, endDate, endpoint, stateId)) {
                LOGGER.info("RCH Responses for state id {} recorded to file successfully.", stateId);
            }
        } catch (RchWebServiceException e) {
            String error = String.format("Cannot read RCH taluka data from %s state with state id: %d", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Taluka Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKA, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKA, stateId));
        }
    }

    @MotechListener(subjects = { Constants.RCH_HEALTHBLOCK_IMPORT_SUBJECT})
    @Transactional
    @Override
    public void importRchHealthBlockData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LOGGER.debug("stateId = {}", stateId);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping HealthBlock import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            if (rchWebServiceFacade.getHealthBlockData(startDate, endDate, endpoint, stateId)) {
                LOGGER.info("RCH Responses for state id {} recorded to file successfully.", stateId);
            }
        } catch (RchWebServiceException e) {
            String error = String.format("Cannot read RCH healthblock data from %s state with state id: %d", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Taluka Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.HEALTHBLOCK, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.HEALTHBLOCK, stateId));
        }
    }

    @MotechListener(subjects = { Constants.RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT})
    @Transactional
    @Override
    public void importRchTalukaHealthBlockData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping HealthBlock import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            if (rchWebServiceFacade.getTalukaHealthBlockData(startDate, endDate, endpoint, stateId)) {
                LOGGER.info("RCH Responses for state id {} recorded to file successfully.", stateId);
            }
        } catch (RchWebServiceException e) {
            String error = String.format("Cannot read RCH taluka-healthblock data from %s state with state id: %d", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Taluka healthBlock Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startDate, endDate, RchUserType.TALUKAHEALTHBLOCK, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endDate, RchUserType.TALUKAHEALTHBLOCK, stateId));
        }
    }

    @MotechListener(subjects = { Constants.RCH_CHILD_IMPORT_SUBJECT })
    @Transactional
    public void importRchChildrenData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s does not exist in database. Skipping RCH Children import for this state", stateId);
            LOGGER.error(error);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateId, null, 0, 0, error));
            return;
        }
        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {

            if (rchWebServiceFacade.getChildrenData(startReferenceDate, endReferenceDate, endpoint, stateId)) {
                LOGGER.info("RCH Child responses for state id {} recorded to file successfully.");
            }
        } catch (RchWebServiceException e) {
            String error = String.format("Cannot read RCH children data from %s state with state id: %d", stateName, stateCode);
            LOGGER.error(error, e);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Child Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
        }
    }

    @MotechListener(subjects = { Constants.RCH_ASHA_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchAshaData(MotechEvent motechEvent) {
        LOGGER.info("Asha import entry point");
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s does not exist in database. Skipping RCH FLW import for this state.", stateId);
            LOGGER.error(error);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateId, null, 0, 0, error));
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();

        try {
            if (rchWebServiceFacade.getAnmAshaData(startReferenceDate, endReferenceDate, endpoint, stateId)) {
                LOGGER.info("RCH FLW responses for state id {} recorded to file successfully.");
            }
        } catch (RchWebServiceException e) {
            String error = String.format("Cannot read FLW data from %s state with state id: %d", stateName, stateCode);
            LOGGER.error(error);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service Asha Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
        }
    }

    private void sendImportEventForAUserType(Long stateId, RchUserType userType, LocalDate referenceDate, URL endpoint, String importSubject) {

        LOGGER.debug("Fetching all the failed imports in the last 7 days for stateId {} and UserType {}", stateId, userType);
        QueryParams queryParams = new QueryParams(new Order("importDate", Order.Direction.ASC));
        List<RchImportFailRecord> failedImports = rchImportFailRecordDataService.getByStateAndImportdateAndUsertype(stateId, referenceDate.minusDays(6), userType, queryParams);
        LOGGER.info("failedImports {}", failedImports);
        LocalDate startDate = failedImports.isEmpty() ? referenceDate : failedImports.get(0).getImportDate();
        LOGGER.info("fromDate {}", startDate);
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(Constants.START_DATE_PARAM, startDate);
        eventParams.put(Constants.END_DATE_PARAM, referenceDate);
        eventParams.put(Constants.STATE_ID_PARAM, stateId);
        eventParams.put(Constants.ENDPOINT_PARAM, endpoint);
        LOGGER.debug("Sending import message for stateId {} and UserType {}", stateId, userType);
        eventRelay.sendEventMessage(new MotechEvent(importSubject, eventParams));
    }
}
