package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "archived", "description", "map", "provider", "targetNamespace",
		"transferNetwork", "vms", "warm" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class PlanSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Whether this plan should be archived.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("archived")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Whether this plan should be archived.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Boolean archived;

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	/**
	 * Description
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("description")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Description")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Resource mapping.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("map")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Resource mapping.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Map map;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Map getMap() {
		return map;
	}

	public void setMap(com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Map map) {
		this.map = map;
	}

	/**
	 * Providers.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("provider")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Providers.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Provider provider;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Provider getProvider() {
		return provider;
	}

	public void setProvider(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Provider provider) {
		this.provider = provider;
	}

	/**
	 * Target namespace.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("targetNamespace")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Target namespace.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String targetNamespace;

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	/**
	 * The network attachment definition that should be used for disk transfer.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("transferNetwork")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The network attachment definition that should be used for disk transfer.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.TransferNetwork transferNetwork;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.TransferNetwork getTransferNetwork() {
		return transferNetwork;
	}

	public void setTransferNetwork(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.TransferNetwork transferNetwork) {
		this.transferNetwork = transferNetwork;
	}

	/**
	 * List of VMs.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("vms")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("List of VMs.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Vms> vms;

	public java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Vms> getVms() {
		return vms;
	}

	public void setVms(
			java.util.List<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Vms> vms) {
		this.vms = vms;
	}

	/**
	 * Whether this is a warm migration.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("warm")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Whether this is a warm migration.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Boolean warm;

	public Boolean getWarm() {
		return warm;
	}

	public void setWarm(Boolean warm) {
		this.warm = warm;
	}

}
