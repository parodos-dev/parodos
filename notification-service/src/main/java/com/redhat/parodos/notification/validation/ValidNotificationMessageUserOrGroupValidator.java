package com.redhat.parodos.notification.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;

import org.springframework.util.CollectionUtils;

public class ValidNotificationMessageUserOrGroupValidator
		implements ConstraintValidator<ValidNotificationMessageUserOrGroup, NotificationMessageCreateRequestDTO> {

	@Override
	public void initialize(ValidNotificationMessageUserOrGroup constraintAnnotation) {
	}

	@Override
	public boolean isValid(NotificationMessageCreateRequestDTO notificationMessage,
			ConstraintValidatorContext context) {
		if (notificationMessage == null) {
			// In case NotificationMessage doesn't exist it is not valid
			// This is an extreme case that shouldn't happen
			return false;
		}

		return !CollectionUtils.isEmpty(notificationMessage.getUsernames())
				|| !CollectionUtils.isEmpty(notificationMessage.getGroupNames());
	}

}
