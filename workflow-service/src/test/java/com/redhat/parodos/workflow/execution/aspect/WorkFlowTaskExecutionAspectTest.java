package com.redhat.parodos.workflow.execution.aspect;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

class WorkFlowTaskExecutionAspectTest {

	private static final String PROJECT_ID = "PROJECT_ID";

	private static final String WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS = "WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS";

	private static final String WORKFLOW_TASK_DEFINITION_TESTTASK_ID = "WORKFLOW_TASK_DEFINITION_TESTTASK_ID";

	private static final String WORKFLOW_EXECUTION_ID = "WORKFLOW_EXECUTION_ID";

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
				.thenReturn(new WorkFlowTaskExecution());

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

		Mockito.verify(this.workFlowService, Mockito.times(1)).updateWorkFlowTask(Mockito.any());
	}

	WorkContext getSampleWorkContext(UUID projectID, String taskName) {
		return new WorkContext() {
			{
				put(WORKFLOW_EXECUTION_ID, UUID.randomUUID());
				put(String.format("WORKFLOW_TASK_DEFINITION_%s_ID", taskName.toUpperCase()), UUID.randomUUID());
				put(String.format("WORKFLOW_TASK_EXECUTION_%s_ARGUMENTS", taskName.toUpperCase()), "{}");
				put(PROJECT_ID, projectID);
			}
		};
	}

	WorkFlowTask getSampleWorkFlowTask(String taskName) {
		WorkFlowTask workFlowTask = Mockito.mock(WorkFlowTask.class);
		Mockito.when(workFlowTask.getName()).thenReturn(taskName);
		return workFlowTask;
	}

}