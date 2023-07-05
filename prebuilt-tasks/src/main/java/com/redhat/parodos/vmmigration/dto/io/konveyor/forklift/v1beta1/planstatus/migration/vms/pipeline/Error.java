package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.vms.pipeline;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "phase", "reasons" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Error implements io.fabric8.kubernetes.api.model.KubernetesResource {

	@com.fasterxml.jackson.annotation.JsonProperty("phase")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String phase;

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	@com.fasterxml.jackson.annotation.JsonProperty("reasons")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<String> reasons;

	public java.util.List<String> getReasons() {
		return reasons;
	}

	public void setReasons(java.util.List<String> reasons) {
		this.reasons = reasons;
	}

}
