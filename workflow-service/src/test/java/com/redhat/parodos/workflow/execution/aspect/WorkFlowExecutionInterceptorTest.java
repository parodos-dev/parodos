package com.redhat.parodos.workflow.execution.aspect;

import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class WorkFlowExecutionInterceptorTest {

	private WorkFlowExecutionInterceptor interceptor;

	@Mock
	private WorkFlowServiceImpl workFlowService;

	@Mock
	private WorkFlowRepository workFlowRepository;

	@Mock
	private WorkContext workContext;

	@Mock
	private WorkFlowDefinition workFlowDefinition;

	@Mock
	private WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	@Mock
	private WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	private WorkFlowExecution expectedWorkFlowExecution;

	@Mock
	private WorkFlowExecution mainWorkFlowExecution;

	@BeforeEach
	public void setUp() {
		expectedWorkFlowExecution = new WorkFlowExecution();
		expectedWorkFlowExecution.setProjectId(UUID.randomUUID());
		interceptor = new WorkFlowExecutionInterceptor(workFlowDefinition, workContext, workFlowService,
				workFlowRepository, workFlowSchedulerService, workFlowContinuationServiceImpl) {
			@Override
			protected WorkFlowExecution doPreWorkFlowExecution() {
				return expectedWorkFlowExecution;
			}

			@Override
			protected WorkFlowExecution getMainWorkFlowExecution() {
				return mainWorkFlowExecution;
			}
		};
	}

	@Test
	public void testHandlePreWorkFlowExecution() {
		WorkFlowExecution result = interceptor.handlePreWorkFlowExecution();
		assertEquals(expectedWorkFlowExecution, result);
	}

	@Test
	public void testHandleIncompletePostWorkFlowExecution() {
		// given
		WorkReport report = Mockito.mock(WorkReport.class);
		when(report.getStatus()).thenReturn(WorkStatus.IN_PROGRESS);

		WorkFlow workFlow = Mockito.mock(WorkFlow.class);
		when(workFlow.getName()).thenReturn("TestWorkFlow");

		when(workFlowDefinition.getType()).thenReturn(WorkFlowType.CHECKER);
		when(workFlowDefinition.getCheckerWorkFlowDefinition())
				.thenReturn(Mockito.mock(WorkFlowCheckerMappingDefinition.class));

		// when
		WorkFlowExecution workFlowExecution = interceptor.handlePreWorkFlowExecution();
		assertNotNull(workFlowExecution);
		WorkReport result = interceptor.handlePostWorkFlowExecution(report, workFlow);

		// then
		verify(workFlowService, Mockito.times(0)).saveWorkFlow(any(UUID.class), any(UUID.class),
				eq(WorkFlowStatus.IN_PROGRESS), any(), anyString());
		verify(workFlowSchedulerService, Mockito.times(1)).schedule(Mockito.any(), Mockito.any(),
				Mockito.any(WorkContext.class), Mockito.any());

		assertEquals(result.getStatus(), report.getStatus());
	}

	@Test
	public void testHandleCompletePostWorkFlowExecution() {
		// given
		WorkReport report = Mockito.mock(WorkReport.class);
		when(report.getStatus()).thenReturn(WorkStatus.COMPLETED);
		when(workContext.get(WorkContextDelegate.buildKey(WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME))).thenReturn("TestWorkFlow");
		WorkFlow workFlow = Mockito.mock(WorkFlow.class);
		when(workFlow.getName()).thenReturn("TestWorkFlow");

		when(workFlowDefinition.getType()).thenReturn(WorkFlowType.CHECKER);
		when(workFlowDefinition.getCheckerWorkFlowDefinition())
				.thenReturn(Mockito.mock(WorkFlowCheckerMappingDefinition.class));
		when(mainWorkFlowExecution.getId()).thenReturn(UUID.randomUUID());

		// when
		WorkFlowExecution workFlowExecution = interceptor.handlePreWorkFlowExecution();
		assertNotNull(workFlowExecution);
		WorkReport result = interceptor.handlePostWorkFlowExecution(report, workFlow);

		// then
		verify(workFlowService, Mockito.times(0)).saveWorkFlow(any(UUID.class), any(UUID.class),
				eq(WorkFlowStatus.IN_PROGRESS), any(), anyString());
		verify(workFlowSchedulerService, Mockito.times(1)).stop(Mockito.any(), Mockito.any(WorkFlow.class));
		verify(workFlowContinuationServiceImpl, Mockito.times(1)).continueWorkFlow(Mockito.any(UUID.class),
				Mockito.anyString(), Mockito.any(WorkContext.class), Mockito.any(UUID.class));
		assertEquals(result.getStatus(), report.getStatus());
	}

}
