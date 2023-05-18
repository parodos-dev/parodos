package com.redhat.parodos.common.exceptions;

public class IllegalWorkFlowStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalWorkFlowStateException(String msg) {
		super(msg);
	}

	public IllegalWorkFlowStateException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
