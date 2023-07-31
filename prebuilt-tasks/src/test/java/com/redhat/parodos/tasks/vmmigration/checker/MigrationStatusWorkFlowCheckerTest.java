package com.redhat.parodos.tasks.vmmigration.checker;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.vmmigration.checker.MigrationStatusWorkFlowChecker;
import com.redhat.parodos.vmmigration.constants.Constants;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.Migration;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.MigrationStatus;
import com.redhat.parodos.vmmigration.dto.io.konveyor.forklift.v1beta1.migrationstatus.Conditions;
import com.redhat.parodos.vmmigration.util.Kubernetes;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class MigrationStatusWorkFlowCheckerTest {

	private String workflowTestName = "workflowName";

	private static WorkContext ctx;

	private OpenShiftServer mockServer;

	private MigrationStatusWorkFlowChecker migrationStatusWorkFlowChecker;

	private static final String[] requiredParamKeys = { Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME,
			Constants.KUBERNETES_TOKEN_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
			Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, Constants.MIGRATION_NAME_PARAMETER_NAME };

	@BeforeEach
	public void setUp() {
		this.migrationStatusWorkFlowChecker = spy(
				(MigrationStatusWorkFlowChecker) new MigrationStatusWorkFlowChecker());

		migrationStatusWorkFlowChecker.setBeanName("migrationStatusWorkFlowChecker");
		ctx = new WorkContext();

		HashMap<String, String> map = new HashMap<>();
		for (String paramKey : requiredParamKeys) {
			map.put(paramKey, paramKey);
			WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
					migrationStatusWorkFlowChecker.getName(), WorkContextDelegate.Resource.ARGUMENTS, map);
		}
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		mockServer = new OpenShiftServer(false, true);
		mockServer.before();
		doReturn(mockServer.getKubernetesClient()).when(this.migrationStatusWorkFlowChecker).getKubernetesClient(
				eq(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME), eq(Constants.KUBERNETES_TOKEN_PARAMETER_NAME),
				eq(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME));

	}

	@AfterEach
	public void tearDown() {
		mockServer.after();
	}

	@Test
	public void executeSuccessFirstCondition() {

		// given
		Migration expectedMigration = Kubernetes.createMigration(Constants.MIGRATION_NAME_PARAMETER_NAME,
				Constants.NAMESPACE_NAME_PARAMETER_NAME);

		expectedMigration.getMetadata().setName(workflowTestName);
		MigrationStatus status = new MigrationStatus();
		Conditions condition = new Conditions();
		condition.setType("Succeeded");
		condition.setStatus("True");
		status.setConditions(List.of(condition));
		expectedMigration.setStatus(status);
		mockServer.expect().get()
				.withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/migrations/%s"
						.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.MIGRATION_NAME_PARAMETER_NAME))
				.andReturn(HTTP_OK, expectedMigration).always();
		// when
		migrationStatusWorkFlowChecker.preExecute(ctx);
		WorkReport workReport = this.migrationStatusWorkFlowChecker.execute(ctx);

		// then
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	public void executeSuccessOnSecondCondition() {

		// given
		Migration expectedMigration = Kubernetes.createMigration(Constants.MIGRATION_NAME_PARAMETER_NAME,
				Constants.NAMESPACE_NAME_PARAMETER_NAME);

		expectedMigration.getMetadata().setName(workflowTestName);
		MigrationStatus status = new MigrationStatus();
		Conditions condition1 = new Conditions();
		condition1.setType("Running");
		condition1.setStatus("True");
		Conditions condition2 = new Conditions();
		condition2.setType("Succeeded");
		condition2.setStatus("True");
		status.setConditions(List.of(condition1, condition2));
		expectedMigration.setStatus(status);
		mockServer.expect().get()
				.withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/migrations/%s"
						.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.MIGRATION_NAME_PARAMETER_NAME))
				.andReturn(HTTP_OK, expectedMigration).always();
		// when
		migrationStatusWorkFlowChecker.preExecute(ctx);
		WorkReport workReport = this.migrationStatusWorkFlowChecker.execute(ctx);

		// then
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	public void executeFail() {

		// given
		Migration migration = Kubernetes.createMigration(Constants.MIGRATION_NAME_PARAMETER_NAME,
				Constants.NAMESPACE_NAME_PARAMETER_NAME);
		migration.getMetadata().setName(workflowTestName);
		MigrationStatus status = new MigrationStatus();
		Conditions condition = new Conditions();
		condition.setType("Running");
		condition.setStatus("True");
		status.setConditions(List.of(condition));
		migration.setStatus(status);
		mockServer.expect().get()
				.withPath("/apis/forklift.konveyor.io/v1beta1/namespaces/%s/migrations/%s"
						.formatted(Constants.NAMESPACE_NAME_PARAMETER_NAME, Constants.MIGRATION_NAME_PARAMETER_NAME))
				.andReturn(HTTP_OK, migration).always();
		// when
		migrationStatusWorkFlowChecker.preExecute(ctx);
		WorkReport workReport = this.migrationStatusWorkFlowChecker.execute(ctx);

		// then
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
