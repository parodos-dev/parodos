package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "completed", "conditions", "error", "hooks", "id", "name",
		"phase", "pipeline", "restorePowerState", "started", "type", "warm" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Vms implements io.fabric8.kubernetes.api.model.KubernetesResource {

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
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Conditions> conditions;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Conditions> getConditions() {
		return conditions;
	}

	public void setConditions(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Conditions> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Errors
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("error")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Errors")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Error error;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Error getError() {
		return error;
	}

	public void setError(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Error error) {
		this.error = error;
	}

	/**
	 * Enable hooks.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("hooks")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Enable hooks.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Hooks> hooks;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Hooks> getHooks() {
		return hooks;
	}

	public void setHooks(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Hooks> hooks) {
		this.hooks = hooks;
	}

	/**
	 * The object ID. vsphere: The managed object ID.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The object ID. vsphere:   The managed object ID.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * An object Name. vsphere: A qualified name.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("An object Name. vsphere:   A qualified name.")
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
	@io.fabric8.generator.annotation.Required()
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
	 * Migration pipeline.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("pipeline")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Migration pipeline.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Pipeline> pipeline;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Pipeline> getPipeline() {
		return pipeline;
	}

	public void setPipeline(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Pipeline> pipeline) {
		this.pipeline = pipeline;
	}

	/**
	 * Source VM power state before migration.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("restorePowerState")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Source VM power state before migration.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String restorePowerState;

	public String getRestorePowerState() {
		return restorePowerState;
	}

	public void setRestorePowerState(String restorePowerState) {
		this.restorePowerState = restorePowerState;
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
	 * Type used to qualify the name.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("type")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Type used to qualify the name.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Warm migration status
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("warm")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Warm migration status")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Warm warm;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Warm getWarm() {
		return warm;
	}

	public void setWarm(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.Warm warm) {
		this.warm = warm;
	}

}
