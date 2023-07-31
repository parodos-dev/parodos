package com.redhat.parodos.tasks.vmmigration.task;

import java.util.HashMap;
import java.util.UUID;

import com.redhat.parodos.vmmigration.constants.Constants;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Migration;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan;
import com.redhat.parodos.vmmigration.task.CreateMigrationWorkFlowTask;
import com.redhat.parodos.vmmigration.util.Kubernetes;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class CreateMigrationWorkFlowTaskTest {

	private static WorkContext ctx;

	public OpenShiftServer mockServer;

	private CreateMigrationWorkFlowTask createMigrationWorkFlowTask;

	private static final String[] requiredParamKeys = { Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME,
			Constants.KUBERNETES_TOKEN_PARAMETER_NAME, Constants.STORAGE_NAME_PARAMETER_NAME,
			Constants.NETWORK_NAME_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
			Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME, Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME,
			Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, Constants.VM_NAME_PARAMETER_NAME };

	@BeforeEach
	public void setUp() {
		this.createMigrationWorkFlowTask = spy((CreateMigrationWorkFlowTask) new CreateMigrationWorkFlowTask());

		createMigrationWorkFlowTask.setBeanName("createMigrationFlow");
		ctx = new WorkContext();
		HashMap<String, String> map = new HashMap<>();
		for (String paramKey : requiredParamKeys) {
			map.put(paramKey, paramKey);
			WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
					createMigrationWorkFlowTask.getName(), WorkContextDelegate.Resource.ARGUMENTS, map);
		}
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		mockServer = new OpenShiftServer(false, true);
		mockServer.before();
		doReturn(mockServer.getKubernetesClient()).when(this.createMigrationWorkFlowTask).getKubernetesClient(
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
		Plan expectedPlan = Kubernetes.createPlan(Constants.VM_NAME_PARAMETER_NAME,
				Constants.STORAGE_NAME_PARAMETER_NAME, Constants.NETWORK_NAME_PARAMETER_NAME,
				Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME,
				Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME);
		// when
		createMigrationWorkFlowTask.preExecute(ctx);
		WorkReport workReport = this.createMigrationWorkFlowTask.execute(ctx);
		// then
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());

		OpenShiftClient client = mockServer.getOpenShiftMockServer().createOpenShiftClient();
		// validate plans
		MixedOperation<Plan, KubernetesResourceList<Plan>, Resource<Plan>> planClient = client.resources(Plan.class);
		KubernetesResourceList<Plan> plans = planClient.inNamespace(Constants.NAMESPACE_NAME_PARAMETER_NAME).list();
		assertEquals(plans.getItems().size(), 1);
		Plan actualPlan = plans.getItems().get(0);

		then(actualPlan.getSpec()).usingRecursiveComparison().isEqualTo(expectedPlan.getSpec());
		assertTrue(actualPlan.getMetadata().getName().startsWith(Constants.VM_NAME_PARAMETER_NAME));

		// validate migration
		MixedOperation<Migration, KubernetesResourceList<Migration>, Resource<Migration>> migrationClient = client
				.resources(Migration.class);
		KubernetesResourceList<Migration> migrations = migrationClient
				.inNamespace(Constants.NAMESPACE_NAME_PARAMETER_NAME).list();
		assertEquals(migrations.getItems().size(), 1);
		Migration actualMigration = migrations.getItems().get(0);
		Migration expectedMigration = Kubernetes.createMigration(actualPlan.getMetadata().getName(),
				Constants.NAMESPACE_NAME_PARAMETER_NAME);

		assertThat(actualMigration.getSpec()).usingRecursiveComparison().isEqualTo(expectedMigration.getSpec());
		assertEquals(expectedMigration.getMetadata().getName(), actualMigration.getMetadata().getName());

	}

}
