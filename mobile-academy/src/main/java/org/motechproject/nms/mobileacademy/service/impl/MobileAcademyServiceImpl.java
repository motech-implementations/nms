package org.motechproject.nms.mobileacademy.service.impl;

import org.motechproject.nms.mobileacademy.domain.Course;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.springframework.stereotype.Service;

/**
 * Simple implementation of the {@link MobileAcademyService} interface.
 */
@Service("mobileAcademyService")
public class MobileAcademyServiceImpl implements MobileAcademyService {

    @Override
    public Course getCourse() {
        return new Course();
    }

    @Override
    public Integer getCourseVersion() {

        return 1;
    }

}
