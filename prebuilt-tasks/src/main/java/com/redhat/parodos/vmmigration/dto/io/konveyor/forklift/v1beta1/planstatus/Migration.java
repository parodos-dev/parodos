package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "completed", "history", "started", "vms" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Migration implements io.fabric8.kubernetes.api.model.KubernetesResource {

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
	 * History
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("history")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("History")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.History> history;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.History> getHistory() {
		return history;
	}

	public void setHistory(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.History> history) {
		this.history = history;
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
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.Vms> vms;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.Vms> getVms() {
		return vms;
	}

	public void setVms(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.Vms> vms) {
		this.vms = vms;
	}

}
