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
    private static final String API_RESPONSE_KEEL = "testKeelValue";
    private static final String API_RESPONSE_DEEL = "testDeelValue";
    private static final String API_RESPONSE_JSON = "{\"keel\":\"" + API_RESPONSE_KEEL + "\",\"deel\":\"" + API_RESPONSE_DEEL + "\"}";
    private static final String TEMP_FILE_PREFIX = "tempFile";
    private static final String TEMP_FILE_SUFFIX = ".json";
    private static final String SAMPLE_JSON_CONTENT = "{\"sampleKey\":\"sampleValue\"}";
    private static final String FILE_PATH = "testPath/tempFile.json";
    private static final String AUTH_TOKEN = "testAuthToken";
    private static final String SUCCESS_RESPONSE = "{\"status\":\"success\"}";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        setupMocksAndInitialData();
    }

    @Test
    public void testImportRchMothersDataFlow() throws Exception {
        triggerImportProcess();
        verifyImportProcessInteractions();

        File tempFile = createTempJsonFile();
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

        rchWsImportService.handleThirdApiEvent(createThirdEvent());
        assertFileHasContent(tempFile);
        tempFile.deleteOnExit();
    }

    private void setupMocksAndInitialData() {
        State mockState = createMockState();
        when(stateDataService.findByCode(stateId)).thenReturn(mockState);

        when(settingsFacade.getProperty(Constants.RCH_MOTHER_USER)).thenReturn("testUser");
        when(rchWebServiceFacade.callEncryptApi(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(API_RESPONSE_JSON);
        when(rchWebServiceFacade.generateAuthToken()).thenReturn(AUTH_TOKEN);
        when(rchWebServiceFacade.callKeelDeelApi(anyString(), anyString(), anyString()))
                .thenReturn(SUCCESS_RESPONSE);
        when(rchWebServiceFacade.saveToFile(anyString(), anyString(), anyString()))
                .thenReturn(FILE_PATH);
    }

    private State createMockState() {
        State mockState = new State();
        mockState.setCode(stateId);
        mockState.setName("TestState");
        return mockState;
    }

    private void triggerImportProcess() {
        Map<String, Object> params = createImportEventParams();
        MotechEvent importEvent = new MotechEvent(Constants.RCH_MOTHER_IMPORT_SUBJECT, params);
        rchWsImportService.importRchMothersData(importEvent);
    }

    private Map<String, Object> createImportEventParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("stateId", stateId);
        params.put("fromDate", LocalDate.now().minusDays(7));
        params.put("endDate", LocalDate.now());
        return params;
    }

    private void verifyImportProcessInteractions() throws Exception {
        verify(rchWebServiceFacade).callEncryptApi(eq(stateId.toString()), anyString(), any(LocalDate.class), any(LocalDate.class));
        verify(rchWebServiceFacade).generateAuthToken();
        verify(rchWebServiceFacade).callKeelDeelApi(eq(AUTH_TOKEN), eq(API_RESPONSE_KEEL), eq(API_RESPONSE_DEEL));
        verify(rchWebServiceFacade).saveToFile(eq(SUCCESS_RESPONSE), eq("mother"), eq(stateId.toString()));
    }

    private File createTempJsonFile() throws Exception {
        File tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(SAMPLE_JSON_CONTENT);
        }
        return tempFile;
    }

    private MotechEvent createThirdEvent() {
        Map<String, Object> params = new HashMap<>();
        params.put("state", stateId.toString());
        params.put("tempFilePath", FILE_PATH);
        params.put("stateName", "TestState");
        params.put("stateCode", stateId.toString());
        return new MotechEvent(Constants.SECOND_EVENT_PREFIX + "mother", params);
    }

    private void assertFileHasContent(File file) {
        assertTrue("The file should contain data", file.length() > 0);
    }
}

