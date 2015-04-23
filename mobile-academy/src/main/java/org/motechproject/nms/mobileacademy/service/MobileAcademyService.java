package org.motechproject.nms.mobileacademy.service;

import org.motechproject.nms.mobileacademy.domain.Bookmark;
import org.motechproject.nms.mobileacademy.domain.CallDetail;
import org.motechproject.nms.mobileacademy.domain.Course;

/**
 * Simple example of a service interface.
 */
public interface MobileAcademyService {

    /**
     * Get the MA couse structure
     * @return
     */
    Course getCourse();

    /**
     * Gets the course modification date as an epoch representation
     * @return
     */
    Integer getCourseVersion();

    /**
     * Get the bookmark for a caller
     * @param callingNumber phone number of the caller
     * @param callId unique call tracking id
     * @return bookmark for the user if it exists, null otherwise
     */
    Bookmark getBookmark(Long callingNumber, Long callId);

    /**
     * Update the bookmark for a caller
     * @param bookmark updated bookmark to be stored
     */
    void setBookmark(Bookmark bookmark);

    /**
     * Save the details of a call instance
     * @param callDetail call detail data
     */
    void saveCallDetails(CallDetail callDetail);

}
