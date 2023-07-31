package com.redhat.parodos.examples.simple.task;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Logging WorkFlow Task execution test
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 * @author Annel Ketcha (Github: anludke)
 */
public class LoggingWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String WORKFLOW_TASK_NAME = "loggingWorkFlowTask";

	private static final String WORKFLOW_PARAMETER_API_SERVER_KEY = "api-server";

	private static final String WORKFLOW_PARAMETER_API_SERVER_DESCRIPTION = "The api server";

	private static final String WORKFLOW_PARAMETER_API_SERVER_VALUE = "api-server-test";

	private static final String WORKFLOW_PARAMETER_USER_ID_KEY = "user-id";

	private static final String WORKFLOW_PARAMETER_USER_ID_VALUE = "user-id-test";

	private BaseInfrastructureWorkFlowTask loggingWorkFlowTask;

	private WorkContext workContext;

	@SuppressWarnings("serial")
	@BeforeEach
	public void setUp() {
		loggingWorkFlowTask = getTaskUnderTest();
		workContext = new WorkContext();
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		loggingWorkFlowTask.preExecute(workContext);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
				WORKFLOW_TASK_NAME, WorkContextDelegate.Resource.ARGUMENTS, new HashMap<>() {
					{
						put(WORKFLOW_PARAMETER_API_SERVER_KEY, WORKFLOW_PARAMETER_API_SERVER_VALUE);
						put(WORKFLOW_PARAMETER_USER_ID_KEY, WORKFLOW_PARAMETER_USER_ID_VALUE);
					}
				});
	}

	@Test
	public void executeNoChecker() {
		// when
		WorkReport workReport = loggingWorkFlowTask.execute(workContext);

		// then
		assertNull(loggingWorkFlowTask.getWorkFlowCheckers());
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	public void executeWithChecker() {
		// given
		loggingWorkFlowTask.setWorkFlowCheckers(getWorkFlowCheckers());

		// when
		WorkReport execute = loggingWorkFlowTask.execute(workContext);

		// then
		assertNotNull(loggingWorkFlowTask.getWorkFlowCheckers());
		assertEquals(WorkStatus.COMPLETED, execute.getStatus());
	}

	@Test
	public void getWorkFlowTaskParameters() {
		// when
		List<WorkParameter> workParameters = loggingWorkFlowTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(loggingWorkFlowTask.getWorkFlowTaskParameters());
		assertEquals(2, workParameters.size());
		assertEquals(WorkParameterType.URI, workParameters.get(0).getType());
		assertEquals(WORKFLOW_PARAMETER_API_SERVER_KEY, workParameters.get(0).getKey());
		assertEquals(WORKFLOW_PARAMETER_API_SERVER_DESCRIPTION, workParameters.get(0).getDescription());
	}

	@Test
	public void testGetWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = loggingWorkFlowTask.getWorkFlowTaskOutputs();
		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(1, workFlowTaskOutputs.size());
		assertEquals(WorkFlowTaskOutput.OTHER, workFlowTaskOutputs.get(0));
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getTaskUnderTest() {
		LoggingWorkFlowTask loggingWorkFlowTask = new LoggingWorkFlowTask();
		loggingWorkFlowTask.setBeanName(WORKFLOW_TASK_NAME);
		return loggingWorkFlowTask;
	}

}
