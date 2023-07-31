package com.redhat.parodos.workflow.execution.validation;

import jakarta.validation.ConstraintValidatorContext;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class PubliclyVisibleValidatorTest {

	private PublicVisibilityValidator validator;

	private ConstraintValidatorContext constraintValidatorContext;

	@BeforeEach
	public void setUp() {
		validator = new PublicVisibilityValidator();
		constraintValidatorContext = mock(ConstraintValidatorContext.class);
	}

	@Test
	public void shouldReturnTrueWhenResourceIsPublic() {
		WorkContextDelegate.Resource resource = WorkContextDelegate.Resource.WORKFLOW_OPTIONS;

		assertTrue(validator.isValid(resource, constraintValidatorContext));
	}

	@Test
	public void shouldReturnFalseWhenResourceIsNotPublic() {
		WorkContextDelegate.Resource resource = WorkContextDelegate.Resource.PARENT_WORKFLOW;

		assertFalse(validator.isValid(resource, constraintValidatorContext));
	}

	@Test
	public void shouldReturnFalseWhenResourceIsNull() {
		assertFalse(validator.isValid(null, constraintValidatorContext));
	}

}
