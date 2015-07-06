package org.motechproject.nms.region.domain.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FullLocationValidator.class })
@Documented
public @interface ValidFullLocation {

    String message() default "Full Location hierarchy is not valid.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
