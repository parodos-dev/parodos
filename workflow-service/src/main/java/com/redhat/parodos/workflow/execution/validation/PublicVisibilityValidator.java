package com.redhat.parodos.workflow.execution.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.redhat.parodos.workflow.context.WorkContextDelegate;

public class PublicVisibilityValidator implements ConstraintValidator<PubliclyVisible, WorkContextDelegate.Resource> {

	@Override
	public boolean isValid(WorkContextDelegate.Resource value, ConstraintValidatorContext context) {
		return value != null && value.isPublic();
	}

}
