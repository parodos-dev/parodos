package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class WorkFlowServiceImplTest {

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowDelegate workFlowDelegate;

	private WorkFlowServiceImpl workFlowService;

	@BeforeEach
	void initEach() {
		this.workFlowRepository = Mockito.mock(WorkFlowRepository.class);
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowTaskRepository = Mockito.mock(WorkFlowTaskRepository.class);
		this.workFlowDelegate = Mockito.mock(WorkFlowDelegate.class);
		this.workFlowWorkRepository = Mockito.mock(WorkFlowWorkRepository.class);

		this.workFlowService = new WorkFlowServiceImpl(this.workFlowDelegate, this.workFlowDefinitionRepository,
				this.workFlowTaskDefinitionRepository, this.workFlowRepository, this.workFlowTaskRepository,
				workFlowWorkRepository);
	}

	@Test
	void executeTestWithValidData() {
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
		Mockito.when(this.workFlowDelegate.initWorkFlowContext(Mockito.any())).thenReturn(new WorkContext());
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(Mockito.any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));

		// when
		WorkReport report = this.workFlowService.execute("test-project", "test-workflow", new WorkContext(),
				UUID.randomUUID());
		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "COMPLETED");
		assertNull(report.getError());

		assertNotNull(report.getWorkContext());
		assertEquals(report.getWorkContext().get("foo"), "bar");

		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).getWorkFlowExecutionByName(Mockito.any());
		Mockito.verify(work, Mockito.times(1)).execute(Mockito.any());

	}

	@Test
	void executeTestWithNoValidWorkflow() {
		// given
		Mockito.when(this.workFlowDelegate.getWorkFlowExecutionByName(Mockito.any())).thenReturn(null);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId("test-project")
				.works(List.of()).workFlowName("test-workflow").build());
		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "FAILED");
		assertNotNull(report.getError());

		assertNotNull(report.getWorkContext());

		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).getWorkFlowExecutionByName(Mockito.any());
		Mockito.verify(this.workFlowDelegate, Mockito.times(0)).initWorkFlowContext(Mockito.any());
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(0)).findFirstByName(Mockito.any());
	}

	@Test
	void executeWithDTOWithValidData() {
		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		Mockito.when(work.execute(Mockito.any()))
				.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
					{
						put("foo", "bar");
					}
				}));
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(Mockito.any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		Mockito.when(this.workFlowWorkRepository.findByWorkDefinitionId(Mockito.any())).thenReturn(List.of());
		Mockito.when(this.workFlowDelegate.initWorkFlowContext(Mockito.any())).thenReturn(new WorkContext());
		Mockito.when(this.workFlowDelegate.getWorkFlowExecutionByName("test-workflow")).thenReturn(workFlow);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId("test-project")
				.works(List.of()).workFlowName("test-workflow").build());
		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "COMPLETED");
		assertNull(report.getError());

		assertNotNull(report.getWorkContext());

		Mockito.verify(this.workFlowDelegate, Mockito.times(2)).getWorkFlowExecutionByName(Mockito.any());
		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).initWorkFlowContext(Mockito.any());
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findFirstByName(Mockito.any());
	}

	@Test
	void executeWithDTOWithNoMasterWorkFlow() {
		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(Mockito.any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		Mockito.when(this.workFlowWorkRepository.findByWorkDefinitionId(Mockito.any()))
				.thenReturn(List.of(WorkFlowWorkDefinition.builder().build()));

		Mockito.when(this.workFlowDelegate.getWorkFlowExecutionByName("test-workflow")).thenReturn(workFlow);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId("test-project")
				.works(List.of()).workFlowName("test-workflow").build());
		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "FAILED");
		assertNotNull(report.getError());

		assertNotNull(report.getWorkContext());

		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).getWorkFlowExecutionByName(Mockito.any());
		Mockito.verify(this.workFlowDelegate, Mockito.times(0)).initWorkFlowContext(Mockito.any());
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findFirstByName(Mockito.any());
	}

	@Test
	void executeWithDTOWithNoWorkFlowDefinition() {
		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(Mockito.any())).thenReturn(null);
		Mockito.when(this.workFlowDelegate.getWorkFlowExecutionByName("test-workflow")).thenReturn(workFlow);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId("test-project")
				.works(List.of()).workFlowName("test-workflow").build());
		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "FAILED");
		assertNotNull(report.getError());

		assertNotNull(report.getWorkContext());

		Mockito.verify(this.workFlowDelegate, Mockito.times(1)).getWorkFlowExecutionByName(Mockito.any());
		Mockito.verify(this.workFlowDelegate, Mockito.never()).initWorkFlowContext(Mockito.any());
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findFirstByName(Mockito.any());
		Mockito.verify(this.workFlowWorkRepository, Mockito.never()).findByWorkDefinitionId(Mockito.any());
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

		WorkFlowExecution masterWorkFlowExecution = WorkFlowExecution.builder().status(WorkFlowStatus.COMPLETED)
				.build();
		masterWorkFlowExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowRepository.save(Mockito.any())).thenReturn(workFlowExecution);

		// when
		WorkFlowExecution res = this.workFlowService.saveWorkFlow(projectId, workflowDefID, WorkFlowStatus.COMPLETED,
				masterWorkFlowExecution);

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

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithValidData() {
		// given
		// master workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker
		String workFlowCheckerTaskName = "testWorkFlowTask";
		String workFlowCheckerName = "testWorkFlowCheckerName";
		UUID workFlowCheckerDefinitionId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();

		// when
		// master workflow execution
		WorkFlowExecution masterWorkFlowExecution = WorkFlowExecution.builder().status(WorkFlowStatus.FAILED)
				.projectId(projectId).workFlowDefinitionId(UUID.randomUUID()).build();
		masterWorkFlowExecution.setId(UUID.randomUUID());
		Mockito.when(this.workFlowRepository.findById(Mockito.any())).thenReturn(Optional.of(masterWorkFlowExecution));

		// workflow checker definition
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowCheckerName).build();
		workFlowDefinition.setId(workFlowCheckerDefinitionId);
		// workflow checker task definition
		WorkFlowTaskDefinition workFlowCheckerTaskDefinition = WorkFlowTaskDefinition.builder()
				.workFlowDefinition(workFlowDefinition).name(workFlowCheckerTaskName).build();
		workFlowCheckerTaskDefinition.setId(UUID.randomUUID());
		Mockito.when(this.workFlowTaskDefinitionRepository.findFirstByNameAndWorkFlowDefinitionType(Mockito.any(),
				Mockito.any())).thenReturn(workFlowCheckerTaskDefinition);

		// workflow checker task execution
		WorkFlowExecution workFlowCheckerExecution = WorkFlowExecution.builder().status(WorkFlowStatus.IN_PROGRESS)
				.projectId(projectId).workFlowDefinitionId(workFlowCheckerDefinitionId).build();
		workFlowCheckerExecution.setId(UUID.randomUUID());
		Mockito.when(this.workFlowRepository.findByMasterWorkFlowExecution(Mockito.any()))
				.thenReturn(List.of(workFlowCheckerExecution));

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkFlowTaskStatus.FAILED).workFlowTaskDefinitionId(workFlowCheckerTaskDefinition.getId())
				.workFlowExecutionId(workFlowCheckerExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());
		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(Mockito.any(),
				Mockito.any())).thenReturn(List.of(workFlowTaskExecution));

		this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
				WorkFlowTaskStatus.COMPLETED);

		// then
		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		Mockito.verify(this.workFlowTaskRepository, Mockito.times(1)).save(argument.capture());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId(), workFlowCheckerTaskDefinition.getId());
		assertEquals(argument.getValue().getWorkFlowExecutionId(), workFlowCheckerExecution.getId());
		assertEquals(argument.getValue().getStatus(), WorkFlowTaskStatus.COMPLETED);
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithInvalidData() {
		// master workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker
		String workFlowCheckerTaskName = "testWorkFlowTask";

		// given
		Mockito.when(this.workFlowRepository.findById(any())).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> {
			this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
					WorkFlowTaskStatus.COMPLETED);
		});

		Mockito.verify(this.workFlowTaskRepository, Mockito.times(0)).save(any());
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithInvalidTaskData() {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		String workFlowCheckerTaskName = "testWorkFlowTask";

		// when
		Mockito.when(this.workFlowRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(WorkFlowExecution.builder().status(WorkFlowStatus.FAILED)
						.projectId(UUID.randomUUID()).workFlowDefinitionId(UUID.randomUUID()).build()));

		Mockito.when(this.workFlowTaskDefinitionRepository.findFirstByNameAndWorkFlowDefinitionType(any(), any()))
				.thenReturn(null);

		// then
		assertThrows(ResponseStatusException.class, () -> {
			this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
					WorkFlowTaskStatus.COMPLETED);
		});

		Mockito.verify(this.workFlowTaskRepository, Mockito.never()).save(any());

	}

	private WorkFlowDefinition sampleWorkflowDefinition(String name) {
		WorkFlowDefinition wf = WorkFlowDefinition.builder().name(name).build();
		wf.setId(UUID.randomUUID());
		return wf;
	}

}
