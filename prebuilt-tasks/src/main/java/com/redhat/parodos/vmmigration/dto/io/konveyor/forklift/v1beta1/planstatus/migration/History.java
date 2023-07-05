package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "conditions", "map", "migration", "plan", "provider" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class History implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * List of conditions.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("conditions")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("List of conditions.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Conditions> conditions;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Conditions> getConditions() {
		return conditions;
	}

	public void setConditions(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Conditions> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Map.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("map")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Map.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Map map;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Map getMap() {
		return map;
	}

	public void setMap(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Map map) {
		this.map = map;
	}

	/**
	 * Migration
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("migration")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Migration")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Migration migration;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Migration getMigration() {
		return migration;
	}

	public void setMigration(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Migration migration) {
		this.migration = migration;
	}

	/**
	 * Plan
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("plan")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Plan")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Plan plan;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Plan getPlan() {
		return plan;
	}

	public void setPlan(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Plan plan) {
		this.plan = plan;
	}

	/**
	 * Provider
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("provider")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Provider")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Provider provider;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Provider getProvider() {
		return provider;
	}

	public void setProvider(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.Provider provider) {
		this.provider = provider;
	}

}
