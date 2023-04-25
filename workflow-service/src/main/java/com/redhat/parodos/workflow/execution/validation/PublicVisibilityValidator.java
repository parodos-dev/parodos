package com.redhat.parodos.workflow.execution.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.redhat.parodos.workflow.context.WorkContextDelegate;

public class PublicVisibilityValidator implements ConstraintValidator<PubliclyVisible, WorkContextDelegate.Resource> {

	@Override
	public boolean isValid(WorkContextDelegate.Resource value, ConstraintValidatorContext context) {
		return value != null && value.isPublic();
	}

}
