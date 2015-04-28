package org.motechproject.nms.outbounddialer.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.MotechDataService;

import java.util.List;

public interface BeneficiaryDataService extends MotechDataService<Beneficiary> {
    @Lookup
    List<Beneficiary> findByNextCallDay(
            @LookupField(name = "nextCallDay") DayOfTheWeek day,
            @LookupField(name = "status") BeneficiaryStatus status);

    @Lookup
    List<Beneficiary> findByNextCallDay(
            @LookupField(name = "nextCallDay") DayOfTheWeek day,
            @LookupField(name = "status") BeneficiaryStatus status,
            QueryParams queryParams);

    long countFindByNextCallDay(
            @LookupField(name = "nextCallDay") DayOfTheWeek day,
            @LookupField(name = "status") BeneficiaryStatus status);
}
