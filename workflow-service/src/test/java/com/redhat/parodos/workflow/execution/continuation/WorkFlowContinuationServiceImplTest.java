package com.redhat.parodos.workflow.execution.continuation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WorkFlowContinuationServiceImplTest {

	private static final String TEST_WORKFLOW = "testWorkFlow";

	private static final String TEST_WORKFLOW_TASK = "testWorkFlowTask";

	private final List<WorkFlowStatus> workFlowStatuses = List.of(WorkFlowStatus.IN_PROGRESS, WorkFlowStatus.PENDING);

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowContinuationServiceImpl service;

	private AsyncWorkFlowContinuerImpl asyncWorkFlowContinuer;

	@BeforeEach
	void initEach() {
		this.workFlowDefinitionRepository = mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowRepository = mock(WorkFlowRepository.class);
		this.workFlowTaskRepository = mock(WorkFlowTaskRepository.class);
		this.asyncWorkFlowContinuer = mock(AsyncWorkFlowContinuerImpl.class);
		this.service = new WorkFlowContinuationServiceImpl(this.workFlowDefinitionRepository, this.workFlowRepository,
				this.asyncWorkFlowContinuer);
	}

	@Test
	void workFlowSkipCompletedJobs() {
		// given
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of());

		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verify(this.asyncWorkFlowContinuer, times(0)).executeAsync(any(), any(), any(), any());
	}

	@Test
	void workFlowCompleteInProgress() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkFlowStatus.IN_PROGRESS);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(workFlowExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));
		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verify(this.asyncWorkFlowContinuer, times(1)).executeAsync(eq(workFlowExecution.getProjectId()),
				eq(TEST_WORKFLOW), any(), any());
	}

	@Test
	void workFlowCompletePending() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkFlowStatus.PENDING);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(workFlowExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));
		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verify(this.asyncWorkFlowContinuer, times(1)).executeAsync(eq(workFlowExecution.getProjectId()),
				eq(TEST_WORKFLOW), any(), any());
	}

	@Test
	void workFlowCompleteWithTaskExecutions() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkFlowStatus.IN_PROGRESS);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(workFlowExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));
		WorkFlowTaskDefinition workFlowTaskDefinition = sampleWorkFlowTaskDefinition();
		when(this.workFlowTaskDefinitionRepository.findById(any())).thenReturn(Optional.of(workFlowTaskDefinition));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("{\"test\": \"test\"}")
				.results("res").status(WorkFlowTaskStatus.COMPLETED)
				.workFlowTaskDefinitionId(workFlowTaskDefinition.getId()).workFlowExecutionId(workFlowExecution.getId())
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.findByWorkFlowExecutionId(workFlowExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verify(this.asyncWorkFlowContinuer, times(1)).executeAsync(eq(workFlowExecution.getProjectId()),
				eq(TEST_WORKFLOW), any(), any());
	}

	@Test
	void workFlowCompleteWithInvalidJson() {
		// given
		WorkFlowExecution wfExecution = this.sampleWorkFlowExecution(WorkFlowStatus.IN_PROGRESS);
		when(this.workFlowRepository.findByStatusInAndIsMain(workFlowStatuses)).thenReturn(List.of(wfExecution));
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.of(sampleWorkFlowDefinition()));
		WorkFlowTaskDefinition wfTaskDef = sampleWorkFlowTaskDefinition();
		when(this.workFlowTaskDefinitionRepository.findById(any())).thenReturn(Optional.of(wfTaskDef));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("invalid")
				.results("res").status(WorkFlowTaskStatus.FAILED).workFlowTaskDefinitionId(wfTaskDef.getId())
				.workFlowExecutionId(wfExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.findByWorkFlowExecutionId(wfExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));
		doThrow(new RuntimeException("JsonParseException")).when(asyncWorkFlowContinuer).executeAsync(any(), any(),
				any(), any());

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> this.service.workFlowRunAfterStartup());

		// then
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("JsonParseException"));

		verify(this.workFlowRepository, times(1)).findByStatusInAndIsMain(workFlowStatuses);
		verify(this.asyncWorkFlowContinuer, times(1)).executeAsync(any(), any(), any(), any());
	}

	private WorkFlowExecution sampleWorkFlowExecution(WorkFlowStatus workFlowStatus) {
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().projectId(UUID.randomUUID())
				.status(workFlowStatus).build();
		workFlowExecution.setId(UUID.randomUUID());
		workFlowExecution.setArguments("{\"test\": \"test\"}");
		workFlowExecution.setWorkFlowExecutionContext(WorkFlowExecutionContext.builder()
				.mainWorkFlowExecution(workFlowExecution).workContext(new WorkContext()).build());
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

}
