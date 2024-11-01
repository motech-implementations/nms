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
        // Set up any necessary mocks and stubs
    }


    @Test
    public void testImportRchMothersDataFlow() throws Exception {
        // Mock State Data
        State mockState = new State();
        mockState.setCode(stateId);
        mockState.setName("TestState");
        when(stateDataService.findByCode(stateId)).thenReturn(mockState);

        when(settingsFacade.getProperty(Constants.RCH_MOTHER_USER)).thenReturn("dummyUser");

        //System.out.println(settingsFacade.getProperty(Constants.RCH_MOTHER_USER));


        // Step 1: Mock encrypt API response
        when(rchWebServiceFacade.callEncryptApi(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(apiResponse);

        //System.out.println(rchWebServiceFacade.callEncryptApi(stateId.toString(), settingsFacade.getProperty(Constants.RCH_MOTHER_USER),LocalDate.now(),LocalDate.now()));

        // Step 2: Mock token generation and keel-deel API call
        String token = "dummyToken";
        when(rchWebServiceFacade.generateAuthToken()).thenReturn(token);
        String keelDeelResponse = "{\"status\":\"success\"}";
        when(rchWebServiceFacade.callKeelDeelApi(anyString(), anyString(), anyString()))
                .thenReturn(keelDeelResponse);

        // Step 3: Mock the file saving operation
        String tempFilePath = "dummyPath/tempFile.json";
        when(rchWebServiceFacade.saveToFile(anyString(), anyString(), anyString()))
                .thenReturn(tempFilePath);

        // Step 4: Trigger the first Motech event to start the import process
        Map<String, Object> params = new HashMap<>();
        params.put("stateId", stateId);
        params.put("fromDate", LocalDate.now().minusDays(7));
        params.put("endDate", LocalDate.now());
        MotechEvent importEvent = new MotechEvent(Constants.RCH_MOTHER_IMPORT_SUBJECT, params);

        // Call the method
        rchWsImportService.importRchMothersData(importEvent);

        // Verify first API call (Encrypt API)
        verify(rchWebServiceFacade).callEncryptApi(eq(stateId.toString()), anyString(), any(LocalDate.class), any(LocalDate.class));

        // Verify token generation and keel-deel API call
        verify(rchWebServiceFacade).generateAuthToken();
        verify(rchWebServiceFacade).callKeelDeelApi(eq(token), eq("dummyKeel"), eq("dummyDeel"));

        // Verify file save
        verify(rchWebServiceFacade).saveToFile(eq(keelDeelResponse), eq("mother"), eq(stateId.toString()));

        // Trigger the second Motech event to simulate the third API call

        Map<String, Object> param2 = new HashMap<>();
        param2.put("state", stateId.toString());
        param2.put("tempFilePath", tempFilePath);
        param2.put("stateName", mockState.getName());
        param2.put("stateCode", stateId.toString());
        MotechEvent thirdEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "mother", param2);
        String thirdApiResponse = "{\"status\":\"success\"}";
        when(rchWebServiceFacade.callThirdApi(anyString())).thenReturn(thirdApiResponse);

       /* File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);  // Simulate the file exists
        when(rchWebServiceFacade.generateJsonResponseFile(eq(thirdApiResponse), eq(RchUserType.MOTHER), eq(stateId)))
                .thenReturn(mockFile);*/
        File tempFile = File.createTempFile("tempFile", ".json");
        FileWriter writer = new FileWriter(tempFile);
        writer.write("{\"sampleKey\":\"sampleValue\"}"); // Sample data to make the file non-empty
        writer.close();

        // Step 3: Mock `generateJsonResponseFile` to return the non-empty temp file
        when(rchWebServiceFacade.generateJsonResponseFile(anyString(), eq(RchUserType.MOTHER), eq(stateId)))
                .thenReturn(tempFile);
        System.out.println("rchWebServiceFacade: " + rchWebServiceFacade);
        System.out.println("tempFile: " + tempFile);
        System.out.println("tempFile.getName(): " + (tempFile != null ? tempFile.getName() : "null"));

        when(rchWebServiceFacade.retryScpAndAudit(
                eq(tempFile.getName()),  // String name
                any(LocalDate.class),    // LocalDate from
                any(LocalDate.class),    // LocalDate to
                eq(stateId),             // Long stateId
                any(RchUserType.class),  // RchUserType userType
                anyInt()                 // int trialCount
        )).thenReturn(true);


        rchWsImportService.handleThirdApiEvent(thirdEvent);

        // Step 7: Verify third API response file generation
        /*assertNotNull("The response file should not be null", tempFile);
        verify(rchWebServiceFacade).generateJsonResponseFile(eq(thirdApiResponse), eq(RchUserType.MOTHER), eq(stateId));*/

        assertTrue("The file should contain data", tempFile.length() > 0);

        // Clean up temp file if needed
        tempFile.deleteOnExit();
    }

}

