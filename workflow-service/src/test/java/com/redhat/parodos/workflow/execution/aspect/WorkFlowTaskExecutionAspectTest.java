package com.redhat.parodos.workflow.execution.aspect;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkFlowTaskExecutionAspectTest {

	private static final String PROJECT_ID = "PROJECT_ID";

	private static final String WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS = "WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS";

	private static final String WORKFLOW_TASK_DEFINITION_TESTTASK_ID = "WORKFLOW_TASK_DEFINITION_TESTTASK_ID";

	private static final String WORKFLOW_EXECUTION_ID = "WORKFLOW_EXECUTION_ID";

	private static final String WORKFLOW_DEFINITION_NAME = "WORKFLOW_DEFINITION_NAME";

	private static final String TEST_WORK_FLOW = "testWorkFlow";

	private static final String TEST_TASK = "testTask";

	private WorkFlowServiceImpl workFlowService;

	private WorkFlowTaskExecutionAspect workFlowTaskExecutionAspect;

	@Mock
	private WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private WorkFlowRepository workFlowRepository = Mockito.mock(WorkFlowRepository.class);

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository = Mockito
			.mock(WorkFlowTaskDefinitionRepository.class);

	@BeforeEach
	public void setUp() {
		this.workFlowService = Mockito.mock(WorkFlowServiceImpl.class);
		this.workFlowTaskExecutionAspect = new WorkFlowTaskExecutionAspect(workFlowRepository,
				workFlowTaskDefinitionRepository, workFlowService, workFlowSchedulerService);
		WorkFlowTaskDefinition workFlowTaskDefinition = Mockito.mock(WorkFlowTaskDefinition.class);
		Mockito.when(workFlowTaskDefinition.getId()).thenReturn(UUID.randomUUID());
		Mockito.when(workFlowTaskDefinitionRepository.findFirstByName(Mockito.any()))
				.thenReturn(workFlowTaskDefinition);
	}

	@Test
	public void executeAroundAdviceTask() {
		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = getSampleWorkContext(projectID, TEST_TASK);
		WorkFlowTask workFlowTask = getSampleWorkFlowTask(TEST_TASK);

		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(proceedingJoinPoint.getTarget()).thenReturn(workFlowTask);
		assertDoesNotThrow(() -> {
			Mockito.when(proceedingJoinPoint.proceed())
					.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});

		WorkFlowExecution workFlowExecution = getSampleWorkFlowExecution();
		Mockito.when(workFlowRepository.findById(Mockito.any())).thenReturn(Optional.of(workFlowExecution));
		Mockito.when(workFlowTaskDefinitionRepository.findFirstByName(Mockito.anyString()))
				.thenReturn(getSampleWorkFlowTaskDefinition(TEST_TASK));
		Mockito.when(workFlowService.saveWorkFlowTask(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(getSampleWorkFlowTaskExecution());

		// when
		WorkReport report = this.workFlowTaskExecutionAspect.executeAroundAdviceTask(proceedingJoinPoint, workContext);

		// then
		assertNotNull(report);
		assertEquals(report.getStatus(), WorkStatus.COMPLETED);
		assertEquals(report.getWorkContext().get(WORKFLOW_EXECUTION_ID), workContext.get(WORKFLOW_EXECUTION_ID));
		assertEquals(report.getWorkContext().get(WORKFLOW_TASK_DEFINITION_TESTTASK_ID),
				workContext.get(WORKFLOW_TASK_DEFINITION_TESTTASK_ID));
		assertEquals(report.getWorkContext().get(WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS),
				workContext.get(WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS));
		assertEquals(report.getWorkContext().get(PROJECT_ID), projectID);

		Mockito.verify(this.workFlowService, Mockito.times(1)).saveWorkFlowTask(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
	}

	@Test
	public void executeAroundAdviceTaskWithSequentialFlowWhenTaskExecutionIsNotNull() {
		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = getSampleWorkContext(projectID, TEST_TASK);
		WorkFlowTask workFlowTask = getSampleWorkFlowTask(TEST_TASK);

		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(proceedingJoinPoint.getTarget()).thenReturn(workFlowTask);
		assertDoesNotThrow(() -> {
			Mockito.when(proceedingJoinPoint.proceed())
					.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});

		Mockito.when(this.workFlowService.getWorkFlowTask(Mockito.any(), Mockito.any()))
				.thenReturn(getSampleWorkFlowTaskExecution());

		WorkFlowExecution workFlowExecution = getSampleWorkFlowExecution();
		Mockito.when(workFlowRepository.findById(Mockito.any())).thenReturn(Optional.of(workFlowExecution));
		Mockito.when(workFlowTaskDefinitionRepository.findFirstByName(Mockito.anyString()))
				.thenReturn(getSampleWorkFlowTaskDefinition(TEST_TASK));
		Mockito.when(workFlowService.saveWorkFlowTask(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(getSampleWorkFlowTaskExecution());

		// when
		WorkReport report = this.workFlowTaskExecutionAspect.executeAroundAdviceTask(proceedingJoinPoint, workContext);

		// then
		assertNotNull(report);
		assertEquals(WorkStatus.COMPLETED, report.getStatus());
		assertEquals(report.getWorkContext().get(WORKFLOW_EXECUTION_ID), workContext.get(WORKFLOW_EXECUTION_ID));
		assertEquals(report.getWorkContext().get(WORKFLOW_TASK_DEFINITION_TESTTASK_ID),
				workContext.get(WORKFLOW_TASK_DEFINITION_TESTTASK_ID));
		assertEquals(report.getWorkContext().get(WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS),
				workContext.get(WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS));
		assertEquals(report.getWorkContext().get(PROJECT_ID), projectID);

		Mockito.verify(this.workFlowService, Mockito.times(1)).updateWorkFlowTask(Mockito.any());
	}

	WorkContext getSampleWorkContext(UUID projectID, String taskName) {
		return new WorkContext() {
			{
				put(WORKFLOW_EXECUTION_ID, UUID.randomUUID());
				put(String.format("WORKFLOW_TASK_DEFINITION_%s_ID", taskName.toUpperCase()), UUID.randomUUID());
				put(String.format("WORKFLOW_TASK_EXECUTION_%s_ARGUMENTS", taskName.toUpperCase()), "{}");
				put(PROJECT_ID, projectID);
				put(WORKFLOW_DEFINITION_NAME, TEST_WORK_FLOW);
			}
		};
	}

	WorkFlowTask getSampleWorkFlowTask(String taskName) {
		WorkFlowTask workFlowTask = Mockito.mock(WorkFlowTask.class);
		Mockito.when(workFlowTask.getName()).thenReturn(taskName);
		return workFlowTask;
	}

	WorkFlowExecution getSampleWorkFlowExecution() {
		return new WorkFlowExecution() {
			{
				setId(UUID.randomUUID());
				setStatus(WorkFlowStatus.IN_PROGRESS);
				setProjectId(UUID.randomUUID());
			}
		};
	}

	WorkFlowDefinition getSampleWorkFlowDefinition(String name) {
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().type(WorkFlowType.INFRASTRUCTURE)
				.name(name).build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

	WorkFlowTaskDefinition getSampleWorkFlowTaskDefinition(String name) {

		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name(name)
				.workFlowDefinition(getSampleWorkFlowDefinition(TEST_WORK_FLOW)).build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	WorkFlowTaskExecution getSampleWorkFlowTaskExecution() {
		return new WorkFlowTaskExecution() {
			{
				setId(UUID.randomUUID());
				setStatus(WorkFlowTaskStatus.FAILED);
				setWorkFlowExecutionId(getSampleWorkFlowExecution().getId());
				setWorkFlowTaskDefinitionId(getSampleWorkFlowTaskDefinition(TEST_TASK).getId());
			}
		};
	}

}