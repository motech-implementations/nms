package org.motechproject.nms.mobileacademy.exception;

/**
 * Created by kosh on 5/14/15.
 */
public class CourseNotCompletedException extends IllegalStateException {

    public CourseNotCompletedException(String s) {
        super(s);
    }

    public CourseNotCompletedException(String message, Throwable cause) {
        super(message, cause);
    }

}
