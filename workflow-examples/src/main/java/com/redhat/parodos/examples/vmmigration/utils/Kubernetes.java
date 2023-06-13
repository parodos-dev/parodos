package com.redhat.parodos.examples.vmmigration.utils;

import java.util.List;

import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1Migration;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1MigrationSpec;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1MigrationSpecPlan;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1Plan;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpec;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpecMap;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpecMapNetwork;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpecMapStorage;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpecProvider;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpecProviderDestination;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpecProviderSource;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanSpecVms;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;
import org.apache.commons.lang3.RandomStringUtils;

public class Kubernetes {

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

	public static V1beta1Migration createMigrationManifest(String planName, String namespace) {
		V1beta1Migration migration = new V1beta1Migration();

		migration.apiVersion("forklift.konveyor.io/v1beta1");
		migration.kind("Migration");
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(planName);
		meta.setNamespace(namespace);
		migration.setMetadata(meta);

		V1beta1MigrationSpecPlan plan = new V1beta1MigrationSpecPlan();
		plan.setName(planName);
		plan.setNamespace(namespace);

		V1beta1MigrationSpec spec = new V1beta1MigrationSpec();
		spec.setPlan(plan);

		migration.setSpec(spec);

		return migration;
	}

	public static V1beta1Plan createPlanForVM(String vmName) {
		V1beta1PlanSpecMapStorage storage = new V1beta1PlanSpecMapStorage();
		storage.setName("vmware-qfs8w");
		storage.namespace("demo24");
		V1beta1PlanSpecMapNetwork network = new V1beta1PlanSpecMapNetwork();
		network.setName("vmware-n9lxw");
		network.setNamespace("demo24");
		V1beta1PlanSpecMap map = new V1beta1PlanSpecMap();
		map.setStorage(storage);
		map.setNetwork(network);

		V1beta1PlanSpecProviderDestination destination = new V1beta1PlanSpecProviderDestination();
		destination.setName("openshift");
		destination.setNamespace("demo24");
		V1beta1PlanSpecProviderSource source = new V1beta1PlanSpecProviderSource();
		source.setName("vmware");
		source.setNamespace("demo24");

		V1beta1PlanSpecProvider provider = new V1beta1PlanSpecProvider();
		provider.setSource(source);
		provider.setDestination(destination);

		V1beta1PlanSpecVms vm = new V1beta1PlanSpecVms();
		vm.setName(vmName);
		V1beta1PlanSpec spec = new V1beta1PlanSpec();
		spec.archived(false);
		spec.description("");
		spec.warm(false);
		spec.setTargetNamespace("demo24");
		spec.setVms(List.of(vm));
		spec.setProvider(provider);
		spec.setMap(map);

		V1beta1Plan plan = new V1beta1Plan();
		plan.apiVersion("forklift.konveyor.io/v1beta1");
		plan.kind("Plan");
		V1ObjectMeta meta = new V1ObjectMeta();
		int length = 5;

		boolean useLetters = true;
		boolean useNumbers = false;
		String generatedString = RandomStringUtils.random(length, useLetters, useNumbers).toLowerCase();

		meta.setName(vmName + "-" + generatedString);
		meta.setNamespace("demo24");
		plan.setMetadata(meta);
		plan.setSpec(spec);
		return plan;
	}

}
