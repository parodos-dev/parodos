package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkStatus;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

		// workflow (master)
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
				.masterWorkFlowExecution(workFlowExecution).build();
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
		// link workflow task definition 2 to master workFlow
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(testWorkFlowTaskDefinition1));

		// workflow's works
		WorkFlowWorkDefinition masterWorkFlowWorkDefinition1 = WorkFlowWorkDefinition.builder()
				.workDefinitionId(testSubWorkFlowDefinitionId1).workDefinitionType(WorkType.WORKFLOW)
				.workFlowDefinition(workFlowDefinition).build();
		masterWorkFlowWorkDefinition1.setId(UUID.randomUUID());

		WorkFlowWorkDefinition masterWorkFlowWorkDefinition2 = WorkFlowWorkDefinition.builder()
				.workDefinitionId(testWorkFlowTaskDefinitionId1).workDefinitionType(WorkType.TASK)
				.workFlowDefinition(workFlowDefinition).build();
		masterWorkFlowWorkDefinition2.setId(UUID.randomUUID());

		workFlowDefinition
				.setWorkFlowWorkDefinitions(List.of(masterWorkFlowWorkDefinition1, masterWorkFlowWorkDefinition2));

		WorkFlowWorkDefinition subWorkFlowWorkDefinition1 = WorkFlowWorkDefinition.builder()
				.workDefinitionId(testSubWorkFlowTaskDefinitionId1).workDefinitionType(WorkType.TASK)
				.workFlowDefinition(testSubWorkFlowDefinition1).build();
		subWorkFlowWorkDefinition1.setId(UUID.randomUUID());

		Mockito.when(this.workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(Mockito.eq(workFlowDefinitionId)))
				.thenReturn(List.of(masterWorkFlowWorkDefinition1, masterWorkFlowWorkDefinition2));

		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.eq(testSubWorkFlowDefinitionId1)))
				.thenReturn(Optional.of(testSubWorkFlowDefinition1));

		Mockito.when(this.workFlowRepository.findFirstByMasterWorkFlowExecutionAndWorkFlowDefinitionId(
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
		assertEquals(workStatusResponseDTOs.get(0).getStatus(), WorkStatus.PENDING);
		assertEquals(workStatusResponseDTOs.get(0).getWorks().size(), 1);

		// sub workflow 1 task 1
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getType(), WorkType.TASK);
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getName(),
				testSubWorkFlowTaskDefinition1.getName());
		assertEquals(workStatusResponseDTOs.get(0).getWorks().get(0).getStatus(), WorkStatus.PENDING);
		assertNull(workStatusResponseDTOs.get(0).getWorks().get(0).getWorks());

		// workflow task 1
		assertEquals(workStatusResponseDTOs.get(1).getType(), WorkType.TASK);
		assertEquals(workStatusResponseDTOs.get(1).getName(), testWorkFlowTaskDefinition1.getName());
		assertEquals(workStatusResponseDTOs.get(1).getStatus(), WorkStatus.COMPLETED);
		assertNull(workStatusResponseDTOs.get(1).getWorks());
	}

}