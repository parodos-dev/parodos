package com.redhat.parodos.workflow.enums;

import lombok.Getter;

public enum WorkFlowLogLevel {

	INFO("\u001B[32m"), WARNING("\u001B[33m"), ERROR("\u001B[34m");

	// Ansi code
	@Getter
	private final String code;

	WorkFlowLogLevel(String code) {
		this.code = code;
	}

}
