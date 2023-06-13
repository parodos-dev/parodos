package com.redhat.parodos.examples.vmmigration.task;

import java.util.List;

import com.redhat.parodos.examples.vmmigration.constants.Constants;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1Migration;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1Plan;
import com.redhat.parodos.examples.vmmigration.utils.Kubernetes;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateMigrationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start CreateMigrationWorkFlow...");

		try {

			// Create ApiClient
			String apiURL = getRequiredParameterValue(Constants.KUBERNETES_API_SERVER_URL);
			String token = getRequiredParameterValue(Constants.KUBERNETES_TOKEN);
			String caCert = getOptionalParameterValue(Constants.KUBERNETES_CA_CERT, "");
			ApiClient client = Kubernetes.buildApiClient(apiURL, token, caCert);

			// Create forklift plan
			String vmName = getRequiredParameterValue(Constants.VM_NAME_PARAMETER_NAME);
			V1beta1Plan plan = Kubernetes.createPlanForVM(vmName); // mtv-rhel8-sanity
			CustomObjectsApi coapi = new CustomObjectsApi(client);

			coapi.createNamespacedCustomObject("forklift.konveyor.io", "v1beta1", "demo24", "plans", plan, null, null,
					null);

			V1beta1Migration migration = Kubernetes.createMigrationManifest(plan.getMetadata().getName(),
					plan.getMetadata().getNamespace());

			coapi.createNamespacedCustomObject("forklift.konveyor.io", "v1beta1", "demo24", "migrations", migration,
					null, null, null);
			addParameter(Constants.MIGRATION_NAME_PARAMETER_NAME, migration.getMetadata().getName());
			addParameter(Constants.KUBERNETES_API_SERVER_URL, apiURL);
			addParameter(Constants.KUBERNETES_TOKEN, token);
			if (caCert != null) {
				addParameter(Constants.KUBERNETES_CA_CERT, caCert);
			}
		}
		catch (ApiException e) {
			log.error("Failed to create plan or migration", e);
			ApiException a = (ApiException) e;
			log.error(a.getResponseBody());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (MissingParameterException e) {
			log.error("Can't get parameter {} value", Constants.VM_NAME_PARAMETER_NAME);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
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
