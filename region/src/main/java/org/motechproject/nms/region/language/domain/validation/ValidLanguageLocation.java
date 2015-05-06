package org.motechproject.nms.region.language.domain.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { LanguageLocationValidator.class })
@Documented
public @interface ValidLanguageLocation {

    String message() default "At least one of state or district must be set.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}