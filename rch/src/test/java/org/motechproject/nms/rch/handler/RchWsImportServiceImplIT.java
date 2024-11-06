package org.motechproject.nms.rch.handler;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.repository.RchImportAuditDataService;
import org.motechproject.nms.rch.repository.RchImportFacilitatorDataService;
import org.motechproject.nms.rch.repository.RchImportFailRecordDataService;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.service.impl.RchWsImportServiceImpl;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.testing.osgi.BasePaxIT;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RchWsImportServiceImpl.class, MockConfig.class})
public class RchWsImportServiceImplIT extends BasePaxIT {

    @InjectMocks
    private RchWsImportServiceImpl rchWsImportService;

    @Mock
    private RchWebServiceFacade rchWebServiceFacade;

    @Mock
    private EventRelay eventRelay;

    @Mock
    private SettingsFacade settingsFacade;

    @Mock
    private StateDataService stateDataService;

    @Mock
    private RchImportAuditDataService rchImportAuditDataService;

    @Mock
    private RchImportFailRecordDataService rchImportFailRecordDataService;

    @Mock
    private RchImportFacilitatorDataService rchImportFacilitatorDataService;

    @Mock
    private AlertService alertService; // Add this line

    private Long stateId = 1L;
    private String apiResponse = "{\"keel\":\"dummyKeel\",\"deel\":\"dummyDeel\"}";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testImportRchMothersDataFlow() throws Exception {
       setupMocksAndInitialData();

        // Step 4: Trigger the first Motech event to start the import process
        Map<String, Object> params = new HashMap<>();
        params.put("stateId", stateId);
        params.put("fromDate", LocalDate.now().minusDays(7));
        params.put("endDate", LocalDate.now());
        MotechEvent importEvent = new MotechEvent(Constants.RCH_MOTHER_IMPORT_SUBJECT, params);

        // Call the method
        rchWsImportService.importRchMothersData(importEvent);
        verifyImportProcessInteractions();

        Map<String, Object> param2 = new HashMap<>();
        param2.put("state", stateId.toString());
        param2.put("tempFilePath", "dummyPath/tempFile.json");
        param2.put("stateName", "TestState");
        param2.put("stateCode", stateId.toString());
        MotechEvent thirdEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "mother", param2);
        String thirdApiResponse = "{\"status\":\"success\"}";
        when(rchWebServiceFacade.callThirdApi(anyString())).thenReturn(thirdApiResponse);

        File tempFile = File.createTempFile("tempFile", ".json");
        FileWriter writer = new FileWriter(tempFile);
        writer.write("{\"sampleKey\":\"sampleValue\"}");
        writer.close();

        when(rchWebServiceFacade.generateJsonResponseFile(anyString(), eq(RchUserType.MOTHER), eq(stateId)))
                .thenReturn(tempFile);

        when(rchWebServiceFacade.retryScpAndAudit(
                eq(tempFile.getName()),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(stateId),
                any(RchUserType.class),
                anyInt()
        )).thenReturn(true);


        rchWsImportService.handleThirdApiEvent(thirdEvent);

        assertTrue("The file should contain data", tempFile.length() > 0);
        tempFile.deleteOnExit();
    }

    private void setupMocksAndInitialData() throws Exception {
        State mockState = new State();
        mockState.setCode(stateId);
        mockState.setName("TestState");
        when(stateDataService.findByCode(stateId)).thenReturn(mockState);

        when(settingsFacade.getProperty(Constants.RCH_MOTHER_USER)).thenReturn("dummyUser");
        when(rchWebServiceFacade.callEncryptApi(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(apiResponse);

        String token = "dummyToken";
        when(rchWebServiceFacade.generateAuthToken()).thenReturn(token);
        when(rchWebServiceFacade.callKeelDeelApi(anyString(), anyString(), anyString()))
                .thenReturn("{\"status\":\"success\"}");
        when(rchWebServiceFacade.saveToFile(anyString(), anyString(), anyString()))
                .thenReturn("dummyPath/tempFile.json");
    }

    private void verifyImportProcessInteractions() throws Exception {
        verify(rchWebServiceFacade).callEncryptApi(eq(stateId.toString()), anyString(), any(LocalDate.class), any(LocalDate.class));

        verify(rchWebServiceFacade).generateAuthToken();
        verify(rchWebServiceFacade).callKeelDeelApi(eq("dummyToken"), eq("dummyKeel"), eq("dummyDeel"));

        verify(rchWebServiceFacade).saveToFile(eq("{\"status\":\"success\"}"), eq("mother"), eq(stateId.toString()));
    }
}

