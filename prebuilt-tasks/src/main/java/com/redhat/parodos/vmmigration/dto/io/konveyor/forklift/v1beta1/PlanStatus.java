package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "conditions", "migration", "observedGeneration" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class PlanStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * List of conditions.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("conditions")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("List of conditions.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Conditions> conditions;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Conditions> getConditions() {
		return conditions;
	}

	public void setConditions(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Conditions> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Migration
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("migration")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Migration")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Migration migration;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Migration getMigration() {
		return migration;
	}

	public void setMigration(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Migration migration) {
		this.migration = migration;
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

}
