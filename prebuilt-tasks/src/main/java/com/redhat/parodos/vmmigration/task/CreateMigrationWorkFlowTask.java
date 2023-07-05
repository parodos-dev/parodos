package com.redhat.parodos.vmmigration.task;

import java.util.List;

import com.redhat.parodos.vmmigration.constants.Constants;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Migration;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan;
import com.redhat.parodos.vmmigration.util.Kubernetes;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateMigrationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	public KubernetesClient getKubernetesClient(String apiUrl, String token, String caCert) {
		Config config = Kubernetes.buildKubernetesClient(apiUrl, token, caCert);
		return new KubernetesClientBuilder().withConfig(config).build();
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start CreateMigrationWorkFlow...");

		try {
			// Create ApiClient
			String apiUrl = getRequiredParameterValue(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME);
			String token = getRequiredParameterValue(Constants.KUBERNETES_TOKEN_PARAMETER_NAME);
			String storageName = getRequiredParameterValue(Constants.STORAGE_NAME_PARAMETER_NAME);
			String networkName = getRequiredParameterValue(Constants.NETWORK_NAME_PARAMETER_NAME);
			String namespaceName = getRequiredParameterValue(Constants.NAMESPACE_NAME_PARAMETER_NAME);
			String destinationProviderType = getRequiredParameterValue(
					Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME);
			String sourceProviderType = getRequiredParameterValue(Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME);
			String caCert = getOptionalParameterValue(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, "");
			String vmName = getRequiredParameterValue(Constants.VM_NAME_PARAMETER_NAME);

			// Create ApiClient

			try (KubernetesClient client = getKubernetesClient(apiUrl, token, caCert)) {

				Plan plan = Kubernetes.createPlan(vmName, storageName, networkName, namespaceName,
						destinationProviderType, sourceProviderType);
				// Create ApiClient

				MixedOperation<Plan, KubernetesResourceList<Plan>, Resource<Plan>> planClient = client
						.resources(Plan.class);
				planClient.inNamespace(namespaceName).resource(plan).create();

				MixedOperation<Migration, KubernetesResourceList<Migration>, Resource<Migration>> migrationClient = client
						.resources(Migration.class);
				Migration migration = Kubernetes.createMigration(plan.getMetadata().getName(), namespaceName);
				migrationClient.inNamespace(namespaceName).resource(migration).create();
				addParameter(Constants.MIGRATION_NAME_PARAMETER_NAME, migration.getMetadata().getName());
				addParameter(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME, apiUrl);
				addParameter(Constants.KUBERNETES_TOKEN_PARAMETER_NAME, token);
				if (caCert != null) {
					addParameter(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, caCert);
				}
			}
		}
		catch (MissingParameterException e) {
			log.debug("Failed to resolve required parameter: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(Constants.vmNameParameter, Constants.apiUrlParameter, Constants.tokenParameter,
				Constants.caCertParameter, Constants.namespaceNameParameter, Constants.storageNameParameter,
				Constants.networkNameParameter, Constants.destinationProviderTypeParameter,
				Constants.sourceProviderTypeParameter);
	}

}
