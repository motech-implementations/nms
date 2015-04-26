package org.motechproject.nms.mobileacademy.service.impl;

import org.apache.commons.lang.NotImplementedException;
import org.motechproject.nms.mobileacademy.domain.Bookmark;
import org.motechproject.nms.mobileacademy.domain.CallDetail;
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

    @Override
    public Bookmark getBookmark(Long callingNumber, Long callId) {

        return new Bookmark();
    }

    @Override
    public void setBookmark(Bookmark bookmark) {

        // placeholder for void until this gets implemented
        throw new NotImplementedException();
    }

    @Override
    public void saveCallDetails(CallDetail callDetail) {

        // placeholder for void until this gets implemented
        throw new NotImplementedException();
    }

}
