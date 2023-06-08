package com.redhat.parodos.common.controller;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.redhat.parodos.common.exceptions.IllegalWorkFlowStateException;
import com.redhat.parodos.common.exceptions.OperationDeniedException;
import com.redhat.parodos.common.exceptions.ResourceAlreadyExistsException;
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
		String errorMessage = "Incorrect request parameters (constraint violation).";
		if (!violations.isEmpty()) {
			errorMessage = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
		}

		ErrorMessageDTO message = new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), new Date(), errorMessage,
				"Incorrect request parameters");
		return message;
	}

	@ExceptionHandler(value = { ResourceAlreadyExistsException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.CONFLICT)
	public ErrorMessageDTO resourceAlreadyExistsException(ResourceAlreadyExistsException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.CONFLICT.value(), new Date(), ex.getMessage(), "Resource already exists");
	}

	@ExceptionHandler(value = { OperationDeniedException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO operationDeniedException(OperationDeniedException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), new Date(), ex.getMessage(), "Operation denied");
	}

	@ExceptionHandler(value = { IllegalWorkFlowStateException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO illegalWorkFlowStateException(IllegalWorkFlowStateException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), new Date(), ex.getMessage(),
				"Illegal workflow state");
	}

	record ErrorMessageDTO(int status, Date date, String message, String description) {
	}

}
