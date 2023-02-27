package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowStatus;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.*;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WorkFlowServiceImplTest {

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowDelegate workFlowDelegate;

	private WorkFlowServiceImpl workFlowService;

	@BeforeEach
	void init_earch() {
		this.workFlowRepository = Mockito.mock(WorkFlowRepository.class);
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskRepository = Mockito.mock(WorkFlowTaskRepository.class);
		this.workFlowDelegate = Mockito.mock(WorkFlowDelegate.class);

		this.workFlowService = new WorkFlowServiceImpl(this.workFlowDelegate, this.workFlowDefinitionRepository,
				this.workFlowRepository, this.workFlowTaskRepository);
	}

	@Test
	void executeTestwithValidData() {
		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();

		Mockito.when(work.execute(Mockito.any()))
				.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
					{
						put("foo", "bar");
					}
				}));
		Mockito.when(this.workFlowDelegate.getWorkFlowExecutionByName("test-workflow")).thenReturn(workFlow);
		Mockito.when(this.workFlowDelegate.getWorkFlowContext(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(new WorkContext());
		Mockito.when(this.workFlowDefinitionRepository.findByName(Mockito.any()))
				.thenReturn(List.of(this.sampleWorkflowDefinition("test")));

		// when
		WorkReport report = this.workFlowService.execute("test-project", "test-workflow", null, Map.of());
		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "COMPLETED");
		assertNull(report.getError());

		assertNotNull(report.getWorkContext());
		assertEquals(report.getWorkContext().get("foo"), "bar");

		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).getWorkFlowExecutionByName(Mockito.any());
		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).getWorkFlowContext(Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findByName(Mockito.any());
		Mockito.verify(work, Mockito.times(1)).execute(Mockito.any());

	}

	@Test
	void executeTestwithNoValidWorkflow() {
		// given
		Mockito.when(this.workFlowDelegate.getWorkFlowExecutionByName(Mockito.any())).thenReturn(null);

		// when
		WorkReport report = this.workFlowService.execute("test-project", "test-workflow", null, Map.of());
		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "FAILED");
		assertNotNull(report.getError());

		assertNotNull(report.getWorkContext());

		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).getWorkFlowExecutionByName(Mockito.any());
		Mockito.verify(this.workFlowDelegate, Mockito.times(0)).getWorkFlowContext(Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(0)).findByName(Mockito.any());
	}

	@Test
	void getWorkFlowByIDTestWithValidData() {
		// given
		UUID id = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkFlowStatus.COMPLETED).build();
		workFlowExecution.setId(id);
		Mockito.when(this.workFlowRepository.findById(id)).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkFlowExecution res = this.workFlowService.getWorkFlowById(id);

		// then
		assertNotNull(res);
		assertEquals(res.getId().toString(), id.toString());
		assertEquals(res.getStatus().toString(), "COMPLETED");
	}

	@Test
	void getWorkFlowByIDTestWithInvalidData() {
		// given
		UUID id = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkFlowStatus.COMPLETED).build();
		workFlowExecution.setId(id);
		Mockito.when(this.workFlowRepository.findById(id)).thenReturn(Optional.empty());

		// when
		WorkFlowExecution res = this.workFlowService.getWorkFlowById(id);
		// then
		assertNull(res);
	}

	@Test
	void testsaveWorkflow() {
		// given
		UUID projectId = UUID.randomUUID();
		UUID workflowDefID = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkFlowStatus.COMPLETED).build();
		workFlowExecution.setId(UUID.randomUUID());
		Mockito.when(this.workFlowRepository.save(Mockito.any())).thenReturn(workFlowExecution);

		// when
		WorkFlowExecution res = this.workFlowService.saveWorkFlow(projectId, workflowDefID, WorkFlowStatus.COMPLETED);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowExecution> argument = ArgumentCaptor.forClass(WorkFlowExecution.class);
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getProjectId().toString(), projectId.toString());
		assertEquals(argument.getValue().getWorkFlowDefinitionId().toString(), workflowDefID.toString());
	}

	@Test
	void testUpdateWorkflowWithValidData() {
		// given
		UUID projectId = UUID.randomUUID();
		UUID workflowDefID = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkFlowStatus.COMPLETED)
				.projectId(projectId).workFlowDefinitionId(workflowDefID).build();
		workFlowExecution.setId(UUID.randomUUID());
		Mockito.when(this.workFlowRepository.save(Mockito.any())).thenReturn(workFlowExecution);

		// when
		WorkFlowExecution res = this.workFlowService.updateWorkFlow(workFlowExecution);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowExecution> argument = ArgumentCaptor.forClass(WorkFlowExecution.class);
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getProjectId().toString(), projectId.toString());
		assertEquals(argument.getValue().getWorkFlowDefinitionId().toString(), workflowDefID.toString());
	}

	@Test
	void getWorkFlowTaskTestWithValidData() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkFlowTaskStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID)
				.workFlowExecutionId(wfExecutionID).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(List.of(workFlowTaskExecution));
		// when

		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNotNull(res);
		assertEquals(res.getStatus().toString(), workFlowTaskExecution.getStatus().toString());
		assertEquals(res.getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(res.getWorkFlowTaskDefinitionId().toString(),
				workFlowTaskExecution.getWorkFlowTaskDefinitionId().toString());
		assertEquals(res.getArguments(), workFlowTaskExecution.getArguments());
		assertEquals(res.getResults(), workFlowTaskExecution.getResults());
	}

	@Test
	void getWorkFlowTaskTestWithInvalidData() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();
		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(null);

		// when
		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNull(res);
		Mockito.verify(this.workFlowTaskRepository, Mockito.times(1))
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID, wfTaskDefID);
	}

	@Test
	void getWorkFlowTaskTestWithEmptyArray() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();
		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(new LinkedList<>());

		// when
		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNull(res);
		Mockito.verify(this.workFlowTaskRepository, Mockito.times(1))
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID, wfTaskDefID);
	}

	@Test
	void testSaveWorkFlowTaskExecution() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkFlowTaskStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID)
				.workFlowExecutionId(wfExecutionID).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.save(Mockito.any())).thenReturn(workFlowTaskExecution);
		// when
		WorkFlowTaskExecution res = this.workFlowService.saveWorkFlowTask("arguments", wfTaskDefID, wfExecutionID,
				WorkFlowTaskStatus.COMPLETED);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		Mockito.verify(this.workFlowTaskRepository, Mockito.times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId().toString(), wfTaskDefID.toString());
		assertEquals(argument.getValue().getArguments(), "arguments");
		assertNull(argument.getValue().getResults());
	}

	@Test
	void testUpdateWorkFlowTask() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkFlowTaskStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID)
				.workFlowExecutionId(wfExecutionID).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.save(Mockito.any())).thenReturn(workFlowTaskExecution);
		// when
		WorkFlowTaskExecution res = this.workFlowService.updateWorkFlowTask(workFlowTaskExecution);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		Mockito.verify(this.workFlowTaskRepository, Mockito.times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId().toString(), wfTaskDefID.toString());
		assertEquals(argument.getValue().getArguments(), "test");
		assertNotNull(argument.getValue().getResults(), "res");
	}

	private WorkFlowDefinition sampleWorkflowDefinition(String name) {
		WorkFlowDefinition wf = WorkFlowDefinition.builder().name(name).build();
		wf.setId(UUID.randomUUID());
		return wf;
	}

}
