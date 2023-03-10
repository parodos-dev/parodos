package com.redhat.parodos.workflow.execution.aspect;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;

class WorkFlowExecutionAspectTest {

	private static final String CRON_EXPRESSION = "* * * * *";

	private static final String FAILED = "FAILED";

	private static final String PROJECT_ID = "PROJECT_ID";

	private static final String WORKFLOW_DEFINITION_ID = "WORKFLOW_DEFINITION_ID";

	private static final String WORKFLOW_DEFINITION_NAME = "WORKFLOW_DEFINITION_NAME";

	private static final String COMPLETED = "COMPLETED";

	private static final String TEST = "test";

	private static final String TEST_WORK_FLOW = "testWorkFlow";

	private WorkFlowServiceImpl workFlowService;

	private WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowExecutionAspect workFlowExecutionAspect; // private WorkFlow workFlow;

	@BeforeEach
	public void initEach() {
		this.workFlowService = Mockito.mock(WorkFlowServiceImpl.class);
		this.workFlowSchedulerService = Mockito.mock(WorkFlowSchedulerServiceImpl.class);
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		WorkFlow workflow = Mockito.mock(WorkFlow.class);
		WorkFlowDelegate workFlowDelegate = Mockito.mock(WorkFlowDelegate.class);
		this.workFlowExecutionAspect = new WorkFlowExecutionAspect(this.workFlowService, this.workFlowSchedulerService,
				this.workFlowDefinitionRepository, workFlowDelegate);
		Mockito.when(workFlowDelegate.getWorkFlowExecutionByName(Mockito.any()))
				.thenReturn(Mockito.mock(WorkFlow.class));
		Mockito.when(workflow.getName()).thenReturn(TEST);
	}

	@Test
	public void ExecuteAroundAdviceWithValidDataTest() {

		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = new WorkContext() {
			{
				put(WORKFLOW_DEFINITION_NAME, TEST_WORK_FLOW);
				put(PROJECT_ID, projectID);
			}
		};

		WorkFlowDefinition workFlowDefinition = getSampleWorkFlowDefinition(TEST);
		Mockito.when(this.workFlowDefinitionRepository.findByName(Mockito.any()))
				.thenReturn(List.of(workFlowDefinition));
		Mockito.when(this.workFlowService.saveWorkFlow(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(getSampleWorkFlowExecution());

		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(proceedingJoinPoint.getTarget()).thenReturn(Mockito.mock(WorkFlow.class));
		assertDoesNotThrow(() -> {
			Mockito.when(proceedingJoinPoint.proceed())
					.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});

		// when
		WorkReport workReport = this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);

		// then
		assertNotNull(workReport);
		assertEquals(workReport.getStatus().toString(), COMPLETED);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_NAME), TEST_WORK_FLOW);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_ID), workFlowDefinition.getId().toString());
		assertEquals(workReport.getWorkContext().get(PROJECT_ID), projectID);
		Mockito.verify(this.workFlowSchedulerService, Mockito.times(1)).stop(Mockito.any());
		Mockito.verify(this.workFlowService, Mockito.times(1)).saveWorkFlow(Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(this.workFlowService, Mockito.times(1))
				.updateWorkFlow(Mockito.argThat(w -> w.getStatus().toString().equals(COMPLETED)));
	}

	@Test
	void ExecuteAroundAdviceWithInProgressWorkFlowTest() {
		// given
		UUID projectID = UUID.randomUUID();
		WorkContext workContext = new WorkContext() {
			{
				put(WORKFLOW_DEFINITION_NAME, TEST_WORK_FLOW);
				put(PROJECT_ID, projectID);
			}
		};

		WorkFlowDefinition workFlowDefinition = getSampleWorkFlowDefinition(TEST);

		Mockito.when(this.workFlowDefinitionRepository.findByName(Mockito.any()))
				.thenReturn(List.of(workFlowDefinition));
		Mockito.when(this.workFlowService.saveWorkFlow(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(getSampleWorkFlowExecution());

		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(proceedingJoinPoint.getTarget()).thenReturn(Mockito.mock(WorkFlow.class));
		assertDoesNotThrow(() -> {
			Mockito.when(proceedingJoinPoint.proceed())
					.thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
		});

		// when
		WorkReport workReport = this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);

		// then
		assertNotNull(workReport);
		assertEquals(workReport.getStatus().toString(), FAILED);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_NAME), TEST_WORK_FLOW);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_ID), workFlowDefinition.getId().toString());
		assertEquals(workReport.getWorkContext().get(PROJECT_ID), projectID);
		Mockito.verify(this.workFlowSchedulerService, Mockito.times(1)).schedule(Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(this.workFlowService, Mockito.times(1)).saveWorkFlow(Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.verify(this.workFlowService, Mockito.times(1))
				.updateWorkFlow(Mockito.argThat(w -> w.getStatus().toString().equals(FAILED)));
	}

	WorkFlowExecution getSampleWorkFlowExecution() {
		return new WorkFlowExecution() {
			{
				setId(UUID.randomUUID());
			}
		};
	}

	WorkFlowDefinition getSampleWorkFlowDefinition(String name) {
		WorkFlowCheckerDefinition workFlowCheckerDefinition = WorkFlowCheckerDefinition.builder()
				.cronExpression(CRON_EXPRESSION).nextWorkFlow(Mockito.mock(WorkFlowDefinition.class)).build();

		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().type(WorkFlowType.CHECKER.toString())
				.checkerWorkFlowDefinition(workFlowCheckerDefinition).name(name).build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

}