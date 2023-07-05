package com.redhat.parodos.vmmigration.checker;

import java.util.List;

import com.redhat.parodos.vmmigration.constants.Constants;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Migration;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.MigrationStatus;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Conditions;
import com.redhat.parodos.vmmigration.util.Kubernetes;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MigrationStatusWorkFlowChecker extends BaseWorkFlowCheckerTask {

	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start MigrationStatusWorkFlowChecker...");
		try {
			String migrationName = getRequiredParameterValue(Constants.MIGRATION_NAME_PARAMETER_NAME);
			String namespaceName = getRequiredParameterValue(Constants.NAMESPACE_NAME_PARAMETER_NAME);
			String apiUrl = getRequiredParameterValue(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME);
			String token = getRequiredParameterValue(Constants.KUBERNETES_TOKEN_PARAMETER_NAME);
			String caCert = getOptionalParameterValue(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, "");

			try (KubernetesClient client = getKubernetesClient(apiUrl, token, caCert)) {

				MixedOperation<Migration, KubernetesResourceList<Migration>, Resource<Migration>> migrationClient = client
						.resources(Migration.class);
				Migration migration = migrationClient.inNamespace(namespaceName).withName(migrationName).get();

				MigrationStatus status = migration.getStatus();

				List<Conditions> conditions = status.getConditions();
				for (Conditions condition : conditions) {
					if (condition.getType().equals("Succeeded") && condition.getStatus().equals("True")) {
						return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
					}
				}
			}
		}
		catch (MissingParameterException e) {
			log.debug("Failed to resolve required parameter: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	public KubernetesClient getKubernetesClient(String apiUrl, String token, String caCert) {
		Config config = Kubernetes.buildKubernetesClient(apiUrl, token, caCert);
		return new KubernetesClientBuilder().withConfig(config).build();
	}

}
