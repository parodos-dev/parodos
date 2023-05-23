package com.redhat.parodos.notification.controller.advice;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.redhat.parodos.notification.exceptions.NotificationRecordNotFoundException;
import com.redhat.parodos.notification.exceptions.UnsupportedStateException;
import com.redhat.parodos.notification.exceptions.UsernameNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ControllerExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO handleConstraintViolationException(ConstraintViolationException ex,
			HttpServletRequest request) {
		Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
		String errorMessage = "Incorrect request parameters (constraint violation).";
		if (!violations.isEmpty()) {
			errorMessage = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
		}

		ErrorMessageDTO message = new ErrorMessageDTO(new Date(), errorMessage, "Incorrect request parameters");
		return message;
	}

	@ExceptionHandler(value = { UsernameNotFoundException.class, NotificationRecordNotFoundException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public ErrorMessageDTO resourceNotFoundException(UsernameNotFoundException ex, WebRequest request) {
		return new ErrorMessageDTO(new Date(), ex.getMessage(), "Resource not found");
	}

	@ExceptionHandler(value = { UnsupportedStateException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO unsupportedStateException(UnsupportedStateException ex, WebRequest request) {
		return new ErrorMessageDTO(new Date(), ex.getMessage(), "Unsupported notification state");
	}

	@ExceptionHandler(value = { UnsupportedOperationException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO unsupportedOperationException(UnsupportedStateException ex, WebRequest request) {
		return new ErrorMessageDTO(new Date(), ex.getMessage(), "Unsupported operation");
	}

	record ErrorMessageDTO(Date date, String message, String description) {
	}

}
