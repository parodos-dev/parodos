package com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1beta1", storage = true, served = true)
@io.fabric8.kubernetes.model.annotation.Group("forklift.konveyor.io")
@io.fabric8.kubernetes.model.annotation.Singular("migration")
@io.fabric8.kubernetes.model.annotation.Plural("migrations")
@javax.annotation.processing.Generated("io.fabric8.java.generator.CRGeneratorRunner")
public class Migration extends
		io.fabric8.kubernetes.client.CustomResource<com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.MigrationSpec, com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.MigrationStatus>
		implements io.fabric8.kubernetes.api.model.Namespaced {

}
