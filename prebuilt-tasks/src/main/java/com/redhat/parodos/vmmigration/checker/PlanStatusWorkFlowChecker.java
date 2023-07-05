package com.redhat.parodos.vmmigration.checker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.redhat.parodos.vmmigration.constants.Constants;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.PlanStatus;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Conditions;
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
public class PlanStatusWorkFlowChecker extends BaseWorkFlowCheckerTask {

	private static final int maxElapsedCheckTimeInMinutes = 1;

	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start PlanStatusWorkFlowChecker...");
		try {
			String planName = getRequiredParameterValue(Constants.PLAN_NAME_PARAMETER_NAME);
			String namespaceName = getRequiredParameterValue(Constants.NAMESPACE_NAME_PARAMETER_NAME);
			String apiUrl = getRequiredParameterValue(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME);
			String token = getRequiredParameterValue(Constants.KUBERNETES_TOKEN_PARAMETER_NAME);
			String caCert = getOptionalParameterValue(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, "");
			// Create ApiClient
			try (KubernetesClient client = getKubernetesClient(apiUrl, token, caCert)) {

				MixedOperation<Plan, KubernetesResourceList<Plan>, Resource<Plan>> planClient = client
						.resources(Plan.class);
				Plan plan = planClient.inNamespace(namespaceName).withName(planName).get();
				if (plan == null) {
					return new DefaultWorkReport(WorkStatus.FAILED, workContext,
							new Exception("plan %s not found".formatted(planName)));
				}
				PlanStatus status = plan.getStatus();
				if (status == null) {
					return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Exception("no status found"));
				}
				List<Conditions> conditions = status.getConditions();
				if (conditions == null) {
					return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Exception("no conditions found"));
				}
				WorkStatus wStatus = WorkStatus.FAILED;
				if (conditions.size() > 0) {
					Conditions condition = conditions.get(conditions.size() - 1);
					log.info("condition type: {}, status {} ", condition.getType(), condition.getStatus());
					if (condition.getType().equals("Ready") && condition.getStatus().equals("True")) {
						wStatus = WorkStatus.COMPLETED;
					}
					else if (condition.getType().equals("Critical")) {
						log.info("condition type: {},  message: {}", condition.getType(), condition.getMessage());
						wStatus = WorkStatus.REJECTED;
					}
				}
				if (wStatus != WorkStatus.COMPLETED && plan.getMetadata().getCreationTimestamp() != null
						&& Instant.parse(plan.getMetadata().getCreationTimestamp())
								.plus(maxElapsedCheckTimeInMinutes, ChronoUnit.MINUTES).isAfter(Instant.now())) {
					wStatus = WorkStatus.REJECTED;
				}
				if (wStatus != WorkStatus.FAILED) {
					planClient.inNamespace(namespaceName).withName(planName).delete();
				}

				return new DefaultWorkReport(wStatus, workContext);
			}
		}
		catch (MissingParameterException e) {
			log.debug("Failed to resolve required parameter: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
	}

	public KubernetesClient getKubernetesClient(String apiUrl, String token, String caCert) {
		Config config = Kubernetes.buildKubernetesClient(apiUrl, token, caCert);
		return new KubernetesClientBuilder().withConfig(config).build();
	}

}
