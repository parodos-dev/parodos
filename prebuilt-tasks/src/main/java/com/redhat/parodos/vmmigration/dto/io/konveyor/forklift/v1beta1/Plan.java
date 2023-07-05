package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1beta1", storage = true, served = true)
@io.fabric8.kubernetes.model.annotation.Group("forklift.konveyor.io")
@io.fabric8.kubernetes.model.annotation.Singular("plan")
@io.fabric8.kubernetes.model.annotation.Plural("plans")
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Plan extends
		io.fabric8.kubernetes.client.CustomResource<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.PlanSpec, com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.PlanStatus>
		implements io.fabric8.kubernetes.api.model.Namespaced {

}
