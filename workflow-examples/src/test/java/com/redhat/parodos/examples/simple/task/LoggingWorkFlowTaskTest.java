package com.redhat.parodos.examples.simple.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.simple.task.LoggingWorkFlowTask;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Logging WorkFlow Task execution test
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public class LoggingWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private BaseInfrastructureWorkFlowTask loggingWorkFlowTask;

	@Before
	public void setUp() {
		loggingWorkFlowTask = getConcretePersonImplementation();
	}

	@Test
	public void executeNoChecker() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);

		// when
		WorkReport workReport = loggingWorkFlowTask.execute(workContext);

		// then
		assertNull(loggingWorkFlowTask.getWorkFlowChecker());
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		Mockito.verifyNoInteractions(workContext);
	}

	@Test
	public void executeWithChecker() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		loggingWorkFlowTask.setWorkFlowChecker(getWorkFlowChecker());

		// when
		WorkReport execute = loggingWorkFlowTask.execute(workContext);

		// then
		assertNotNull(loggingWorkFlowTask.getWorkFlowChecker());
		assertEquals(WorkStatus.COMPLETED, execute.getStatus());
		Mockito.verify(workContext, Mockito.times(1)).put(WorkFlowConstants.WORKFLOW_CHECKER_ID, workflowTestName);
		Mockito.verifyNoMoreInteractions(workContext);
	}

	@Test
	public void getWorkFlowTaskParameters() {
		// when
		List<WorkFlowTaskParameter> workFlowTaskParameters = loggingWorkFlowTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(loggingWorkFlowTask.getWorkFlowTaskParameters());
		assertEquals(1, workFlowTaskParameters.size());
		assertEquals(WorkFlowTaskParameterType.URL, workFlowTaskParameters.get(0).getType());
		assertEquals("api-server", workFlowTaskParameters.get(0).getKey());
		assertEquals("The api server", workFlowTaskParameters.get(0).getDescription());
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
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new LoggingWorkFlowTask();
	}

}