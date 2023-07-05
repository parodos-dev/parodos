package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "completed", "conditions", "observedGeneration", "started",
		"vms" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class MigrationStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

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
	 * List of conditions.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("conditions")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("List of conditions.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Conditions> conditions;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Conditions> getConditions() {
		return conditions;
	}

	public void setConditions(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Conditions> conditions) {
		this.conditions = conditions;
	}

	/**
	 * The most recent generation observed by the controller.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The most recent generation observed by the controller.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Long observedGeneration;

	public Long getObservedGeneration() {
		return observedGeneration;
	}

	public void setObservedGeneration(Long observedGeneration) {
		this.observedGeneration = observedGeneration;
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

	/**
	 * VM status
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("vms")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("VM status")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Vms> vms;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Vms> getVms() {
		return vms;
	}

	public void setVms(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Vms> vms) {
		this.vms = vms;
	}

}
