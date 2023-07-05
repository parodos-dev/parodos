package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.pipeline;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "annotations", "completed", "description", "error", "name",
		"phase", "progress", "reason", "started" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Tasks implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Annotations.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("annotations")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Annotations.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.Map<java.lang.String, String> annotations;

	public java.util.Map<java.lang.String, String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(java.util.Map<java.lang.String, String> annotations) {
		this.annotations = annotations;
	}

	/**
	 * Completed timestamp.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("completed")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Completed timestamp.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String completed;

	public String getCompleted() {
		return completed;
	}

	public void setCompleted(String completed) {
		this.completed = completed;
	}

	/**
	 * Name
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("description")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Name")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Error.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("error")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Error.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.pipeline.tasks.Error error;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.pipeline.tasks.Error getError() {
		return error;
	}

	public void setError(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.pipeline.tasks.Error error) {
		this.error = error;
	}

	/**
	 * Name.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Name.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Phase
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("phase")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Phase")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String phase;

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	/**
	 * Progress.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("progress")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Progress.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.pipeline.tasks.Progress progress;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.pipeline.tasks.Progress getProgress() {
		return progress;
	}

	public void setProgress(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.pipeline.tasks.Progress progress) {
		this.progress = progress;
	}

	/**
	 * Reason
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("reason")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Reason")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String reason;

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Started timestamp.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("started")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Started timestamp.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String started;

	public String getStarted() {
		return started;
	}

	public void setStarted(String started) {
		this.started = started;
	}

}
