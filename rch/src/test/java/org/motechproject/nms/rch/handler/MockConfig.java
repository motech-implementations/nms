package org.motechproject.nms.rch.handler;

import org.mockito.Mockito;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.rch.repository.RchImportAuditDataService;
import org.motechproject.nms.rch.repository.RchImportFacilitatorDataService;
import org.motechproject.nms.rch.repository.RchImportFailRecordDataService;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@Configuration
public class MockConfig {

    @Bean
    @Primary
    public RchWebServiceFacade rchWebServiceFacade() {
        return mock(RchWebServiceFacade.class);
    }

    @Bean
    @Primary
    public StateDataService stateDataService() {
        return mock(StateDataService.class);
    }

    @Bean
    @Primary
    public AlertService alertService() {
        return mock(AlertService.class);
    }

    @Bean
    public RchImportAuditDataService rchImportAuditDataService() {
        return Mockito.mock(RchImportAuditDataService.class); // Mock the RchImportAuditDataService
    }
    @Bean
    public RchImportFailRecordDataService rchImportFailRecordDataService() {
        return Mockito.mock(RchImportFailRecordDataService.class); // Mock the RchImportFailRecordDataService
    }

    @Bean
    public RchImportFacilitatorDataService rchImportFacilitatorDataService() {
        return Mockito.mock(RchImportFacilitatorDataService.class); // Mock the RchImportFacilitatorDataService
    }

    @Bean
    public SettingsFacade rchSettings() {
        return Mockito.mock(SettingsFacade.class); // Mocking the SettingsFacade
    }

    @Bean
    public EventRelay eventRelay() {
        return Mockito.mock(EventRelay.class); // Mocking EventRelay for tests
    }

}
