package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkFlowTaskExecutionAspectTest {

	private WorkFlowServiceImpl workFlowExecutionService;

	private WorkFlowTaskExecutionAspect workFlowTaskExecutionAspect;

	@Mock
	private WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	@Mock
	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	@BeforeEach
	public void setUp() {
		this.workFlowExecutionService = Mockito.mock(WorkFlowServiceImpl.class);
		this.workFlowTaskExecutionAspect = new WorkFlowTaskExecutionAspect(this.workFlowExecutionService,
				workFlowSchedulerService, workFlowTaskDefinitionRepository);
	}

	@Test
	public void executeAroundAdviceTask() {

		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = getSampleWorkContext(projectID, "testTask");
		WorkFlowTask workFlowTask = getSampleWorkFlowTask("testTask");

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
		assertEquals(report.getWorkContext().get("WORKFLOW_EXECUTION_ID"), workContext.get("WORKFLOW_EXECUTION_ID"));
		assertEquals(report.getWorkContext().get("WORKFLOW_TASK_DEFINITION_TESTTASK_ID"),
				workContext.get("WORKFLOW_TASK_DEFINITION_TESTTASK_ID"));
		assertEquals(report.getWorkContext().get("WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS"),
				workContext.get("WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS"));
		assertEquals(report.getWorkContext().get("PROJECT_ID"), projectID);

		Mockito.verify(this.workFlowExecutionService, Mockito.times(1)).saveWorkFlowTask(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
	}

	@Test
	public void executeAroundAdviceTaskWithSequentialFlowWhenTaskExecutionIsNotNull() {
		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = getSampleWorkContext(projectID, "testTask");
		WorkFlowTask workFlowTask = getSampleWorkFlowTask("testTask");

		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(proceedingJoinPoint.getTarget()).thenReturn(workFlowTask);
		assertDoesNotThrow(() -> {
			Mockito.when(proceedingJoinPoint.proceed())
					.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});

		Mockito.when(this.workFlowExecutionService.getWorkFlowTask(Mockito.any(), Mockito.any()))
				.thenReturn(new WorkFlowTaskExecution());

		// when
		WorkReport report = this.workFlowTaskExecutionAspect.executeAroundAdviceTask(proceedingJoinPoint, workContext);

		// then
		assertNotNull(report);
		assertEquals(report.getStatus(), WorkStatus.COMPLETED);
		assertEquals(report.getWorkContext().get("WORKFLOW_EXECUTION_ID"), workContext.get("WORKFLOW_EXECUTION_ID"));
		assertEquals(report.getWorkContext().get("WORKFLOW_TASK_DEFINITION_TESTTASK_ID"),
				workContext.get("WORKFLOW_TASK_DEFINITION_TESTTASK_ID"));
		assertEquals(report.getWorkContext().get("WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS"),
				workContext.get("WORKFLOW_TASK_EXECUTION_TESTTASK_ARGUMENTS"));
		assertEquals(report.getWorkContext().get("PROJECT_ID"), projectID);

		Mockito.verify(this.workFlowExecutionService, Mockito.times(1)).updateWorkFlowTask(Mockito.any());
	}

	WorkContext getSampleWorkContext(UUID projectID, String taskName) {
		return new WorkContext() {
			{
				put("WORKFLOW_EXECUTION_ID", UUID.randomUUID());
				put(String.format("WORKFLOW_TASK_DEFINITION_%s_ID", taskName.toUpperCase()), UUID.randomUUID());
				put(String.format("WORKFLOW_TASK_EXECUTION_%s_ARGUMENTS", taskName.toUpperCase()), "{}");
				put("PROJECT_ID", projectID);
			}
		};
	}

	WorkFlowTask getSampleWorkFlowTask(String taskName) {
		WorkFlowTask workFlowTask = Mockito.mock(WorkFlowTask.class);
		Mockito.when(workFlowTask.getName()).thenReturn(taskName);
		return workFlowTask;
	}

}