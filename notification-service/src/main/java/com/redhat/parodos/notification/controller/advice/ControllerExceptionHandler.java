package com.redhat.parodos.notification.controller.advice;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

	record ErrorMessageDTO(Date date, String message, String description) {
	}

}
