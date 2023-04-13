package com.redhat.parodos.workflow.execution.continuation;

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
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkFlowContinuationServiceImplTest {

	private static final String TEST_WORKFLOW = "testWorkFlow";

	private static final String TEST_WORKFLOW_TASK = "testWorkFlowTask";

	private List<WorkFlowStatus> workFlowStatuses = List.of(WorkFlowStatus.IN_PROGRESS, WorkFlowStatus.PENDING);

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowContinuationServiceImpl service;

	private AsyncWorkFlowContinuerImpl asyncWorkFlowContinuer;

	@BeforeEach
	void initEach() {
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowRepository = Mockito.mock(WorkFlowRepository.class);
		this.workFlowTaskRepository = Mockito.mock(WorkFlowTaskRepository.class);
		this.asyncWorkFlowContinuer = Mockito.mock(AsyncWorkFlowContinuerImpl.class);
		this.service = new WorkFlowContinuationServiceImpl(this.workFlowDefinitionRepository, this.workFlowRepository,
				this.asyncWorkFlowContinuer);
	}

	@Test
	void workFlowSkipCompletedJobs() {
		// given
		Mockito.when(this.workFlowRepository.findByStatusInAndIsMaster(workFlowStatuses)).thenReturn(Arrays.asList());

		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findByStatusInAndIsMaster(workFlowStatuses);
		Mockito.verify(this.asyncWorkFlowContinuer, Mockito.times(0)).executeAsync(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
	}

	@Test
	void workFlowCompleteInProgress() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkFlowStatus.IN_PROGRESS);
		Mockito.when(this.workFlowRepository.findByStatusInAndIsMaster(workFlowStatuses))
				.thenReturn(List.of(workFlowExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition()));
		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findByStatusInAndIsMaster(workFlowStatuses);
		Mockito.verify(this.asyncWorkFlowContinuer, Mockito.times(1)).executeAsync(
				Mockito.eq(workFlowExecution.getProjectId().toString()), Mockito.eq(TEST_WORKFLOW), Mockito.any(),
				Mockito.any());
	}

	@Test
	void workFlowCompletePending() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkFlowStatus.PENDING);
		Mockito.when(this.workFlowRepository.findByStatusInAndIsMaster(workFlowStatuses))
				.thenReturn(List.of(workFlowExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition()));
		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findByStatusInAndIsMaster(workFlowStatuses);
		Mockito.verify(this.asyncWorkFlowContinuer, Mockito.times(1)).executeAsync(
				Mockito.eq(workFlowExecution.getProjectId().toString()), Mockito.eq(TEST_WORKFLOW), Mockito.any(),
				Mockito.any());
	}

	@Test
	void workFlowCompleteWithTaskExecutions() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution(WorkFlowStatus.IN_PROGRESS);
		Mockito.when(this.workFlowRepository.findByStatusInAndIsMaster(workFlowStatuses))
				.thenReturn(List.of(workFlowExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition()));
		WorkFlowTaskDefinition workFlowTaskDefinition = sampleWorkFlowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(workFlowTaskDefinition));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("{\"test\": \"test\"}")
				.results("res").status(WorkFlowTaskStatus.COMPLETED)
				.workFlowTaskDefinitionId(workFlowTaskDefinition.getId()).workFlowExecutionId(workFlowExecution.getId())
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionId(workFlowExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findByStatusInAndIsMaster(workFlowStatuses);
		Mockito.verify(this.asyncWorkFlowContinuer, Mockito.times(1)).executeAsync(
				Mockito.eq(workFlowExecution.getProjectId().toString()), Mockito.eq(TEST_WORKFLOW), Mockito.any(),
				Mockito.any());
	}

	@Test
	void workFlowCompleteWithInvalidJson() {
		// given
		WorkFlowExecution wfExecution = this.sampleWorkFlowExecution(WorkFlowStatus.IN_PROGRESS);
		Mockito.when(this.workFlowRepository.findByStatusInAndIsMaster(workFlowStatuses))
				.thenReturn(List.of(wfExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition()));
		WorkFlowTaskDefinition wfTaskDef = sampleWorkFlowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findById(Mockito.any())).thenReturn(Optional.of(wfTaskDef));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("invalid")
				.results("res").status(WorkFlowTaskStatus.FAILED).workFlowTaskDefinitionId(wfTaskDef.getId())
				.workFlowExecutionId(wfExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionId(wfExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));
		Mockito.doThrow(new RuntimeException("JsonParseException")).when(asyncWorkFlowContinuer)
				.executeAsync(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.service.workFlowRunAfterStartup();
		});

		// then
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("JsonParseException"));

		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findByStatusInAndIsMaster(workFlowStatuses);
		Mockito.verify(this.asyncWorkFlowContinuer, Mockito.times(1)).executeAsync(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
	}

	private WorkFlowExecution sampleWorkFlowExecution(WorkFlowStatus workFlowStatus) {
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().projectId(UUID.randomUUID())
				.status(workFlowStatus).build();
		workFlowExecution.setId(UUID.randomUUID());
		workFlowExecution.setArguments("{\"test\": \"test\"}");
		workFlowExecution.setWorkFlowExecutionContext(WorkFlowExecutionContext.builder()
				.masterWorkFlowExecution(workFlowExecution).workContext(new WorkContext()).build());
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
