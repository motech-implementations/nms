package org.motechproject.nms.mobileacademy.service;

import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.dto.MaCourse;

/**
 * Mobile academy service interface to perform crud operations on course and bookmarks
 * This also lets you manually (re)trigger notifications for course completion
 */
public interface MobileAcademyService {

    /**
     * Get the MA course structure for the given course name. This defaults to "MobileAcademyCourse" name
     * @return Course data object with the course name
     */
    MaCourse getCourse();

    /**
     * Set the MA course structure. This should only be called by the config handler on json update
     * @param course course to update and save
     */
    void setCourse(MaCourse course);

    /**
     * Gets the course modification date as an epoch representation. This defaults to MobileAcademyCourse name
     * @return int representation (epoch) of modified course date
     */
    long getCourseVersion();

    /**
     * Get the bookmark for a caller
     * @param callingNumber phone number of the caller
     * @param callId unique call tracking id
     * @return bookmark for the user if it exists, null otherwise
     */
    MaBookmark getBookmark(Long callingNumber, Long callId);

    /**
     * Update the bookmark for a caller
     * @param bookmark updated bookmark to be stored
     */
    void setBookmark(MaBookmark bookmark);

    /**
     * Retrigger the sms notification for course completion for user
     * @param callingNumber
     */
    void triggerCompletionNotification(Long callingNumber);

}
