package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.ParodosWorkStatus;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class WorkFlowServiceDelegateTest {

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowServiceDelegate workFlowServiceDelegate;

	@BeforeEach
	void initEach() {
		this.workFlowRepository = Mockito.mock(WorkFlowRepository.class);
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowTaskRepository = Mockito.mock(WorkFlowTaskRepository.class);
		this.workFlowWorkRepository = Mockito.mock(WorkFlowWorkRepository.class);

		this.workFlowServiceDelegate = new WorkFlowServiceDelegate(this.workFlowDefinitionRepository,
				this.workFlowTaskDefinitionRepository, this.workFlowRepository, this.workFlowTaskRepository,
				this.workFlowWorkRepository);
	}

	@Test
	void testGetWorkFlowWorksStatus() {
		String workFlowName = "testWorkFlow";
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();

		// workflow (main)
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowName).numberOfWorks(2)
				.build();
		workFlowDefinition.setId(workFlowDefinitionId);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinitionId(workFlowDefinitionId)
				.status(WorkFlowStatus.IN_PROGRESS).build();
		workFlowExecution.setId(workFlowExecutionId);

		// sub workflow 1
		String TEST_SUB_WORKFLOW_NAME_1 = "testSubWorkFlow1";
		UUID testSubWorkFlowDefinitionId1 = UUID.randomUUID();
		UUID testSubWorkFlowExecutionId1 = UUID.randomUUID();
		// sub workflow definition 1
		WorkFlowDefinition testSubWorkFlowDefinition1 = WorkFlowDefinition.builder().name(TEST_SUB_WORKFLOW_NAME_1)
				.numberOfWorks(1).build();
		testSubWorkFlowDefinition1.setId(testSubWorkFlowDefinitionId1);
		// sub workflow execution 1
		WorkFlowExecution testSubWorkFlowExecution1 = WorkFlowExecution.builder().projectId(projectId)
				.status(WorkFlowStatus.IN_PROGRESS).workFlowDefinitionId(testSubWorkFlowDefinitionId1)
				.mainWorkFlowExecution(workFlowExecution).build();
		testSubWorkFlowExecution1.setId(testSubWorkFlowExecutionId1);

		// sub workflow 1 task 1
		String TEST_SUB_WORKFLOW_TASK_NAME_1 = "testSubWorkFlowTask1";
		UUID testSubWorkFlowTaskDefinitionId1 = UUID.randomUUID();
		UUID testSubWorkFlowTaskExecutionId1 = UUID.randomUUID();
		// sub workflow 1 task definition 1
		WorkFlowTaskDefinition testSubWorkFlowTaskDefinition1 = WorkFlowTaskDefinition.builder()
				.name(TEST_SUB_WORKFLOW_TASK_NAME_1).build();
		testSubWorkFlowTaskDefinition1.setId(testSubWorkFlowTaskDefinitionId1);
		// link sub workflow 1 task 1 to sub workflow 1
		testSubWorkFlowDefinition1.setWorkFlowTaskDefinitions(List.of(testSubWorkFlowTaskDefinition1));
		// sub workflow 1 task execution 1
		WorkFlowTaskExecution testSubWorkFlowTaskExecution1 = WorkFlowTaskExecution.builder()
				.status(WorkFlowTaskStatus.IN_PROGRESS).workFlowExecutionId(testSubWorkFlowExecutionId1)
				.workFlowTaskDefinitionId(testSubWorkFlowTaskDefinitionId1).build();
		testSubWorkFlowTaskExecution1.setId(testSubWorkFlowTaskExecutionId1);

		// workflow task 2
		String TEST_WORKFLOW_TASK_NAME_1 = "testWorkFlowTask1";
		UUID testWorkFlowTaskDefinitionId1 = UUID.randomUUID();
		UUID testWorkFlowTaskExecutionId1 = UUID.randomUUID();
		// workflow task definition 2
		WorkFlowTaskDefinition testWorkFlowTaskDefinition1 = WorkFlowTaskDefinition.builder()
				.name(TEST_WORKFLOW_TASK_NAME_1).build();
		testWorkFlowTaskDefinition1.setId(testWorkFlowTaskDefinitionId1);
		// workflow task execution 2
		WorkFlowTaskExecution testWorkFlowTaskExecution1 = WorkFlowTaskExecution.builder()
				.status(WorkFlowTaskStatus.COMPLETED).workFlowExecutionId(workFlowExecutionId)
				.workFlowTaskDefinitionId(testWorkFlowTaskDefinitionId1).build();
		testWorkFlowTaskExecution1.setId(testWorkFlowTaskExecutionId1);
		// link workflow task definition 2 to main workFlow
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(testWorkFlowTaskDefinition1));

		// workflow's works
		WorkFlowWorkDefinition mainWorkFlowWorkDefinition1 = WorkFlowWorkDefinition.builder()
				.workDefinitionId(testSubWorkFlowDefinitionId1).workDefinitionType(WorkType.WORKFLOW)
				.workFlowDefinition(workFlowDefinition).build();
		mainWorkFlowWorkDefinition1.setId(UUID.randomUUID());

		WorkFlowWorkDefinition mainWorkFlowWorkDefinition2 = WorkFlowWorkDefinition.builder()
				.workDefinitionId(testWorkFlowTaskDefinitionId1).workDefinitionType(WorkType.TASK)
				.workFlowDefinition(workFlowDefinition).build();
		mainWorkFlowWorkDefinition2.setId(UUID.randomUUID());

		workFlowDefinition
				.setWorkFlowWorkDefinitions(List.of(mainWorkFlowWorkDefinition1, mainWorkFlowWorkDefinition2));

		WorkFlowWorkDefinition subWorkFlowWorkDefinition1 = WorkFlowWorkDefinition.builder()
				.workDefinitionId(testSubWorkFlowTaskDefinitionId1).workDefinitionType(WorkType.TASK)
				.workFlowDefinition(testSubWorkFlowDefinition1).build();
		subWorkFlowWorkDefinition1.setId(UUID.randomUUID());

		Mockito.when(this.workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(Mockito.eq(workFlowDefinitionId)))
				.thenReturn(List.of(mainWorkFlowWorkDefinition1, mainWorkFlowWorkDefinition2));

		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.eq(testSubWorkFlowDefinitionId1)))
				.thenReturn(Optional.of(testSubWorkFlowDefinition1));

		Mockito.when(this.workFlowRepository.findFirstByMainWorkFlowExecutionAndWorkFlowDefinitionId(
				Mockito.eq(workFlowExecution), Mockito.eq(testSubWorkFlowDefinitionId1)))
				.thenReturn(testSubWorkFlowExecution1);

		Mockito.when(this.workFlowTaskDefinitionRepository.findById(Mockito.eq(testSubWorkFlowTaskDefinitionId1)))
				.thenReturn(Optional.of(testSubWorkFlowTaskDefinition1));

		Mockito.when(this.workFlowTaskDefinitionRepository.findById(Mockito.eq(testWorkFlowTaskDefinitionId1)))
				.thenReturn(Optional.of(testWorkFlowTaskDefinition1));

		Mockito.when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
				Mockito.eq(testSubWorkFlowExecutionId1), Mockito.eq(testSubWorkFlowTaskDefinitionId1)))
				.thenReturn(List.of(testSubWorkFlowTaskExecution1));

		Mockito.when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
				Mockito.eq(workFlowExecutionId), Mockito.eq(testWorkFlowTaskDefinitionId1)))
				.thenReturn(List.of(testWorkFlowTaskExecution1));

		Mockito.when(this.workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(Mockito.eq(testSubWorkFlowDefinitionId1)))
				.thenReturn(List.of(subWorkFlowWorkDefinition1));

		// then
		List<WorkStatusResponseDTO> workStatusResponseDTOs = this.workFlowServiceDelegate
				.getWorkFlowAndWorksStatus(workFlowExecution, workFlowDefinition);

		// workflow
		assertNotNull(workStatusResponseDTOs);
		assertEquals(workStatusResponseDTOs.size(), 2);

		// sub workflow 1
		assertEquals(workStatusResponseDTOs.get(0).getType(), WorkType.WORKFLOW);
		assertEquals(workStatusResponseDTOs.get(0).getName(), testSubWorkFlowDefinition1.getName());
		assertEquals(workStatusResponseDTOs.get(0).getStatus(), ParodosWorkStatus.IN_PROGRESS);
		assertEquals(workStatusResponseDTOs.get(0).getWorks().size(), 1);

		// sub workflow 1 task 1
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getType(), WorkType.TASK);
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getName(),
				testSubWorkFlowTaskDefinition1.getName());
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getStatus(), ParodosWorkStatus.IN_PROGRESS);
		assertNull(workStatusResponseDTOs.get(0).getWorks().get(0).getWorks());

		// workflow task 1
		assertEquals(workStatusResponseDTOs.get(1).getType(), WorkType.TASK);
		assertEquals(workStatusResponseDTOs.get(1).getName(), testWorkFlowTaskDefinition1.getName());
		assertEquals(workStatusResponseDTOs.get(1).getStatus(), ParodosWorkStatus.COMPLETED);
		assertNull(workStatusResponseDTOs.get(1).getWorks());
	}

	@Nested
	@DisplayName("Tests for workflow with checker")
	class TestGetWorkFlowWorksStatusWithChecker {

		// master workflow vars
		final String workFlowName = "testMasterWorkFlow";

		final UUID masterWorkFlowExecutionId = UUID.randomUUID();

		final UUID masterWorkFlowDefinitionId = UUID.randomUUID();

		final UUID projectId = UUID.randomUUID();

		// master workflow task vars
		final String TEST_SUB_WORKFLOW_TASK_NAME = "testSubWorkFlowTask";

		final UUID subWorkFlowTaskDefinitionId = UUID.randomUUID();

		final UUID subWorkFlowTaskExecutionId = UUID.randomUUID();

		// checker workflow vars
		final UUID checkerWorkFlowExecutionId = UUID.randomUUID();

		final UUID checkerWorkFlowDefinitionId = UUID.randomUUID();

		final String checkerWorkFlowName = "testCheckerWorkFlow";

		// checker workflow task vars
		final String CHECKER_WORKFLOW_TASK_NAME_1 = "testWorkFlowTask1";

		final UUID checkerWorkFlowTaskDefinitionId = UUID.randomUUID();

		final UUID testCheckerWorkFlowTaskExecutionId1 = UUID.randomUUID();

		WorkFlowDefinition masterWorkflowDefinition;

		WorkFlowExecution masterWorkflowExecution;

		WorkFlowTaskDefinition masterWorkFlowTaskDefinition;

		WorkFlowTaskExecution masterWorkFlowTaskExecution;

		WorkFlowDefinition checkerWorkflowDefinition;

		WorkFlowExecution checkerWorkflowExecution;

		WorkFlowTaskDefinition checkerWorkFlowTaskDefinition;

		WorkFlowTaskExecution checkerWorkFlowTaskExecution;

		WorkFlowWorkDefinition masterWorkflowWorkDefinition;

		WorkFlowWorkDefinition checkerWorkflowWorkDefinition;

		@BeforeEach
		void beforeEach() {
			setupCheckerWorkflow();
			setupMasterWorkflow();
			setupCheckerMapping();

			Mockito.when(
					workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(masterWorkFlowDefinitionId))
					.thenReturn(List.of(masterWorkflowWorkDefinition, checkerWorkflowWorkDefinition));

			// master
			Mockito.when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
					masterWorkFlowExecutionId, subWorkFlowTaskDefinitionId))
					.thenReturn(List.of(masterWorkFlowTaskExecution));

			Mockito.when(workFlowTaskDefinitionRepository.findById(subWorkFlowTaskDefinitionId))
					.thenReturn(Optional.of(masterWorkFlowTaskDefinition));

			Mockito.when(
					workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(masterWorkFlowDefinitionId))
					.thenReturn(List.of(masterWorkflowWorkDefinition));

			// checker
			Mockito.when(workFlowTaskDefinitionRepository.findById(checkerWorkFlowTaskDefinitionId))
					.thenReturn(Optional.of(checkerWorkFlowTaskDefinition));

			Mockito.when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
					masterWorkFlowExecutionId, checkerWorkFlowTaskDefinitionId))
					.thenReturn(List.of(checkerWorkFlowTaskExecution));

			Mockito.when(
					workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(checkerWorkFlowDefinitionId))
					.thenReturn(List.of(checkerWorkflowWorkDefinition));

			Mockito.when(workFlowRepository.findFirstByMainWorkFlowExecutionAndWorkFlowDefinitionId(
					Mockito.nullable(WorkFlowExecution.class), Mockito.eq(checkerWorkFlowDefinitionId)))
					.thenReturn(checkerWorkflowExecution);
		}

		@Test
		void testGetWorkFlowWorksStatusWithChecker_when_checkerIsFailed_then_taskShouldBeInProgress() {
			checkerWorkflowExecution.setStatus(WorkFlowStatus.FAILED);

			// then
			List<WorkStatusResponseDTO> workStatusResponseDTOs = workFlowServiceDelegate
					.getWorkFlowAndWorksStatus(masterWorkflowExecution, masterWorkflowDefinition);

			// workflow
			assertNotNull(workStatusResponseDTOs);
			assertEquals(workStatusResponseDTOs.size(), 1);

			// sub task
			assertEquals(WorkType.TASK, workStatusResponseDTOs.get(0).getType());
			assertEquals(workStatusResponseDTOs.get(0).getName(), masterWorkFlowTaskDefinition.getName());
			assertEquals(ParodosWorkStatus.IN_PROGRESS, workStatusResponseDTOs.get(0).getStatus());
			assertNull(workStatusResponseDTOs.get(0).getWorks());
		}

		@Test
		void testGetWorkFlowWorksStatusWithChecker_when_checkerIsCompleted_then_taskShouldBeCompleted() {
			checkerWorkflowExecution.setStatus(WorkFlowStatus.COMPLETED);

			// then
			List<WorkStatusResponseDTO> workStatusResponseDTOs = workFlowServiceDelegate
					.getWorkFlowAndWorksStatus(masterWorkflowExecution, masterWorkflowDefinition);

			// workflow
			assertNotNull(workStatusResponseDTOs);
			assertEquals(workStatusResponseDTOs.size(), 1);

			// sub task
			assertEquals(WorkType.TASK, workStatusResponseDTOs.get(0).getType());
			assertEquals(workStatusResponseDTOs.get(0).getName(), masterWorkFlowTaskDefinition.getName());
			assertEquals(ParodosWorkStatus.COMPLETED, workStatusResponseDTOs.get(0).getStatus());
			assertNull(workStatusResponseDTOs.get(0).getWorks());
		}

		@Test
		void testGetWorkFlowWorksStatusWithChecker_when_checkerIsRejected_then_taskShouldBeRejected() {
			checkerWorkflowExecution.setStatus(WorkFlowStatus.REJECTED);

			// then
			List<WorkStatusResponseDTO> workStatusResponseDTOs = workFlowServiceDelegate
					.getWorkFlowAndWorksStatus(masterWorkflowExecution, masterWorkflowDefinition);

			// workflow
			assertNotNull(workStatusResponseDTOs);
			assertEquals(workStatusResponseDTOs.size(), 1);

			// sub task
			assertEquals(WorkType.TASK, workStatusResponseDTOs.get(0).getType());
			assertEquals(workStatusResponseDTOs.get(0).getName(), masterWorkFlowTaskDefinition.getName());
			assertEquals(ParodosWorkStatus.REJECTED, workStatusResponseDTOs.get(0).getStatus());
			assertNull(workStatusResponseDTOs.get(0).getWorks());
		}

		private void setupCheckerMapping() {
			WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = WorkFlowCheckerMappingDefinition
					.builder().cronExpression("test-cron").checkWorkFlow(checkerWorkflowDefinition)
					.tasks(new ArrayList<>(List.of(masterWorkFlowTaskDefinition))).build();
			masterWorkFlowTaskDefinition.setWorkFlowCheckerMappingDefinition(workFlowCheckerMappingDefinition);
		}

		private void setupCheckerWorkflow() {
			// task definition
			checkerWorkFlowTaskDefinition = WorkFlowTaskDefinition.builder().name(CHECKER_WORKFLOW_TASK_NAME_1).build();
			checkerWorkFlowTaskDefinition.setId(checkerWorkFlowTaskDefinitionId);

			// task execution
			checkerWorkFlowTaskExecution = WorkFlowTaskExecution.builder()
					.workFlowExecutionId(checkerWorkFlowExecutionId)
					.workFlowTaskDefinitionId(checkerWorkFlowTaskDefinitionId).build();
			checkerWorkFlowTaskExecution.setId(testCheckerWorkFlowTaskExecutionId1);

			// checker workflow
			// workflow definition
			checkerWorkflowDefinition = WorkFlowDefinition.builder().name(checkerWorkFlowName).numberOfWorks(1)
					.workFlowTaskDefinitions(List.of(checkerWorkFlowTaskDefinition)).build();
			checkerWorkflowDefinition.setId(checkerWorkFlowDefinitionId);
			// workflow execution
			checkerWorkflowExecution = WorkFlowExecution.builder().workFlowDefinitionId(checkerWorkFlowDefinitionId)
					.status(WorkFlowStatus.FAILED).build();
			checkerWorkflowExecution.setId(checkerWorkFlowExecutionId);

			// workflowWork
			checkerWorkflowWorkDefinition = WorkFlowWorkDefinition.builder()
					.workDefinitionId(checkerWorkFlowTaskDefinitionId).workDefinitionType(WorkType.TASK)
					.workFlowDefinition(checkerWorkflowDefinition).build();
			checkerWorkflowWorkDefinition.setId(UUID.randomUUID());

			checkerWorkflowDefinition.setWorkFlowWorkDefinitions(List.of(checkerWorkflowWorkDefinition));
		}

		private void setupMasterWorkflow() {

			// workflow (master)
			masterWorkflowDefinition = WorkFlowDefinition.builder().name(workFlowName).numberOfWorks(1).build();
			masterWorkflowDefinition.setId(masterWorkFlowDefinitionId);

			masterWorkflowExecution = WorkFlowExecution.builder().workFlowDefinitionId(masterWorkFlowDefinitionId)
					.projectId(projectId).status(WorkFlowStatus.IN_PROGRESS).build();
			masterWorkflowExecution.setId(masterWorkFlowExecutionId);

			// sub task
			// task definition
			masterWorkFlowTaskDefinition = WorkFlowTaskDefinition.builder().name(TEST_SUB_WORKFLOW_TASK_NAME).build();
			masterWorkFlowTaskDefinition.setId(subWorkFlowTaskDefinitionId);
			// link sub task to master
			masterWorkflowDefinition.setWorkFlowTaskDefinitions(List.of(masterWorkFlowTaskDefinition));
			// sub task execution
			masterWorkFlowTaskExecution = WorkFlowTaskExecution.builder().status(WorkFlowTaskStatus.IN_PROGRESS)
					.workFlowExecutionId(masterWorkFlowExecutionId)
					.workFlowTaskDefinitionId(subWorkFlowTaskDefinitionId).build();
			masterWorkFlowTaskExecution.setId(subWorkFlowTaskExecutionId);

			// workflow's works
			masterWorkflowWorkDefinition = WorkFlowWorkDefinition.builder()
					.workDefinitionId(subWorkFlowTaskDefinitionId).workDefinitionType(WorkType.TASK)
					.workFlowDefinition(masterWorkflowDefinition).build();
			masterWorkflowWorkDefinition.setId(UUID.randomUUID());
			masterWorkflowDefinition.setWorkFlowWorkDefinitions(List.of(masterWorkflowWorkDefinition));
		}

	}

}