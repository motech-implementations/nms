package org.motechproject.nms.region.utils;

import javax.validation.ConstraintViolation;
import java.util.Set;

public final class ConstraintViolationUtils {
    private ConstraintViolationUtils() {
    }

    public static String toString(Set<ConstraintViolation<?>> violations) {
        StringBuilder builder = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            builder.append(String.format("{'%s': %s}", violation.getPropertyPath(), violation.getMessage()));
        }
        return builder.toString();
    }
}
