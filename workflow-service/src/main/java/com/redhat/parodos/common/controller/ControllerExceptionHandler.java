package com.redhat.parodos.common.controller;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import com.redhat.parodos.common.exceptions.IllegalWorkFlowStateException;
import com.redhat.parodos.common.exceptions.OperationDeniedException;
import com.redhat.parodos.common.exceptions.ResourceAlreadyExistsException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.UnregisteredWorkFlowException;
import com.redhat.parodos.common.exceptions.WorkFlowNotFoundException;
import com.redhat.parodos.common.exceptions.WorkFlowWrongTypeException;
import com.redhat.parodos.workflow.exceptions.WorkflowExecutionException;

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

	@ExceptionHandler(value = { WorkflowExecutionException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageDTO workflowExecutionException(WorkflowExecutionException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), ex.getMessage(),
				"Workflow execution exception");
	}

	@ExceptionHandler(value = { WorkFlowWrongTypeException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO workFlowWrongTypeException(WorkFlowWrongTypeException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), new Date(), ex.getMessage(),
				"Workflow wrong type exception");
	}

	@ExceptionHandler(value = { UnregisteredWorkFlowException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ErrorMessageDTO unregisteredWorkFlowException(UnregisteredWorkFlowException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), new Date(), ex.getMessage(),
				"Un-registered Workflow exception");
	}

	@ExceptionHandler(value = { WorkFlowNotFoundException.class })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public ErrorMessageDTO workFlowNotFoundException(WorkFlowNotFoundException ex, WebRequest request) {
		return new ErrorMessageDTO(HttpStatus.NOT_FOUND.value(), new Date(), ex.getMessage(),
				"Workflow not found exception");
	}

	record ErrorMessageDTO(int status, Date date, String message, String description) {
	}

}
