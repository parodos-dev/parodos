package com.redhat.parodos.notification.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidNotificationMessageUserOrGroupValidator.class })
@Documented
public @interface ValidNotificationMessageUserOrGroup {

	String message() default "usernames and groupNames are empty, at list one of them must have valid values";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
