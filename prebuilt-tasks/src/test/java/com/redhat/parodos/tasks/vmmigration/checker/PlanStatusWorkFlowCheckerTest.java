package com.redhat.parodos.tasks.vmmigration.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.vmmigration.checker.PlanStatusWorkFlowChecker;
import com.redhat.parodos.vmmigration.constants.Constants;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.PlanStatus;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.planstatus.Conditions;
import com.redhat.parodos.vmmigration.util.Kubernetes;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class PlanStatusWorkFlowCheckerTest {

	private String workflowTestName = "workflowName";

	private static WorkContext ctx;

	private OpenShiftServer mockServer;

	private PlanStatusWorkFlowChecker planStatusWorkFlowChecker;

	private static final String[] requiredParamKeys = { Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME,
			Constants.KUBERNETES_TOKEN_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
			Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, Constants.PLAN_NAME_PARAMETER_NAME };

	@BeforeEach
	public void setUp() {
		this.planStatusWorkFlowChecker = spy((PlanStatusWorkFlowChecker) new PlanStatusWorkFlowChecker());

		planStatusWorkFlowChecker.setBeanName("PlanStatusWorkFlowChecker");
		ctx = new WorkContext();

		HashMap<String, String> map = new HashMap<>();
		for (String paramKey : requiredParamKeys) {
			map.put(paramKey, paramKey);
			WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
					planStatusWorkFlowChecker.getName(), WorkContextDelegate.Resource.ARGUMENTS, map);
		}
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		mockServer = new OpenShiftServer(false, true);
		mockServer.before();
		doReturn(mockServer.getKubernetesClient()).when(this.planStatusWorkFlowChecker).getKubernetesClient(
				eq(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME), eq(Constants.KUBERNETES_TOKEN_PARAMETER_NAME),
				eq(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME));

	}

	@AfterEach
	public void tearDown() {
		mockServer.after();
	}

	@Test
	public void executeSuccess() {

		// given
		Plan plan = Kubernetes.createPlan(Constants.VM_NAME_PARAMETER_NAME, Constants.STORAGE_NAME_PARAMETER_NAME,
				Constants.NETWORK_NAME_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
				Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME, Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME);
		plan.getMetadata().setName(workflowTestName);
		PlanStatus status = new PlanStatus();
		Conditions condition = new Conditions();
		condition.setType("Ready");
		condition.setStatus("True");
		status.setConditions(List.of(condition));
		plan.setStatus(status);
		mockServer.expect().get()
				.withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/plans/%s"
						.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.PLAN_NAME_PARAMETER_NAME))
				.andReturn(HTTP_OK, plan).always();
		mockServer.expect().delete()
				.withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/plans/%s"
						.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.PLAN_NAME_PARAMETER_NAME))
				.andReturn(HTTP_OK, new ArrayList<StatusDetails>()).always();
		// when
		planStatusWorkFlowChecker.preExecute(ctx);
		WorkReport workReport = this.planStatusWorkFlowChecker.execute(ctx);

		// then
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	public void executeFail() {

		// given
		Plan plan = Kubernetes.createPlan(Constants.VM_NAME_PARAMETER_NAME, Constants.STORAGE_NAME_PARAMETER_NAME,
				Constants.NETWORK_NAME_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
				Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME, Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME);
		plan.getMetadata().setName(workflowTestName);
		PlanStatus status = new PlanStatus();
		Conditions condition = new Conditions();
		condition.setType("Ready");
		condition.setStatus("False");
		status.setConditions(List.of(condition));
		plan.setStatus(status);
		mockServer.expect().get()
				.withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/plans/%s"
						.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.PLAN_NAME_PARAMETER_NAME))
				.andReturn(HTTP_OK, plan).always();
		// when
		planStatusWorkFlowChecker.preExecute(ctx);
		WorkReport workReport = this.planStatusWorkFlowChecker.execute(ctx);

		// then
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

	@Test
	@Disabled
	// FIXME
	public void executeRejected() {

		// given
		Plan plan = Kubernetes.createPlan(Constants.VM_NAME_PARAMETER_NAME, Constants.STORAGE_NAME_PARAMETER_NAME,
				Constants.NETWORK_NAME_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
				Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME, Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME);
		plan.getMetadata().setName(workflowTestName);
		PlanStatus status = new PlanStatus();
		Conditions condition = new Conditions();
		condition.setType("Critical");
		status.setConditions(List.of(condition));
		plan.setStatus(status);
		mockServer.expect().get()
				.withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/plans/%s"
						.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.PLAN_NAME_PARAMETER_NAME))
				.andReturn(HTTP_OK, plan).always();
		// when
		planStatusWorkFlowChecker.preExecute(ctx);
		WorkReport workReport = this.planStatusWorkFlowChecker.execute(ctx);

		// then
		assertEquals(WorkStatus.REJECTED, workReport.getStatus());
	}

	@Test
	@Disabled
	// FIXME
	public void executeTimeoutRejected() {

		// given
		Plan plan = Kubernetes.createPlan(Constants.VM_NAME_PARAMETER_NAME, Constants.STORAGE_NAME_PARAMETER_NAME,
				Constants.NETWORK_NAME_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
				Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME, Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME);
		plan.getMetadata().setName(Constants.PLAN_NAME_PARAMETER_NAME);
		PlanStatus status = new PlanStatus();
		Conditions condition = new Conditions();
		condition.setType("Ready");
		condition.setStatus("True");
		status.setConditions(List.of(condition));
		plan.setStatus(status);
		mockServer.expect().post().withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/plans/"
				.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME)).andReturn(HTTP_OK, plan).always();

		// when
		planStatusWorkFlowChecker.preExecute(ctx);
		WorkReport workReport = this.planStatusWorkFlowChecker.execute(ctx);

		// then
		assertEquals(WorkStatus.REJECTED, workReport.getStatus());
	}

}
