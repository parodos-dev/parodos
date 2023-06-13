package com.redhat.parodos.examples.vmmigration.checker;

import java.util.List;

import com.redhat.parodos.examples.vmmigration.constants.Constants;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1Migration;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1MigrationStatus;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1MigrationStatusConditions;
import com.redhat.parodos.examples.vmmigration.utils.Kubernetes;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MigrationStatusWorkFlowChecker extends BaseWorkFlowCheckerTask {

	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start MigrationStatusWorkFlowChecker...");
		try {
			String migrationName = getRequiredParameterValue(Constants.MIGRATION_NAME_PARAMETER_NAME);

			// Create ApiClient
			String apiURL = getRequiredParameterValue(Constants.KUBERNETES_API_SERVER_URL);
			String token = getRequiredParameterValue(Constants.KUBERNETES_TOKEN);
			String caCert = getOptionalParameterValue(Constants.KUBERNETES_CA_CERT, "");
			ApiClient client = Kubernetes.buildApiClient(apiURL, token, caCert);

			// Retrieve Migration object
			CustomObjectsApi coapi = new CustomObjectsApi(client);
			Object resp = coapi.getNamespacedCustomObject("forklift.konveyor.io", "v1beta1", "demo24", "migrations",
					migrationName);
			JSON json = client.getJSON();
			V1beta1Migration migration = json.deserialize(json.serialize(resp), V1beta1Migration.class);

			V1beta1MigrationStatus status = migration.getStatus();
			if (status == null) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Exception("no status found"));
			}
			List<V1beta1MigrationStatusConditions> conditions = status.getConditions();
			if (conditions == null) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Exception("no conditions found"));
			}
			for (V1beta1MigrationStatusConditions condition : conditions) {
				if (condition.getType().equals("Succeeded") && condition.getStatus().equals("True")) {
					return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
				}
			}
		}
		catch (ApiException e) {
			log.error(e.getResponseBody());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	public List<WorkParameter> getWorkFlowTaskParameters() {
		WorkParameter vmName = WorkParameter.builder().key(Constants.VM_NAME_PARAMETER_NAME)
				.description("Enter the name of the Vmware VM to determine if it can be migrated to OCP Virtualization")
				.optional(false).type(WorkParameterType.TEXT).selectOptions(List.of("mtv-rhel8-sanity")).build();
		WorkParameter apiUrl = WorkParameter.builder().key(Constants.KUBERNETES_API_SERVER_URL)
				.description("Enter the URL of the OpenShift API server where the VM will be migrated to")
				.optional(false).type(WorkParameterType.TEXT).build();

		WorkParameter token = WorkParameter.builder().key(Constants.KUBERNETES_TOKEN)
				.description("Enter the authentication token to the Openshift API server").type(WorkParameterType.TEXT)
				.optional(false).build();

		WorkParameter caCert = WorkParameter.builder().key(Constants.KUBERNETES_CA_CERT).description(
				"Enter the CA certificate to the API server to verify the SSL certificate with the API server. If not available, the connection will not validate the certificate from the server")
				.optional(true).build();

		return List.of(vmName, apiUrl, token, caCert);
	}

}
