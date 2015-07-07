package org.motechproject.nms.flw.domain.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { FrontLineWorkerValidator.class })
@Documented
public @interface ValidFrontLineWorker {

    String message() default "Active FLWs must have State and District set.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
