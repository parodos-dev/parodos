package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "network", "storage" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Map implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Network.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("network")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Network.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Network network;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Network getNetwork() {
		return network;
	}

	public void setNetwork(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Network network) {
		this.network = network;
	}

	/**
	 * Storage.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("storage")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Storage.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Storage storage;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Storage getStorage() {
		return storage;
	}

	public void setStorage(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Storage storage) {
		this.storage = storage;
	}

}
