package com.redhat.parodos.workflow.execution.aspect;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowExecutor.ExecutionContext;
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

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		this.workFlowService = mock(WorkFlowServiceImpl.class);
		this.workFlowSchedulerService = mock(WorkFlowSchedulerServiceImpl.class);
		this.workFlowDefinitionRepository = mock(WorkFlowDefinitionRepository.class);
		WorkFlowExecutionFactory workFlowExecutionFactory = new WorkFlowExecutionFactory(workFlowService,
				workFlowRepository, workFlowSchedulerService, workFlowContinuationService);
		WorkFlow workflow = mock(WorkFlow.class);
		WorkFlowDelegate workFlowDelegate = mock(WorkFlowDelegate.class);
		this.workFlowExecutionAspect = new WorkFlowExecutionAspect(this.workFlowSchedulerService,
				this.workFlowDefinitionRepository, workFlowExecutionFactory);
		when(workFlowDelegate.getWorkFlowByName(any())).thenReturn(mock(WorkFlow.class));
		when(workflow.getName()).thenReturn(TEST);
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
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(workFlowDefinition);
		when(this.workFlowService.saveWorkFlow(any(), any(), any(), any(), any(), any())).thenReturn(workFlowExecution);

		ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		WorkFlow workFlow = mock(WorkFlow.class);
		when(proceedingJoinPoint.getTarget()).thenReturn(workFlow);
		when(workFlow.getName()).thenReturn(TEST_WORK_FLOW);
		assertDoesNotThrow(() -> {
			when(proceedingJoinPoint.proceed()).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		});
		when(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(any(), any()))
				.thenReturn(workFlowExecution);
		when(workFlowRepository.findById(any())).thenReturn(Optional.of(workFlowExecution));
		doNothing().when(workFlowContinuationService).continueWorkFlow(any(ExecutionContext.class));
		// when
		WorkReport workReport = this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);

		// then
		assertNotNull(workReport);
		assertEquals(workReport.getStatus().toString(), COMPLETED);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_NAME), TEST_WORK_FLOW);
		assertEquals(workReport.getWorkContext().get(PROJECT_ID), projectID);
		assertNull(workReport.getError());
		verify(this.workFlowSchedulerService, times(1)).stop(any(), any(), any());
		verify(this.workFlowService, times(1)).updateWorkFlow(argThat(w -> w.getStatus().toString().equals(COMPLETED)));
	}

	@Test
	void ExecuteAroundAdviceWithInProgressWorkFlowTestWithoutWorkFlowDefinition() {
		// given
		WorkContext workContext = new WorkContext();
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(null);
		ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		WorkFlow workFlow = mock(WorkFlow.class);
		when(proceedingJoinPoint.getTarget()).thenReturn(workFlow);
		when(workFlow.getName()).thenReturn(TEST_WORK_FLOW);

		// when
		WorkReport workReport = this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);

		// then
		assertNotNull(workReport);
		assertEquals(workReport.getStatus().toString(), FAILED);
		assertNotNull(workReport.getError());
		assertThat(workReport.getError(), is(instanceOf(ResourceNotFoundException.class)));
		// To validate that is workflow definition with type Name and the correct name
		assertThat(workReport.getError().getMessage(),
				containsString("Workflow definition with Name: testWorkFlow not found"));
		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(TEST_WORK_FLOW);
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
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(workFlowDefinition);
		when(this.workFlowService.saveWorkFlow(any(), any(), any(), any(), any(), any())).thenReturn(workFlowExecution);
		when(workFlowWorkRepository.findFirstByWorkDefinitionId(any())).thenReturn(workFlowWorkDefinition);
		ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
		WorkFlow workFlow = mock(WorkFlow.class);
		when(proceedingJoinPoint.getTarget()).thenReturn(workFlow);
		when(workFlow.getName()).thenReturn(TEST_WORK_FLOW);
		assertDoesNotThrow(() -> {
			when(proceedingJoinPoint.proceed()).thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
		});
		when(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(any(), any()))
				.thenReturn(workFlowExecution);
		when(workFlowRepository.findById(any())).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkReport workReport = this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);

		// then
		assertNotNull(workReport);
		assertEquals(workReport.getStatus().toString(), FAILED);
		assertEquals(workReport.getWorkContext().get(WORKFLOW_DEFINITION_NAME), TEST_WORK_FLOW);
		assertNull(workReport.getWorkContext().get(WORKFLOW_DEFINITION_ID));
		assertEquals(workReport.getWorkContext().get(PROJECT_ID), projectID);
		verify(this.workFlowSchedulerService, times(1)).schedule(any(), any(), any(), any(), any());
		verify(this.workFlowService, times(1)).updateWorkFlow(argThat(w -> w.getStatus().toString().equals(FAILED)));
	}

	static WorkFlowExecution getSampleWorkFlowExecution() {
		User user = User.builder().build();
		user.setId(UUID.randomUUID());
		return new WorkFlowExecution() {
			{
				setId(UUID.randomUUID());
				setWorkFlowDefinition(WorkFlowDefinition.builder().build());
				setStatus(WorkStatus.IN_PROGRESS);
				setProjectId(UUID.randomUUID());
				setUser(user);
			}
		};
	}

	static WorkFlowDefinition getSampleWorkFlowDefinition(String name) {
		WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = WorkFlowCheckerMappingDefinition.builder()
				.cronExpression(CRON_EXPRESSION).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().type(WorkFlowType.CHECKER)
				.checkerWorkFlowDefinition(workFlowCheckerMappingDefinition).name(name).build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

}
