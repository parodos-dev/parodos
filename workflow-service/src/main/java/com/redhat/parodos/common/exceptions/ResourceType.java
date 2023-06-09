package com.redhat.parodos.common.exceptions;

public enum ResourceType {

	// @formatter:off
	PROJECT("Project"),
	USER("User"),
	ROLE("Role"),
	ACCESS_REQUEST ("Access request"),
	WORKFLOW_DEFINITION("Workflow definition"),
	WORKFLOW_EXECUTION("Workflow execution"),
	WORKFLOW_TASK("Workflow task"),
	WORKFLOW_TASK_EXECUTION("Workflow task execution");
	// @formatter:on
	private final String name;

	ResourceType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
