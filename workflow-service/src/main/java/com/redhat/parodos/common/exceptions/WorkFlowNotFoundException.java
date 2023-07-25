package com.redhat.parodos.common.exceptions;

public class WorkFlowNotFoundException extends RuntimeException {

	public WorkFlowNotFoundException(String msg) {
		super(msg);
	}

	public WorkFlowNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
