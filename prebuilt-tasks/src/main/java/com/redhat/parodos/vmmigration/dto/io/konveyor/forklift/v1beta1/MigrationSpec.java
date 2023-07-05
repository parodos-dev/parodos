package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "cancel", "cutover", "plan" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class MigrationSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * List of VMs which will have their imports canceled.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("cancel")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("List of VMs which will have their imports canceled.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Cancel> cancel;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Cancel> getCancel() {
		return cancel;
	}

	public void setCancel(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Cancel> cancel) {
		this.cancel = cancel;
	}

	/**
	 * Date and time to finalize a warm migration. If present, this will override the
	 * value set on the Plan.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("cutover")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Date and time to finalize a warm migration. If present, this will override the value set on the Plan.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String cutover;

	public String getCutover() {
		return cutover;
	}

	public void setCutover(String cutover) {
		this.cutover = cutover;
	}

	/**
	 * Reference to the associated Plan.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("plan")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Reference to the associated Plan.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Plan plan;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Plan getPlan() {
		return plan;
	}

	public void setPlan(com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Plan plan) {
		this.plan = plan;
	}

}
