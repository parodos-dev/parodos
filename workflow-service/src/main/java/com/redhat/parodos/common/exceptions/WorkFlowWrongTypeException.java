package com.redhat.parodos.common.exceptions;

public class WorkFlowWrongTypeException extends RuntimeException {

	public WorkFlowWrongTypeException(String msg) {
		super(msg);
	}

	public WorkFlowWrongTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
