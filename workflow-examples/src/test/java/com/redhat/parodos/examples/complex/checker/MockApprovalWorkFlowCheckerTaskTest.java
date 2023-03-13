package com.redhat.parodos.examples.complex.checker;

import com.redhat.parodos.examples.base.BaseWorkFlowCheckerTaskTest;
import com.redhat.parodos.examples.complex.checker.MockApprovalWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
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
import static org.mockito.Mockito.spy;

/**
 * Mock Approval WorkFlow Checker Task execution test
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public class MockApprovalWorkFlowCheckerTaskTest extends BaseWorkFlowCheckerTaskTest {

	MockApprovalWorkFlowCheckerTask mockApprovalWorkFlowCheckerTask;

	@Before
	public void setUp() {
		mockApprovalWorkFlowCheckerTask = Mockito
				.spy((MockApprovalWorkFlowCheckerTask) getConcretePersonImplementation());
	}

	@Override
	protected BaseWorkFlowCheckerTask getConcretePersonImplementation() {
		return new MockApprovalWorkFlowCheckerTask();
	}

	@Test
	public void checkWorkFlowStatus() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);

		// when
		WorkReport workReport = mockApprovalWorkFlowCheckerTask.checkWorkFlowStatus(workContext);
		assertNotNull(workReport);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		assertNull(workReport.getError());
	}

	@Test
	public void getWorkFlowTaskParameters() {
		// when
		List<WorkFlowTaskParameter> workFlowTaskParameters = mockApprovalWorkFlowCheckerTask
				.getWorkFlowTaskParameters();

		// then
		assertNotNull(workFlowTaskParameters);
		assertEquals(0, workFlowTaskParameters.size());
	}

	@Test
	public void getWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = mockApprovalWorkFlowCheckerTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(0, workFlowTaskOutputs.size());
	}

}