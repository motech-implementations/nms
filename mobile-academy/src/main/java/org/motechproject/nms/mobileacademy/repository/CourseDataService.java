package org.motechproject.nms.mobileacademy.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.mobileacademy.domain.Course;

/**
 * Data service to set and retrieve course entity objects
 */
public interface CourseDataService extends MotechDataService<Course> {

    @Lookup
    Course findCourseByName(@LookupField(name = "name") String name);
}
