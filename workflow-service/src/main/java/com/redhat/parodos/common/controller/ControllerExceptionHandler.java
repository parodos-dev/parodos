package com.redhat.parodos.common.controller;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.redhat.parodos.common.exceptions.ResourceNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ControllerExceptionHandler {

	@ExceptionHandler(value = { ResourceNotFoundException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public ErrorMessageDTO resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.NOT_FOUND.value(), new Date(), ex.getMessage(), "Resource not found");
	}

	@ExceptionHandler(value = { ConstraintViolationException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO constraintViolationException(ConstraintViolationException ex, WebRequest request) {
		Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
		String errorMessage = "Incorrect request parameters.";
		if (!violations.isEmpty()) {
			errorMessage = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
		}

		ErrorMessageDTO message = new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), new Date(), errorMessage,
				"Incorrect request parameters");
		return message;
	}

	record ErrorMessageDTO(int status, Date date, String message, String description) {
	}

}
