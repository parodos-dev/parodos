package com.redhat.parodos.workflow.execution.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.when;

public class WorkFlowServiceDelegateTest {

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowServiceDelegate workFlowServiceDelegate;

	@BeforeEach
	void initEach() {
		this.workFlowRepository = mock(WorkFlowRepository.class);
		this.workFlowDefinitionRepository = mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowTaskRepository = mock(WorkFlowTaskRepository.class);
		this.workFlowWorkRepository = mock(WorkFlowWorkRepository.class);

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

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(workFlowDefinition)
				.status(WorkStatus.IN_PROGRESS).build();
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
				.status(WorkStatus.IN_PROGRESS).workFlowDefinition(testSubWorkFlowDefinition1)
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
				.status(WorkStatus.IN_PROGRESS).workFlowExecutionId(testSubWorkFlowExecutionId1)
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
		WorkFlowTaskExecution testWorkFlowTaskExecution1 = WorkFlowTaskExecution.builder().status(WorkStatus.COMPLETED)
				.workFlowExecutionId(workFlowExecutionId).workFlowTaskDefinitionId(testWorkFlowTaskDefinitionId1)
				.build();
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

		when(this.workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(eq(workFlowDefinitionId)))
				.thenReturn(List.of(mainWorkFlowWorkDefinition1, mainWorkFlowWorkDefinition2));

		when(this.workFlowDefinitionRepository.findById(eq(testSubWorkFlowDefinitionId1)))
				.thenReturn(Optional.of(testSubWorkFlowDefinition1));

		when(this.workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
				eq(testSubWorkFlowDefinitionId1), eq(workFlowExecution))).thenReturn(testSubWorkFlowExecution1);

		when(this.workFlowTaskDefinitionRepository.findById(eq(testSubWorkFlowTaskDefinitionId1)))
				.thenReturn(Optional.of(testSubWorkFlowTaskDefinition1));

		when(this.workFlowTaskDefinitionRepository.findById(eq(testWorkFlowTaskDefinitionId1)))
				.thenReturn(Optional.of(testWorkFlowTaskDefinition1));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
				eq(testSubWorkFlowExecutionId1), eq(testSubWorkFlowTaskDefinitionId1)))
						.thenReturn(List.of(testSubWorkFlowTaskExecution1));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(eq(workFlowExecutionId),
				eq(testWorkFlowTaskDefinitionId1))).thenReturn(List.of(testWorkFlowTaskExecution1));

		when(this.workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(eq(testSubWorkFlowDefinitionId1)))
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
		assertEquals(workStatusResponseDTOs.get(0).getStatus(), WorkStatus.IN_PROGRESS);
		assertEquals(workStatusResponseDTOs.get(0).getWorks().size(), 1);

		// sub workflow 1 task 1
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getType(), WorkType.TASK);
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getName(),
				testSubWorkFlowTaskDefinition1.getName());
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getStatus(), WorkStatus.IN_PROGRESS);
		assertNull(workStatusResponseDTOs.get(0).getWorks().get(0).getWorks());

		// workflow task 1
		assertEquals(workStatusResponseDTOs.get(1).getType(), WorkType.TASK);
		assertEquals(workStatusResponseDTOs.get(1).getName(), testWorkFlowTaskDefinition1.getName());
		assertEquals(workStatusResponseDTOs.get(1).getStatus(), WorkStatus.COMPLETED);
		assertNull(workStatusResponseDTOs.get(1).getWorks());
	}

	@Nested
	@DisplayName("Tests for workflow with checker")
	class TestGetWorkFlowWorksStatusWithChecker {

		// master workflow vars
		private static final String workFlowName = "testMasterWorkFlow";

		private static final UUID masterWorkFlowExecutionId = UUID.randomUUID();

		private static final UUID masterWorkFlowDefinitionId = UUID.randomUUID();

		private static final UUID projectId = UUID.randomUUID();

		// master workflow task vars
		private static final String TEST_SUB_WORKFLOW_TASK_NAME = "testSubWorkFlowTask";

		private static final UUID subWorkFlowTaskDefinitionId = UUID.randomUUID();

		private static final UUID subWorkFlowTaskExecutionId = UUID.randomUUID();

		// checker workflow vars
		private static final UUID checkerWorkFlowExecutionId = UUID.randomUUID();

		private static final UUID checkerWorkFlowDefinitionId = UUID.randomUUID();

		private static final String checkerWorkFlowName = "testCheckerWorkFlow";

		// checker workflow task vars
		private static final String CHECKER_WORKFLOW_TASK_NAME_1 = "testWorkFlowTask1";

		private static final UUID checkerWorkFlowTaskDefinitionId = UUID.randomUUID();

		private static final UUID testCheckerWorkFlowTaskExecutionId1 = UUID.randomUUID();

		private WorkFlowDefinition masterWorkflowDefinition;

		private WorkFlowExecution masterWorkflowExecution;

		private WorkFlowTaskDefinition masterWorkFlowTaskDefinition;

		private WorkFlowTaskExecution masterWorkFlowTaskExecution;

		private WorkFlowDefinition checkerWorkflowDefinition;

		private WorkFlowExecution checkerWorkflowExecution;

		private WorkFlowTaskDefinition checkerWorkFlowTaskDefinition;

		private WorkFlowTaskExecution checkerWorkFlowTaskExecution;

		private WorkFlowWorkDefinition masterWorkflowWorkDefinition;

		private WorkFlowWorkDefinition checkerWorkflowWorkDefinition;

		@BeforeEach
		void beforeEach() {
			setupCheckerWorkflow();
			setupMasterWorkflow();
			setupCheckerMapping();

			when(workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(masterWorkFlowDefinitionId))
					.thenReturn(List.of(masterWorkflowWorkDefinition, checkerWorkflowWorkDefinition));

			// master
			when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(masterWorkFlowExecutionId,
					subWorkFlowTaskDefinitionId)).thenReturn(List.of(masterWorkFlowTaskExecution));

			when(workFlowTaskDefinitionRepository.findById(subWorkFlowTaskDefinitionId))
					.thenReturn(Optional.of(masterWorkFlowTaskDefinition));

			when(workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(masterWorkFlowDefinitionId))
					.thenReturn(List.of(masterWorkflowWorkDefinition));

			// checker
			when(workFlowTaskDefinitionRepository.findById(checkerWorkFlowTaskDefinitionId))
					.thenReturn(Optional.of(checkerWorkFlowTaskDefinition));

			when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(masterWorkFlowExecutionId,
					checkerWorkFlowTaskDefinitionId)).thenReturn(List.of(checkerWorkFlowTaskExecution));

			when(workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(checkerWorkFlowDefinitionId))
					.thenReturn(List.of(checkerWorkflowWorkDefinition));

			when(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
					eq(checkerWorkFlowDefinitionId), nullable(WorkFlowExecution.class)))
							.thenReturn(checkerWorkflowExecution);
		}

		@Test
		void testGetWorkFlowWorksStatusWithChecker_when_checkerIsFailed_then_taskShouldBeInProgress() {
			checkerWorkflowExecution.setStatus(WorkStatus.FAILED);

			// then
			List<WorkStatusResponseDTO> workStatusResponseDTOs = workFlowServiceDelegate
					.getWorkFlowAndWorksStatus(masterWorkflowExecution, masterWorkflowDefinition);

			// workflow
			assertNotNull(workStatusResponseDTOs);
			assertEquals(workStatusResponseDTOs.size(), 1);

			// sub task
			assertEquals(WorkType.TASK, workStatusResponseDTOs.get(0).getType());
			assertEquals(workStatusResponseDTOs.get(0).getName(), masterWorkFlowTaskDefinition.getName());
			assertEquals(WorkStatus.IN_PROGRESS, workStatusResponseDTOs.get(0).getStatus());
			assertNull(workStatusResponseDTOs.get(0).getWorks());
		}

		@Test
		void testGetWorkFlowWorksStatusWithChecker_when_checkerIsCompleted_then_taskShouldBeCompleted() {
			checkerWorkflowExecution.setStatus(WorkStatus.COMPLETED);

			// then
			List<WorkStatusResponseDTO> workStatusResponseDTOs = workFlowServiceDelegate
					.getWorkFlowAndWorksStatus(masterWorkflowExecution, masterWorkflowDefinition);

			// workflow
			assertNotNull(workStatusResponseDTOs);
			assertEquals(workStatusResponseDTOs.size(), 1);

			// sub task
			assertEquals(WorkType.TASK, workStatusResponseDTOs.get(0).getType());
			assertEquals(workStatusResponseDTOs.get(0).getName(), masterWorkFlowTaskDefinition.getName());
			assertEquals(WorkStatus.COMPLETED, workStatusResponseDTOs.get(0).getStatus());
			assertNull(workStatusResponseDTOs.get(0).getWorks());
		}

		@Test
		void testGetWorkFlowWorksStatusWithChecker_when_checkerIsRejected_then_taskShouldBeRejected() {
			checkerWorkflowExecution.setStatus(WorkStatus.REJECTED);

			// then
			List<WorkStatusResponseDTO> workStatusResponseDTOs = workFlowServiceDelegate
					.getWorkFlowAndWorksStatus(masterWorkflowExecution, masterWorkflowDefinition);

			// workflow
			assertNotNull(workStatusResponseDTOs);
			assertEquals(workStatusResponseDTOs.size(), 1);

			// sub task
			assertEquals(WorkType.TASK, workStatusResponseDTOs.get(0).getType());
			assertEquals(workStatusResponseDTOs.get(0).getName(), masterWorkFlowTaskDefinition.getName());
			assertEquals(WorkStatus.REJECTED, workStatusResponseDTOs.get(0).getStatus());
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
			checkerWorkflowExecution = WorkFlowExecution.builder().workFlowDefinition(checkerWorkflowDefinition)
					.status(WorkStatus.FAILED).build();
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

			masterWorkflowExecution = WorkFlowExecution.builder().workFlowDefinition(masterWorkflowDefinition)
					.projectId(projectId).status(WorkStatus.IN_PROGRESS).build();
			masterWorkflowExecution.setId(masterWorkFlowExecutionId);

			// sub task
			// task definition
			masterWorkFlowTaskDefinition = WorkFlowTaskDefinition.builder().name(TEST_SUB_WORKFLOW_TASK_NAME).build();
			masterWorkFlowTaskDefinition.setId(subWorkFlowTaskDefinitionId);
			// link sub task to master
			masterWorkflowDefinition.setWorkFlowTaskDefinitions(List.of(masterWorkFlowTaskDefinition));
			// sub task execution
			masterWorkFlowTaskExecution = WorkFlowTaskExecution.builder().status(WorkStatus.IN_PROGRESS)
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
