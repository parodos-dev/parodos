package com.redhat.parodos.workflow.execution.continuation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowExecutor;
import com.redhat.parodos.workflow.execution.service.WorkFlowExecutor.ExecutionContext;
import com.redhat.parodos.workflow.execution.service.WorkFlowService;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkFlowContinuationServiceImplTest {

	private static final String TEST_WORKFLOW = "testWorkFlow";

	private static final String TEST_WORKFLOW_TASK = "testWorkFlowTask";

	private final List<WorkStatus> workFlowStatuses = List.of(WorkStatus.IN_PROGRESS, WorkStatus.PENDING);

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowContinuationServiceImpl service;

	private WorkFlowExecutor workFlowExecutor;

	private WorkFlowService workFlowService;

	@BeforeEach
	void initEach() {
		this.workFlowDefinitionRepository = mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowRepository = mock(WorkFlowRepository.class);
		this.workFlowTaskRepository = mock(WorkFlowTaskRepository.class);
		this.workFlowExecutor = mock(WorkFlowExecutor.class);
		this.workFlowService = mock(WorkFlowService.class);
		this.service = new WorkFlowContinuationServiceImpl(this.workFlowRepository, this.workFlowExecutor,
				workFlowService);
	}

	@Test
	void workFlowSkipCompletedJobs() {
		// given
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of());

		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verify(this.workFlowExecutor, times(0)).execute(any(ExecutionContext.class), any());
	}

	@Test
	void workFlowCompleteInProgress() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkStatus.IN_PROGRESS);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(workFlowExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));
		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);

		verifyAsyncExecution(workFlowExecution);
	}

	@Test
	void workFlowCompletePending() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkStatus.PENDING);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(workFlowExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);

		verifyAsyncExecution(workFlowExecution);
	}

	@Test
	void workFlowCompleteWithTaskExecutions() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkStatus.IN_PROGRESS);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(workFlowExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));
		WorkFlowTaskDefinition workFlowTaskDefinition = sampleWorkFlowTaskDefinition();
		when(this.workFlowTaskDefinitionRepository.findById(any())).thenReturn(Optional.of(workFlowTaskDefinition));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("{\"test\": \"test\"}")
				.results("res").status(WorkStatus.COMPLETED).workFlowTaskDefinitionId(workFlowTaskDefinition.getId())
				.workFlowExecutionId(workFlowExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.findByWorkFlowExecutionId(workFlowExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verifyAsyncExecution(workFlowExecution);
	}

	@Test
	void workFlowCompleteWithInvalidJson() {
		// given
		WorkFlowExecution wfExecution = this.sampleWorkFlowExecution(WorkStatus.IN_PROGRESS);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(wfExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));
		WorkFlowTaskDefinition wfTaskDef = sampleWorkFlowTaskDefinition();
		when(this.workFlowTaskDefinitionRepository.findById(any())).thenReturn(Optional.of(wfTaskDef));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("invalid")
				.results("res").status(WorkStatus.FAILED).workFlowTaskDefinitionId(wfTaskDef.getId())
				.workFlowExecutionId(wfExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.findByWorkFlowExecutionId(wfExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));
		doThrow(new RuntimeException("JsonParseException")).when(workFlowExecutor).execute(any(ExecutionContext.class),
				any());

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> this.service.workFlowRunAfterStartup());

		// then
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("JsonParseException"));

		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verify(this.workFlowExecutor, times(1)).execute(any(ExecutionContext.class), any());

	}

	private WorkFlowExecution sampleWorkFlowExecution(WorkStatus workStatus) {
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().projectId(UUID.randomUUID()).user(user)
				.status(workStatus).build();
		workFlowExecution.setId(UUID.randomUUID());
		workFlowExecution.setArguments("{\"test\": \"test\"}");
		workFlowExecution.setWorkFlowExecutionContext(WorkFlowExecutionContext.builder()
				.mainWorkFlowExecution(workFlowExecution).workContext(new WorkContext()).build());
		workFlowExecution.setWorkFlowDefinition(sampleWorkFlowDefinition());
		return workFlowExecution;
	}

	private WorkFlowTaskDefinition sampleWorkFlowTaskDefinition() {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name(TEST_WORKFLOW_TASK)
				.build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowDefinition sampleWorkFlowDefinition() {
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(TEST_WORKFLOW).build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

	private void verifyAsyncExecution(WorkFlowExecution workFlowExecution) {
		var argument = ArgumentCaptor.forClass(ExecutionContext.class);
		verify(this.workFlowExecutor, times(1)).execute(argument.capture(), any());
		assertThat(argument.getValue().projectId(), equalTo(workFlowExecution.getProjectId()));
		assertThat(argument.getValue().userId(), equalTo(workFlowExecution.getUser().getId()));
		assertThat(argument.getValue().workFlowName(), equalTo(TEST_WORKFLOW));
	}

}
