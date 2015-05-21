package org.motechproject.nms.mobileacademy.exception;

/**
 * This exception wrapper handles negative scenarios for course completion triggers
 */
public class CourseNotCompletedException extends IllegalStateException {

    public CourseNotCompletedException(String s) {
        super(s);
    }

    public CourseNotCompletedException(String message, Throwable cause) {
        super(message, cause);
    }

}
