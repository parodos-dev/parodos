package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "consecutiveFailures", "failures", "nextPrecopyAt", "precopies",
		"successes" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Warm implements io.fabric8.kubernetes.api.model.KubernetesResource {

	@com.fasterxml.jackson.annotation.JsonProperty("consecutiveFailures")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Long consecutiveFailures;

	public Long getConsecutiveFailures() {
		return consecutiveFailures;
	}

	public void setConsecutiveFailures(Long consecutiveFailures) {
		this.consecutiveFailures = consecutiveFailures;
	}

	@com.fasterxml.jackson.annotation.JsonProperty("failures")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Long failures;

	public Long getFailures() {
		return failures;
	}

	public void setFailures(Long failures) {
		this.failures = failures;
	}

	@com.fasterxml.jackson.annotation.JsonProperty("nextPrecopyAt")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String nextPrecopyAt;

	public String getNextPrecopyAt() {
		return nextPrecopyAt;
	}

	public void setNextPrecopyAt(String nextPrecopyAt) {
		this.nextPrecopyAt = nextPrecopyAt;
	}

	@com.fasterxml.jackson.annotation.JsonProperty("precopies")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.warm.Precopies> precopies;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.warm.Precopies> getPrecopies() {
		return precopies;
	}

	public void setPrecopies(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.vms.warm.Precopies> precopies) {
		this.precopies = precopies;
	}

	@com.fasterxml.jackson.annotation.JsonProperty("successes")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Long successes;

	public Long getSuccesses() {
		return successes;
	}

	public void setSuccesses(Long successes) {
		this.successes = successes;
	}

}
