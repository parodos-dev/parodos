package com.redhat.parodos.common.exceptions;

public enum ResourceType {

	// @formatter:off
	PROJECT("Project"),
	USER("User"),
	ROLE("Role"),
	WORKFLOW_DEFINITION("Workflow definition"),
	WORKFLOW_EXECUTION("Workflow execution");
	// @formatter:on
	private final String name;

	ResourceType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
