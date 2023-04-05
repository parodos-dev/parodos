package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
class WorkFlowExecutionAspectTest {

	private static final String CRON_EXPRESSION = "* * * * *";

	private static final String FAILED = "FAILED";

	private static final String PROJECT_ID = "PROJECT_ID";

	private static final String WORKFLOW_DEFINITION_ID = "WORKFLOW_DEFINITION_ID";

	private static final String WORKFLOW_DEFINITION_NAME = "WORKFLOW_DEFINITION_NAME";

	private static final String WORKFLOW_EXECUTION_ID = "WORKFLOW_EXECUTION_ID";

	private static final String COMPLETED = "COMPLETED";

	private static final String TEST = "test";

	private static final String TEST_WORK_FLOW = "testWorkFlow";

	private WorkFlowServiceImpl workFlowService;

	private WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	@Mock
	private WorkFlowContinuationServiceImpl workFlowContinuationService;

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	@Mock
	private WorkFlowRepository workFlowRepository;

	@Mock
	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowExecutionAspect workFlowExecutionAspect;

	@BeforeEach
	public void initEach() {
		this.workFlowService = Mockito.mock(WorkFlowServiceImpl.class);
		this.workFlowSchedulerService = Mockito.mock(WorkFlowSchedulerServiceImpl.class);
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		WorkFlowExecutionFactory workFlowExecutionFactory = new WorkFlowExecutionFactory(workFlowService,
				workFlowRepository, workFlowSchedulerService, workFlowContinuationService);
		WorkFlow workflow = Mockito.mock(WorkFlow.class);
		WorkFlowDelegate workFlowDelegate = Mockito.mock(WorkFlowDelegate.class);
		// TODO: fix to align with main
		this.workFlowExecutionAspect = new WorkFlowExecutionAspect(this.workFlowService, this.workFlowSchedulerService,
				this.workFlowDefinitionRepository, this.workFlowRepository, this.workFlowContinuationService);
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
				put(WORKFLOW_EXECUTION_ID, UUID.randomUUID());
			}
		};

		WorkFlowDefinition workFlowDefinition = getSampleWorkFlowDefinition(TEST);
		WorkFlowExecution workFlowExecution = getSampleWorkFlowExecution();
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(Mockito.any())).thenReturn(workFlowDefinition);
		Mockito.when(this.workFlowService.saveWorkFlow(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(workFlowExecution);

		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		WorkFlow workFlow = Mockito.mock(WorkFlow.class);
		Mockito.when(proceedingJoinPoint.getTarget()).thenReturn(workFlow);
		Mockito.when(workFlow.getName()).thenReturn(TEST_WORK_FLOW);
		assertDoesNotThrow(() -> {
			Mockito.when(proceedingJoinPoint.proceed())
					.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});
		Mockito.when(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(Mockito.any(),
				Mockito.any())).thenReturn(workFlowExecution);
		Mockito.when(workFlowRepository.findById(Mockito.any())).thenReturn(Optional.of(workFlowExecution));
		Mockito.doNothing().when(workFlowContinuationService).continueWorkFlow(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
		// when
		WorkReport workReport = this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);

		// then
		assertNotNull(workReport);
		assertEquals(workReport.getStatus().toString(), COMPLETED);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_NAME), TEST_WORK_FLOW);
		assertEquals(workReport.getWorkContext().get(PROJECT_ID), projectID);
		Mockito.verify(this.workFlowSchedulerService, Mockito.times(1)).stop(Mockito.any(), Mockito.any());
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
				put(WORKFLOW_EXECUTION_ID, UUID.randomUUID());
			}
		};

		WorkFlowWorkDefinition workFlowWorkDefinition = WorkFlowWorkDefinition.builder()
				.workDefinitionId(UUID.randomUUID()).workDefinitionType(WorkType.WORKFLOW)
				.workFlowDefinition(WorkFlowDefinition.builder().build()).build();
		WorkFlowDefinition workFlowDefinition = getSampleWorkFlowDefinition(TEST);
		WorkFlowExecution workFlowExecution = getSampleWorkFlowExecution();
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(Mockito.any())).thenReturn(workFlowDefinition);
		Mockito.when(this.workFlowService.saveWorkFlow(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(workFlowExecution);
		Mockito.when(workFlowWorkRepository.findByWorkDefinitionId(Mockito.any()))
				.thenReturn(List.of(workFlowWorkDefinition));
		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		WorkFlow workFlow = Mockito.mock(WorkFlow.class);
		Mockito.when(proceedingJoinPoint.getTarget()).thenReturn(workFlow);
		Mockito.when(workFlow.getName()).thenReturn(TEST_WORK_FLOW);
		assertDoesNotThrow(() -> {
			Mockito.when(proceedingJoinPoint.proceed())
					.thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
		});
		Mockito.when(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(Mockito.any(),
				Mockito.any())).thenReturn(workFlowExecution);
		Mockito.when(workFlowRepository.findById(Mockito.any())).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkReport workReport = this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);

		// then
		assertNotNull(workReport);
		assertEquals(workReport.getStatus().toString(), FAILED);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_NAME), TEST_WORK_FLOW);
		assertNull(workReport.getWorkContext().get(WORKFLOW_DEFINITION_ID));
		assertEquals(workReport.getWorkContext().get(PROJECT_ID), projectID);
		Mockito.verify(this.workFlowSchedulerService, Mockito.times(1)).schedule(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
		Mockito.verify(this.workFlowService, Mockito.times(1))
				.updateWorkFlow(Mockito.argThat(w -> w.getStatus().toString().equals(FAILED)));
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
		WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = WorkFlowCheckerMappingDefinition.builder()
				.cronExpression(CRON_EXPRESSION).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().type(WorkFlowType.CHECKER)
				.checkerWorkFlowDefinition(workFlowCheckerMappingDefinition).name(name).build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

}