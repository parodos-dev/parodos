package com.redhat.parodos.notification.validation;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ValidNotificationMessageUserOrGroupValidatorTest {

	private ValidNotificationMessageUserOrGroupValidator validator;

	private ConstraintValidatorContext constraintValidatorContext;

	@Before
	public void setUp() {
		validator = new ValidNotificationMessageUserOrGroupValidator();
		constraintValidatorContext = mock(ConstraintValidatorContext.class);
	}

	@Test
	public void shouldReturnTrueWhenValidUsernames() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();
		notificationMessage.setUsernames(List.of("test"));

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext)).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenValidGroupNames() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();
		notificationMessage.setGroupNames(List.of("test"));

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext)).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenBothValid() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();
		notificationMessage.setUsernames(List.of("test"));
		notificationMessage.setGroupNames(List.of("test"));

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext)).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenBothEmpty() {
		NotificationMessageCreateRequestDTO notificationMessage = new NotificationMessageCreateRequestDTO();

		assertThat(validator.isValid(notificationMessage, constraintValidatorContext)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenNotificationMessageEmpty() {
		assertThat(validator.isValid(null, constraintValidatorContext)).isFalse();
	}

}
