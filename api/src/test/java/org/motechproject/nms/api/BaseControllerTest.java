package org.motechproject.nms.api;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.nms.api.web.KilkariController;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;


import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BaseControllerTest {

    @InjectMocks
    private KilkariController kilkariController = new KilkariController();

    @Mock
    private SubscriberService subscriberService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kilkariController).build();
    }

    @Test
    public void testInternalError() throws Exception {
        String message = "error";
        ObjectMapper objectMapper = new ObjectMapper();
        BadRequest badRequest = new BadRequest(message);
        when(subscriberService.getSubscriber(anyLong())).thenThrow(new NullPointerException(message));

        String url = "/kilkari/inbox?callingNumber=1111111111&callId=1234567891234561234512345";

        mockMvc.perform(
                get(url)
        ).andExpect(status().isInternalServerError())
        .andExpect(content().string(objectMapper.writeValueAsString(badRequest)));
    }
}
