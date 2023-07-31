package com.redhat.parodos.workflow.execution.aspect;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

	private final WorkFlowRepository workFlowRepository = mock(WorkFlowRepository.class);

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository = mock(
			WorkFlowTaskDefinitionRepository.class);

	@BeforeEach
	public void setUp() {
		this.workFlowService = mock(WorkFlowServiceImpl.class);
		this.workFlowTaskExecutionAspect = new WorkFlowTaskExecutionAspect(workFlowRepository,
				workFlowTaskDefinitionRepository, workFlowService, workFlowSchedulerService);
		WorkFlowTaskDefinition workFlowTaskDefinition = mock(WorkFlowTaskDefinition.class);
		when(workFlowTaskDefinition.getId()).thenReturn(UUID.randomUUID());
		when(workFlowTaskDefinitionRepository.findFirstByName(any())).thenReturn(workFlowTaskDefinition);
	}

	@Test
	public void executeAroundAdviceTask() {
		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = getSampleWorkContext(projectID, TEST_TASK);
		BaseWorkFlowTask workFlowTask = getSampleWorkFlowTask(TEST_TASK);

		ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		when(proceedingJoinPoint.getTarget()).thenReturn(workFlowTask);
		assertDoesNotThrow(() -> {
			when(proceedingJoinPoint.proceed()).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});

		WorkFlowExecution workFlowExecution = getSampleWorkFlowExecution();
		when(workFlowRepository.findById(any())).thenReturn(Optional.of(workFlowExecution));
		when(workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(getSampleWorkFlowTaskDefinition(TEST_TASK));
		when(workFlowService.saveWorkFlowTask(any(), any(), any(), any())).thenReturn(getSampleWorkFlowTaskExecution());

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

		verify(this.workFlowService, times(1)).saveWorkFlowTask(any(), any(), any(), any());
	}

	@Test
	public void executeAroundAdviceTaskWithSequentialFlowWhenTaskExecutionIsNotNull() {
		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = getSampleWorkContext(projectID, TEST_TASK);
		WorkFlowTask workFlowTask = getSampleWorkFlowTask(TEST_TASK);

		ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		when(proceedingJoinPoint.getTarget()).thenReturn(workFlowTask);
		assertDoesNotThrow(() -> {
			when(proceedingJoinPoint.proceed()).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});

		when(this.workFlowService.getWorkFlowTask(any(), any())).thenReturn(getSampleWorkFlowTaskExecution());

		WorkFlowExecution workFlowExecution = getSampleWorkFlowExecution();
		when(workFlowRepository.findById(any())).thenReturn(Optional.of(workFlowExecution));
		when(workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(getSampleWorkFlowTaskDefinition(TEST_TASK));
		when(workFlowService.saveWorkFlowTask(any(), any(), any(), any())).thenReturn(getSampleWorkFlowTaskExecution());

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

		verify(this.workFlowService, times(1)).updateWorkFlowTask(any());
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

	BaseWorkFlowTask getSampleWorkFlowTask(String taskName) {
		BaseWorkFlowTask workFlowTask = mock(BaseWorkFlowTask.class);
		when(workFlowTask.getName()).thenReturn(taskName);
		return workFlowTask;
	}

	WorkFlowExecution getSampleWorkFlowExecution() {
		return new WorkFlowExecution() {
			{
				setId(UUID.randomUUID());
				setStatus(WorkStatus.IN_PROGRESS);
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
				setStatus(WorkStatus.FAILED);
				setWorkFlowExecutionId(getSampleWorkFlowExecution().getId());
				setWorkFlowTaskDefinitionId(getSampleWorkFlowTaskDefinition(TEST_TASK).getId());
			}
		};
	}

}
