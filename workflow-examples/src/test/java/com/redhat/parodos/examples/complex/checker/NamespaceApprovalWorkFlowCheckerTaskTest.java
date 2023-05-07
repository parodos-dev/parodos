package com.redhat.parodos.examples.complex.checker;

import java.util.List;

import com.redhat.parodos.examples.base.BaseWorkFlowCheckerTaskTest;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Namespace Approval WorkFlow Checker Task execution test
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 * @author Richard Wang(Github: richardw98)
 */
public class NamespaceApprovalWorkFlowCheckerTaskTest extends BaseWorkFlowCheckerTaskTest {

	NamespaceApprovalWorkFlowCheckerTask namespaceApprovalWorkFlowCheckerTask;

	@Before
	public void setUp() {
		namespaceApprovalWorkFlowCheckerTask = spy(
				(NamespaceApprovalWorkFlowCheckerTask) getConcretePersonImplementation());
	}

	@Override
	protected BaseWorkFlowCheckerTask getConcretePersonImplementation() {
		return new NamespaceApprovalWorkFlowCheckerTask();
	}

	@Test
	public void checkWorkFlowStatus() {
		// given
		WorkContext workContext = mock(WorkContext.class);

		// when
		WorkReport workReport = namespaceApprovalWorkFlowCheckerTask.checkWorkFlowStatus(workContext);
		assertNotNull(workReport);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		assertNull(workReport.getError());
	}

	@Test
	public void getWorkFlowTaskParameters() {
		// when
		List<WorkParameter> workParameters = namespaceApprovalWorkFlowCheckerTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(workParameters);
		assertEquals(0, workParameters.size());
	}

	@Test
	public void getWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = namespaceApprovalWorkFlowCheckerTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(0, workFlowTaskOutputs.size());
	}

}