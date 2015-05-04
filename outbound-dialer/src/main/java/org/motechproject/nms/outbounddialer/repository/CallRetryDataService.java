package org.motechproject.nms.outbounddialer.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.outbounddialer.domain.CallRetry;
import org.motechproject.nms.outbounddialer.domain.DayOfTheWeek;

import java.util.List;

public interface CallRetryDataService extends MotechDataService<CallRetry> {
    @Lookup
    List<CallRetry> findByDayOfTheWeek(@LookupField(name = "dayOfTheWeek") DayOfTheWeek dayOfTheWeek,
                                                  QueryParams queryParams);
}
