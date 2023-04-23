package com.redhat.parodos.workflow.execution.controller;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PublicVisibleValidator.class)
public @interface PublicVisible {

	String message() default "Resource is not public visible";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
