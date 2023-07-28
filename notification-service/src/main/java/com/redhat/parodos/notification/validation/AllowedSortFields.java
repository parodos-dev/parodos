package com.redhat.parodos.notification.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = { AllowedSortFieldsValidator.class })
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedSortFields {

	String message() default "Sort field values provided are not within the allowed fields that are sortable.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * Specify an array of fields that are allowed.
	 * @return the allowed sort fields
	 */
	String[] value() default {};

}
