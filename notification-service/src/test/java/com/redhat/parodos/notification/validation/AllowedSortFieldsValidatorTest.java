package com.redhat.parodos.notification.validation;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AllowedSortFieldsValidatorTest {

	private AllowedSortFieldsValidator validator;

	private ConstraintValidatorContext constraintValidatorContext;

	private AllowedSortFields constraintAnnotation;

	@BeforeEach
	public void setUp() {
		validator = new AllowedSortFieldsValidator();
		constraintValidatorContext = mock(ConstraintValidatorContext.class);
		constraintAnnotation = mock(AllowedSortFields.class);
		when(constraintAnnotation.value()).thenReturn(new String[] { "test1", "test2" });
		validator.initialize(constraintAnnotation);
	}

	@Test
	public void shouldReturnTrueWhenPageableEmpty() {
		// when
		assertThat(validator.isValid(null, constraintValidatorContext), is(true));
	}

	@Test
	public void shouldReturnTrueWhenAllowedListEmpty() {
		// given
		when(constraintAnnotation.value()).thenReturn(new String[] {});
		validator.initialize(constraintAnnotation);
		Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.asc("test1"), Sort.Order.desc("test2")));

		// when
		assertThat(validator.isValid(pageable, constraintValidatorContext), is(true));
	}

	@Test
	public void shouldReturnTrueWhenSortEmpty() {
		// given
		Sort sort = mock(Sort.class);
		Pageable pageable = PageRequest.of(0, 5, sort);
		when(sort.isUnsorted()).thenReturn(true);

		// when
		assertThat(validator.isValid(pageable, constraintValidatorContext), is(true));
	}

	@Test
	public void shouldReturnTrueWhenSortValuesAllowed() {
		// given
		Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.asc("test1"), Sort.Order.desc("test2")));

		// when
		assertThat(validator.isValid(pageable, constraintValidatorContext), is(true));
	}

	@Test
	public void shouldReturnFalseWhenSortValuesNotAllowed() {
		// given
		Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.asc("test1"), Sort.Order.desc("test3")));
		ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder = mock(
				ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(constraintValidatorContext.buildConstraintViolationWithTemplate(any()))
				.thenReturn(constraintViolationBuilder);
		when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);

		// when
		assertThat(validator.isValid(pageable, constraintValidatorContext), is(false));
	}

}
