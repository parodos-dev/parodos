package com.redhat.parodos.workflows.workflow;

import lombok.Builder;

@Builder
public class WorkFlowPropertiesMetadata {

	private String version;

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
