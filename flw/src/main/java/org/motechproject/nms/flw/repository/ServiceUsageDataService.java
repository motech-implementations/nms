package org.motechproject.nms.flw.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;

import java.util.List;

/**
 * Interface for repository that persists simple records and allows CRUD.
 * MotechDataService base class will provide the implementation of this class as well
 * as methods for adding, deleting, saving and finding all instances.  In this class we
 * define and custom lookups we may need.
 */
public interface ServiceUsageDataService extends MotechDataService<ServiceUsage> {
    @Lookup
    List<ServiceUsage> findRecordByContactNumber(@LookupField(name = "service") Service service);
}
