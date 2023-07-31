package com.redhat.parodos.examples.complex.checker;

import java.util.List;

import com.redhat.parodos.examples.base.BaseWorkFlowCheckerTaskTest;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Namespace Approval WorkFlow Checker Task execution test
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 * @author Richard Wang(Github: richardw98)
 */
@ExtendWith(MockitoExtension.class)
public class NamespaceApprovalWorkFlowCheckerTaskTest extends BaseWorkFlowCheckerTaskTest {

	NamespaceApprovalWorkFlowCheckerTask namespaceApprovalWorkFlowCheckerTask;

	@BeforeEach
	public void setUp() {
		openMocks(this);
		namespaceApprovalWorkFlowCheckerTask = spy((NamespaceApprovalWorkFlowCheckerTask) getTaskUnderTest());
	}

	@Override
	protected BaseWorkFlowCheckerTask getTaskUnderTest() {
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
