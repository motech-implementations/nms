package org.motechproject.nms.mobileacademy.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.mobileacademy.domain.CourseCompletionRecord;

import java.util.List;

/**
 * data interface to create and update completion record for course
 */
public interface CourseCompletionRecordDataService extends MotechDataService<CourseCompletionRecord> {

    @Lookup
    List<CourseCompletionRecord> findByFlwId(@LookupField(name = "flwId") Long flwId);
}
