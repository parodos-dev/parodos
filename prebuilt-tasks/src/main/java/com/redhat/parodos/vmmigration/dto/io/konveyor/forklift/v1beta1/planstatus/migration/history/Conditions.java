package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.migration.history;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "category", "durable", "items", "lastTransitionTime", "message",
		"reason", "status", "type" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Conditions implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * The condition category.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("category")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The condition category.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String category;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * The condition is durable - never un-staged.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("durable")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The condition is durable - never un-staged.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Boolean durable;

	public Boolean getDurable() {
		return durable;
	}

	public void setDurable(Boolean durable) {
		this.durable = durable;
	}

	/**
	 * A list of items referenced in the `Message`.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("items")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("A list of items referenced in the `Message`.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<String> items;

	public java.util.List<String> getItems() {
		return items;
	}

	public void setItems(java.util.List<String> items) {
		this.items = items;
	}

	/**
	 * When the last status transition occurred.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("lastTransitionTime")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("When the last status transition occurred.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String lastTransitionTime;

	public String getLastTransitionTime() {
		return lastTransitionTime;
	}

	public void setLastTransitionTime(String lastTransitionTime) {
		this.lastTransitionTime = lastTransitionTime;
	}

	/**
	 * The human readable description of the condition.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("message")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The human readable description of the condition.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * The reason for the condition or transition.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("reason")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The reason for the condition or transition.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String reason;

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * The condition status [true,false].
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("status")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The condition status [true,false].")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * The condition type.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("type")
	@io.fabric8.generator.annotation.Required()
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("The condition type.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
