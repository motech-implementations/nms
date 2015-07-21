package org.motechproject.nms.flw.service.impl;

import org.joda.time.DateTime;
import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.flw.domain.CallDetailRecord;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.util.List;


@Service("serviceUsageService")
public class ServiceUsageServiceImpl implements ServiceUsageService {
    private CallDetailRecordDataService callDetailRecordDataService;

    @Autowired
    public ServiceUsageServiceImpl(CallDetailRecordDataService callDetailRecordDataService) {
        this.callDetailRecordDataService = callDetailRecordDataService;
    }

    @Override
    public ServiceUsage getCurrentMonthlyUsageForFLWAndService(final FrontLineWorker frontLineWorker, final org.motechproject.nms.props.domain.Service service) {
        ServiceUsage serviceUsage = new ServiceUsage(frontLineWorker, service, 0, 0, false);

        @SuppressWarnings("unchecked")
        QueryExecution<List<CallDetailRecord>> queryExecution = new QueryExecution<List<CallDetailRecord>>() {
            @Override
            public List<CallDetailRecord> execute(Query query, InstanceSecurityRestriction restriction) {
                DateTime monthStart = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay();

                query.setFilter("frontLineWorker == flw && service == flw_service && callStartTime >= monthStart");
                query.declareParameters("org.motechproject.nms.flw.domain.FrontLineWorker flw, org.joda.time.DateTime monthStart, org.motechproject.nms.props.domain.Service flw_service");

                return (List<CallDetailRecord>) query.execute(frontLineWorker, monthStart, service);
            }
        };

        List<CallDetailRecord> callDetailRecords = callDetailRecordDataService.executeQuery(queryExecution);

        // TODO: I'm not sure I like combining the individual service usage records into an aggregate record and using the same domain object for it.
        for (CallDetailRecord callDetailRecord : callDetailRecords) {
            // Add up pulse usage, endOfUsagePromptCounter and or together welcomePrompt
            serviceUsage.setEndOfUsage(serviceUsage.getEndOfUsage() + callDetailRecord
                    .getEndOfUsagePromptCounter());
            serviceUsage.setUsageInPulses(serviceUsage.getUsageInPulses() + callDetailRecord
                    .getCallDurationInPulses());

            boolean welcomePrompt = callDetailRecord.getWelcomePrompt() != null ? callDetailRecord.getWelcomePrompt() : false;
            serviceUsage.setWelcomePrompt(serviceUsage.getWelcomePrompt() || welcomePrompt);
        }

        return serviceUsage;
    }
}
