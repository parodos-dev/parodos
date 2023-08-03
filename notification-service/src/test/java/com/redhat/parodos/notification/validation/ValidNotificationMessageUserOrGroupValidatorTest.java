package com.redhat.parodos.notification.validation;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class ValidNotificationMessageUserOrGroupValidatorTest {

	private ValidNotificationMessageUserOrGroupValidator validator;

	private ConstraintValidatorContext constraintValidatorContext;

	@BeforeEach
	public void setUp() {
		validator = new ValidNotificationMessageUserOrGroupValidator();
		constraintValidatorContext = mock(ConstraintValidatorContext.class);
	}

	@Test
	public void shouldReturnTrueWhenValidUsernames() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();
		notificationMessage.setUsernames(List.of("test"));

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext), is(true));
	}

	@Test
	public void shouldReturnTrueWhenValidGroupNames() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();
		notificationMessage.setGroupNames(List.of("test"));

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext), is(true));
	}

	@Test
	public void shouldReturnTrueWhenBothValid() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();
		notificationMessage.setUsernames(List.of("test"));
		notificationMessage.setGroupNames(List.of("test"));

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext), is(true));
	}

	@Test
	public void shouldReturnFalseWhenBothEmpty() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext), is(false));
	}

	@Test
	public void shouldReturnFalseWhenNotificationMessageEmpty() {
		assertThat(validator.isValid(null, constraintValidatorContext), is(false));
	}

}
