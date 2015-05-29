package org.motechproject.nms.mobileacademy.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;

/**
 * data service to perform CRUD operations on NmsCourse
 */
public interface NmsCourseDataService extends MotechDataService<NmsCourse> {

    @Lookup
    NmsCourse getCourseByName(@LookupField(name = "name") String name);
}
