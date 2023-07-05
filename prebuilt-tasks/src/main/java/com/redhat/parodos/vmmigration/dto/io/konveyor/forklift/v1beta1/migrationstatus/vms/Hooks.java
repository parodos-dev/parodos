package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "hook", "step" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Hooks implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Hook reference.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("hook")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Hook reference.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.hooks.Hook hook;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.hooks.Hook getHook() {
		return hook;
	}

	public void setHook(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.hooks.Hook hook) {
		this.hook = hook;
	}

	/**
	 * Pipeline step.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("step")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Pipeline step.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String step;

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

}
