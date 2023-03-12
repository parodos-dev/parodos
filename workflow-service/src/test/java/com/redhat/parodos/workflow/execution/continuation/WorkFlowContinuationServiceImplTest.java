package com.redhat.parodos.workflow.execution.continuation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkFlowContinuationServiceImplTest {

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowServiceImpl workFlowService;

	private WorkFlowDelegate workFlowDelegate;

	private WorkFlowContinuationServiceImpl service;

	@BeforeEach
	void initEach() {
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowRepository = Mockito.mock(WorkFlowRepository.class);
		this.workFlowTaskRepository = Mockito.mock(WorkFlowTaskRepository.class);
		this.workFlowService = Mockito.mock(WorkFlowServiceImpl.class);
		this.service = new WorkFlowContinuationServiceImpl(this.workFlowDefinitionRepository,
				this.workFlowTaskDefinitionRepository, this.workFlowRepository, this.workFlowTaskRepository,
				this.workFlowService, this.workFlowDelegate, new ObjectMapper());
	}

	@Test
	void workFlowSkipCompletedJobs() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution();
		workFlowExecution.setStatus(WorkFlowStatus.COMPLETED);

		WorkFlowExecution wfFailed = this.sampleWorkFlowExecution();
		wfFailed.setStatus(WorkFlowStatus.FAILED);

		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(workFlowExecution, wfFailed));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(0)).execute(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	@Test
	void workFlowCompleteInProgress() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution();
		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(workFlowExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition()));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(1)).execute(
				Mockito.eq(workFlowExecution.getProjectId().toString()), Mockito.eq("test"), new WorkContext(),
				UUID.randomUUID());

		// execute(String projectId, String workflowName, WorkContext workContext, UUID
		// executionId)

	}

	@Test
	void workFlowCompleteWithTaskExecutions() {
		// given
		WorkFlowExecution workFlowExecution = this.sampleWorkFlowExecution();
		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(workFlowExecution));
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
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(1)).execute(
				Mockito.eq(workFlowExecution.getProjectId().toString()), Mockito.eq("test"), new WorkContext(),
				UUID.randomUUID());
	}

	@Test
	void workFlowCompleteWithInvalidJson() {
		// given
		WorkFlowExecution wfExecution = this.sampleWorkFlowExecution();
		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(wfExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition()));

		WorkFlowTaskDefinition wfTaskDef = sampleWorkFlowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findById(Mockito.any())).thenReturn(Optional.of(wfTaskDef));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("invalid")
				.results("res").status(WorkFlowTaskStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDef.getId())
				.workFlowExecutionId(wfExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionId(wfExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.service.workFlowRunAfterStartup();
		});

		// then
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("JsonParseException"));

		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(0)).execute(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	private WorkFlowExecution sampleWorkFlowExecution() {
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().projectId(UUID.randomUUID())
				.status(WorkFlowStatus.IN_PROGRESS).build();
		workFlowExecution.setId(UUID.randomUUID());
		workFlowExecution.setArguments("{\"test\": \"test\"}");
		return workFlowExecution;
	}

	private WorkFlowTaskDefinition sampleWorkFlowTaskDefinition() {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name("test").build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowDefinition sampleWorkFlowDefinition() {
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name("test").build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

}
