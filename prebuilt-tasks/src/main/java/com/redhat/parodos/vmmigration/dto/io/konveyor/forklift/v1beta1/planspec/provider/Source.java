package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.provider;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "apiVersion", "fieldPath", "kind", "name", "namespace",
		"resourceVersion", "uid" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
		using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Source implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * API version of the referent.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("apiVersion")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("API version of the referent.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String apiVersion;

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	/**
	 * If referring to a piece of an object instead of an entire object, this string
	 * should contain a valid JSON/Go field access statement, such as
	 * desiredState.manifest.containers[2]. For example, if the object reference is to a
	 * container within a pod, this would take on a value like: "spec.containers{name}"
	 * (where "name" refers to the name of the container that triggered the event) or if
	 * no container name is specified "spec.containers[2]" (container with index 2 in this
	 * pod). This syntax is chosen only to have some well-defined way of referencing a
	 * part of an object. TODO: this design is not final and this field is subject to
	 * change in the future.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("fieldPath")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("If referring to a piece of an object instead of an entire object, this string should contain a valid JSON/Go field access statement, such as desiredState.manifest.containers[2]. For example, if the object reference is to a container within a pod, this would take on a value like: \"spec.containers{name}\" (where \"name\" refers to the name of the container that triggered the event) or if no container name is specified \"spec.containers[2]\" (container with index 2 in this pod). This syntax is chosen only to have some well-defined way of referencing a part of an object. TODO: this design is not final and this field is subject to change in the future.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String fieldPath;

	public String getFieldPath() {
		return fieldPath;
	}

	public void setFieldPath(String fieldPath) {
		this.fieldPath = fieldPath;
	}

	/**
	 * Kind of the referent. More info:
	 * https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#types-kinds
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("kind")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Kind of the referent. More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#types-kinds")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String kind;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * Name of the referent. More info:
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/names/#names
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Name of the referent. More info: https://kubernetes.io/docs/concepts/overview/working-with-objects/names/#names")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Namespace of the referent. More info:
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("namespace")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Namespace of the referent. More info: https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Specific resourceVersion to which this reference is made, if any. More info:
	 * https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#concurrency-control-and-consistency
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("resourceVersion")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Specific resourceVersion to which this reference is made, if any. More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#concurrency-control-and-consistency")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String resourceVersion;

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	/**
	 * UID of the referent. More info:
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/names/#uids
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("uid")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("UID of the referent. More info: https://kubernetes.io/docs/concepts/overview/working-with-objects/names/#uids")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String uid;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
