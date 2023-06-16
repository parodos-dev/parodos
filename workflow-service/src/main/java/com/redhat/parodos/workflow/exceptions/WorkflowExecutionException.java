package com.redhat.parodos.workflow.exceptions;

public class WorkflowExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WorkflowExecutionException(String message) {
		super(message);
	}

}
