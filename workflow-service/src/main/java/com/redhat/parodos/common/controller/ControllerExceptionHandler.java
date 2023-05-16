package com.redhat.parodos.common.controller;

import java.util.Date;

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
		ErrorMessageDTO message = new ErrorMessageDTO(HttpStatus.NOT_FOUND.value(), new Date(), ex.getMessage(),
				"Resource not found");
		return message;
	}

	record ErrorMessageDTO(int status, Date date, String message, String description) {
	}

}
