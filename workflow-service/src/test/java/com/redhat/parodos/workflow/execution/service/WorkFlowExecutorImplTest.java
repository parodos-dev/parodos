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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WorkFlowExecutorImplTest {

	private static final WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().build();

	private static final String workflowName = "test-workflow";

	private static final String fallbackWorkFlowName = "test-fallback-workflow";

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
	private WorkFlow fallbackWorkFlow;

	private WorkFlowExecutorImpl workFlowExecutor;

	private WorkFlowServiceImpl workFlowService;

	@BeforeEach
	public void init() {
		when(workFlowDelegate.getWorkFlowByName(workflowName)).thenReturn(mainWorkFlow);
		when(workFlowDelegate.getWorkFlowByName(fallbackWorkFlowName)).thenReturn(fallbackWorkFlow);
		when(workFlowRepository.findById(executionId)).thenReturn(Optional.of(workFlowExecution));
		when(fallbackWorkFlow.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		when(mainWorkFlow.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
		workFlowExecutor = new WorkFlowExecutorImpl(workFlowDelegate, workFlowRepository);
		this.workFlowService = mock(WorkFlowServiceImpl.class);
	}

	@Test
	void execute_when_mainWorkflowIsFailed_and_fallbackIsFound_then_executeFallback() {
		workFlowExecution.setStatus(WorkStatus.FAILED);
		ExecutionContext executionContext = ExecutionContext.builder().projectId(projectId).userId(userId)
				.workFlowName(workflowName).workContext(workContext).executionId(executionId)
				.fallbackWorkFlowName(fallbackWorkFlowName).build();
		workFlowExecutor.execute(executionContext, this.workFlowService);

		verify(mainWorkFlow, times(1)).execute(any(WorkContext.class));
		verify(workFlowService, times(1)).executeFallbackWorkFlow(eq(fallbackWorkFlowName), any(UUID.class));

	}

	@Test
	void execute_when_mainWorkflowIsFailed_and_fallbackIsNotFound_then_notExecuteFallback() {
		workFlowExecution.setStatus(WorkStatus.FAILED);
		ExecutionContext executionContext = ExecutionContext.builder().projectId(projectId).userId(userId)
				.workFlowName(workflowName).workContext(workContext).executionId(executionId).build();
		workFlowExecutor.execute(executionContext, workFlowService);

		verify(mainWorkFlow, times(1)).execute(any(WorkContext.class));
		verify(fallbackWorkFlow, never()).execute(any(WorkContext.class));
	}

	@Test
	void execute_when_mainWorkflowIsCompleted_then_notExecuteFallback() {
		workFlowExecution.setStatus(WorkStatus.COMPLETED);
		ExecutionContext executionContext = ExecutionContext.builder().projectId(projectId).userId(userId)
				.workFlowName(workflowName).workContext(workContext).executionId(executionId)
				.fallbackWorkFlowName(fallbackWorkFlowName).build();
		workFlowExecutor.execute(executionContext, workFlowService);

		verify(mainWorkFlow, times(1)).execute(any(WorkContext.class));
		verify(fallbackWorkFlow, never()).execute(any(WorkContext.class));
	}

}
