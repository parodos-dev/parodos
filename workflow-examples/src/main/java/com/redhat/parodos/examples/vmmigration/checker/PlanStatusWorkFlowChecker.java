package com.redhat.parodos.examples.vmmigration.checker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.redhat.parodos.examples.vmmigration.constants.Constants;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1MigrationStatusConditions;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1Plan;
import com.redhat.parodos.examples.vmmigration.dto.models.V1beta1PlanStatus;
import com.redhat.parodos.examples.vmmigration.utils.Kubernetes;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlanStatusWorkFlowChecker extends BaseWorkFlowCheckerTask {

	private static final int maxElapsedCheckTimeInMinutes = 1;

	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start PlanStatusWorkFlowChecker...");
		try {
			log.info("parameter {} value: {}", Constants.PLAN_NAME_PARAMETER_NAME,
					getRequiredParameterValue(Constants.PLAN_NAME_PARAMETER_NAME));
			String planName = getRequiredParameterValue(Constants.PLAN_NAME_PARAMETER_NAME);

			// Create ApiClient
			String apiURL = getRequiredParameterValue(Constants.KUBERNETES_API_SERVER_URL);
			String token = getRequiredParameterValue(Constants.KUBERNETES_TOKEN);
			String caCert = getOptionalParameterValue(Constants.KUBERNETES_CA_CERT, "");
			ApiClient client = Kubernetes.buildApiClient(apiURL, token, caCert);

			// Retrieve plan object
			CustomObjectsApi coapi = new CustomObjectsApi(client);
			Object resp = coapi.getNamespacedCustomObject("forklift.konveyor.io", "v1beta1", "demo24", "plans",
					planName);
			JSON json = client.getJSON();
			V1beta1Plan plan = json.deserialize(json.serialize(resp), V1beta1Plan.class);
			V1beta1PlanStatus status = plan.getStatus();
			if (status == null) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Exception("no status found"));
			}
			List<V1beta1MigrationStatusConditions> conditions = status.getConditions();
			if (conditions == null) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Exception("no conditions found"));
			}
			WorkStatus wStatus = WorkStatus.FAILED;
			for (V1beta1MigrationStatusConditions condition : conditions) {
				log.info("condition type " + condition.getType() + " status " + condition.getStatus());
				if (condition.getType().equals("Ready") && condition.getStatus().equals("True")) {
					wStatus = WorkStatus.COMPLETED;
				}
				if (condition.getCategory().equals("Critical")) {
					addParameter(Constants.PLAN_CONDITION_MESSAGE_PARAMETER_NAME, condition.getMessage());
					log.info("condition type: {},  message: {}", condition.getType(), condition.getMessage());
					wStatus = WorkStatus.REJECTED;
				}
			}
			if (wStatus != WorkStatus.COMPLETED && plan.getMetadata() != null
					&& plan.getMetadata().getCreationTimestamp() != null
					&& plan.getMetadata().getCreationTimestamp().toInstant()
							.plus(maxElapsedCheckTimeInMinutes, ChronoUnit.MINUTES).isAfter(Instant.now())) {
				wStatus = WorkStatus.REJECTED;
			}
			if (wStatus != WorkStatus.FAILED) {
				Object respDelete = coapi.deleteNamespacedCustomObject("forklift.konveyor.io", "v1beta1", "demo24",
						"plans", planName, null, null, null, null, new V1DeleteOptions());
				V1Status resStatus = json.deserialize(json.serialize(respDelete), V1Status.class);
				log.info("status is {}", resStatus.getStatus());
			}

			return new DefaultWorkReport(wStatus, workContext);
		}
		catch (ApiException e) {
			log.error(e.getResponseBody());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (MissingParameterException e) {
			log.error("{}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

	}

}
