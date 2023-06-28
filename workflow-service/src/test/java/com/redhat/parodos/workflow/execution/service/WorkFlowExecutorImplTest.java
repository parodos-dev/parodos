package com.redhat.parodos.workflow.execution.service;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowExecutor.ExecutionContext;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WorkFlowExecutorImplTest {

	private static final WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().build();

	private static final String workflowName = "test-workflow";

	private static final String rollbackWorkflowName = "test-rollback-workflow";

	private static final UUID projectId = UUID.randomUUID();

	private static final UUID userId = UUID.randomUUID();

	private static final UUID executionId = UUID.randomUUID();

	private static final WorkContext workContext = new WorkContext();

	@Mock
	private WorkFlowDelegate workFlowDelegate;

	@Mock
	private WorkFlowRepository workFlowRepository;

	@Mock
	private WorkFlow mainWorkFlow;

	@Mock
	private WorkFlow rollbackWorkFlow;

	private WorkFlowExecutorImpl workFlowExecutor;

	@BeforeEach
	public void init() {
		when(workFlowDelegate.getWorkFlowByName(workflowName)).thenReturn(mainWorkFlow);
		when(workFlowDelegate.getWorkFlowByName(rollbackWorkflowName)).thenReturn(rollbackWorkFlow);
		when(workFlowRepository.findById(executionId)).thenReturn(Optional.of(workFlowExecution));
		when(rollbackWorkFlow.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		when(mainWorkFlow.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
		workFlowExecutor = new WorkFlowExecutorImpl(workFlowDelegate, workFlowRepository);
	}

	@Test
	void execute_when_mainWorkflowIsFailed_and_rollbackIsFound_then_executeRollback() {
		workFlowExecution.setStatus(WorkStatus.FAILED);
		ExecutionContext executionContext = ExecutionContext.builder().projectId(projectId).userId(userId)
				.workFlowName(workflowName).workContext(workContext).executionId(executionId)
				.rollbackWorkFlowName(rollbackWorkflowName).build();
		workFlowExecutor.execute(executionContext);

		verify(mainWorkFlow, times(1)).execute(any(WorkContext.class));
		verify(rollbackWorkFlow, times(1)).execute(any(WorkContext.class));

	}

	@Test
	void execute_when_mainWorkflowIsFailed_and_rollbackIsNotFound_then_notExecuteRollback() {
		workFlowExecution.setStatus(WorkStatus.FAILED);
		ExecutionContext executionContext = ExecutionContext.builder().projectId(projectId).userId(userId)
				.workFlowName(workflowName).workContext(workContext).executionId(executionId).build();
		workFlowExecutor.execute(executionContext);

		verify(mainWorkFlow, times(1)).execute(any(WorkContext.class));
		verify(rollbackWorkFlow, never()).execute(any(WorkContext.class));
	}

	@Test
	void execute_when_mainWorkflowIsCompleted_then_notExecuteRollback() {
		workFlowExecution.setStatus(WorkStatus.COMPLETED);
		ExecutionContext executionContext = ExecutionContext.builder().projectId(projectId).userId(userId)
				.workFlowName(workflowName).workContext(workContext).executionId(executionId)
				.rollbackWorkFlowName(rollbackWorkflowName).build();
		workFlowExecutor.execute(executionContext);

		verify(mainWorkFlow, times(1)).execute(any(WorkContext.class));
		verify(rollbackWorkFlow, never()).execute(any(WorkContext.class));
	}

}
