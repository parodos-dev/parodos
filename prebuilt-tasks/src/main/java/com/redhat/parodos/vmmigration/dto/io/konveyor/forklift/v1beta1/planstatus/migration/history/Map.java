package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "network", "storage" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Map implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Snapshot object reference.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("network")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Snapshot object reference.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.map.Network network;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.map.Network getNetwork() {
		return network;
	}

	public void setNetwork(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.map.Network network) {
		this.network = network;
	}

	/**
	 * Snapshot object reference.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("storage")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Snapshot object reference.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.map.Storage storage;

	public com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.map.Storage getStorage() {
		return storage;
	}

	public void setStorage(
			com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history.map.Storage storage) {
		this.storage = storage;
	}

}
