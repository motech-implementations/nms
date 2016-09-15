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
    MaBookmark getBookmark(Long callingNumber, String callId);

    /**
     * Get the bookmark for the caller (to be used for Ops only)
     * @param callingNumber phone number of the user
     * @return bookmark of the user if it exists, null otherwise
     */
    MaBookmark getBookmarkOps(Long callingNumber);

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

    /**
     * Get scores for a user
     * @param callingNumber
     * @return
     */
    String getScoresForUser(Long callingNumber);

    /**
     * Updates Calling Number in MTRAINING_MODULE_BOOKMARK, nms_ma_completion_records, MTRAINING_MODULE_ACTIVITYRECORD tables
     * @param oldCallingNumber existing Msisdn of caller
     * @param newCallingNumber new Msisdn of caller
     * @return
     */
    void updateMsisdn(Long oldCallingNumber, Long newCallingNumber);

}