package com.redhat.parodos.vmmigration.util;

import java.util.List;

import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Migration;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.MigrationSpec;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.PlanSpec;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Map;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Provider;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.Vms;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Network;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.map.Storage;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.provider.Destination;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planspec.provider.Source;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;
import org.apache.commons.lang3.RandomStringUtils;

public class Kubernetes {

	private static int length = 5;

	public static ApiClient buildApiClient(String apiUrl, String token, String caCert) {

		// build apiClient
		ClientBuilder builder = new ClientBuilder();
		Boolean verifySsl = false;
		if (caCert != null && caCert.length() > 0 && !caCert.equals("null")) {
			verifySsl = true;
			builder.setCertificateAuthority(caCert.getBytes());
		}
		return builder.setAuthentication(new AccessTokenAuthentication(token)).setVerifyingSsl(verifySsl)
				.setBasePath(apiUrl).build();
	}

	public static Config buildKubernetesClient(String apiUrl, String token, String caCert) {
		ConfigBuilder builder = new ConfigBuilder().withMasterUrl(apiUrl).withOauthToken(token);
		if (caCert != null && caCert.length() > 0 && !caCert.equals("null")) {
			builder.withCaCertData(caCert);
		}
		return builder.build();
	}

	public static Migration createMigration(String planName, String namespace) {
		com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Plan plan = new com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationspec.Plan();
		plan.setName(planName);
		plan.setNamespace(namespace);
		ObjectMeta metadata = new ObjectMeta(null, null, null, null, null, null, null, null, null, planName, namespace,
				null, null, null, null);

		MigrationSpec spec = new MigrationSpec();
		spec.setPlan(plan);
		Migration migration = new Migration();
		migration.setMetadata(metadata);
		migration.setSpec(spec);
		return migration;
	}

	public static Plan createPlan(String vmName, String storageName, String networkName, String namespaceName,
			String destinationType, String sourceType) {

		Storage storage = new Storage();
		storage.setName(storageName);
		storage.setNamespace(namespaceName);

		Network network = new Network();
		network.setName(networkName);
		network.setNamespace(namespaceName);

		Map map = new Map();
		map.setNetwork(network);
		map.setStorage(storage);

		Destination destination = new Destination();
		destination.setName(destinationType);
		destination.setNamespace(namespaceName);

		Source source = new Source();
		source.setName(sourceType);
		source.setNamespace(namespaceName);

		Provider provider = new Provider();
		provider.setDestination(destination);
		provider.setSource(source);

		Vms vm = new Vms();
		vm.setName(vmName);

		PlanSpec spec = new PlanSpec();

		spec.setArchived(false);
		spec.setDescription("");
		spec.setWarm(false);
		spec.setTargetNamespace(namespaceName);
		spec.setVms(List.of(vm));
		spec.setProvider(provider);
		spec.setMap(map);

		boolean useLetters = true;
		boolean useNumbers = false;
		String generatedString = RandomStringUtils.random(length, useLetters, useNumbers).toLowerCase();

		ObjectMeta metadata = new ObjectMeta(null, null, null, null, null, null, null, null, null,
				"%s-%s".formatted(vmName, generatedString), namespaceName, null, null, null, null);
		com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan plan = new com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan();
		plan.setMetadata(metadata);
		plan.setSpec(spec);
		return plan;
	}

}
