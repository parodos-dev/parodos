package com.redhat.parodos.common.exceptions;

public class UnregisteredWorkFlowException extends RuntimeException {

	public UnregisteredWorkFlowException(String msg) {
		super(msg);
	}

	public UnregisteredWorkFlowException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
