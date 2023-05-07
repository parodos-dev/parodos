package com.redhat.parodos.workflow.execution.aspect;

import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WorkFlowExecutionFactoryTest {

	private WorkFlowExecutionFactory factory;

	@Mock
	private WorkFlowServiceImpl workFlowService;

	@Mock
	private WorkFlowRepository workFlowRepository;

	@Mock
	private WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	@Mock
	private WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	@BeforeEach
	void setUp() {
		factory = new WorkFlowExecutionFactory(workFlowService, workFlowRepository, workFlowSchedulerService,
				workFlowContinuationServiceImpl);
	}

	@Test
	void testCreateMainWorkFlowExecutionInterceptor() {
		// given
		WorkFlowDefinition workflow = mock(WorkFlowDefinition.class);
		when(workflow.getName()).thenReturn("TestWorkflow");

		WorkContext workContext = mock(WorkContext.class);
		when(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME)).thenReturn("TestWorkflow");
		when(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID)).thenReturn(UUID.randomUUID().toString());

		// when
		WorkFlowExecutionInterceptor executionHandler = factory.createExecutionHandler(workflow, workContext);

		// then
		// verify that the correct interceptor is created
		assertNotNull(executionHandler);
		assertTrue(executionHandler instanceof MainWorkFlowExecutionInterceptor);
	}

	@Test
	void testCreateInitialMainWorkFlowExecutionInterceptor() {
		// given
		WorkFlowDefinition workflow = mock(WorkFlowDefinition.class);
		when(workflow.getName()).thenReturn("TestWorkflow");

		WorkContext workContext = mock(WorkContext.class);
		when(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID)).thenReturn(null);

		// when
		WorkFlowExecutionInterceptor executionHandler = factory.createExecutionHandler(workflow, workContext);

		// then
		// verify that the correct interceptor is created
		assertNotNull(executionHandler);
		assertTrue(executionHandler instanceof InitialMainWorkflowInterceptor);
	}

	@Test
	void testCreateContinuedWorkFlowExecutionInterceptor() {
		// given
		WorkFlowDefinition workflow = mock(WorkFlowDefinition.class);
		when(workflow.getName()).thenReturn("TestWorkflow");

		WorkContext workContext = mock(WorkContext.class);
		when(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID)).thenReturn(UUID.randomUUID().toString());
		when(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME)).thenReturn("OtherTestWorkflow");

		// when
		WorkFlowExecutionInterceptor executionHandler = factory.createExecutionHandler(workflow, workContext);

		// then
		// verify that the correct interceptor is created
		assertNotNull(executionHandler);
		assertTrue(executionHandler instanceof ContinuedWorkFlowExecutionInterceptor);
	}

	@Test
	void testIsMainWorkFlow() {
		// when
		WorkFlowDefinition workflow = mock(WorkFlowDefinition.class);
		when(workflow.getName()).thenReturn("TestWorkflow");

		WorkContext workContext = mock(WorkContext.class);
		when(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME)).thenReturn("TestWorkflow");

		// given
		boolean isMain = WorkFlowExecutionFactory.isMainWorkFlow(workflow, workContext);

		// then
		assertTrue(isMain);
	}

	@Test
	void testGetMainWorkFlowExecutionId() {
		// when
		WorkContext workContext = mock(WorkContext.class);
		String expectedExecutionId = UUID.randomUUID().toString();
		when(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID)).thenReturn(expectedExecutionId);

		// given
		UUID mainId = WorkFlowExecutionFactory.getMainWorkFlowExecutionId(workContext);

		// then
		assertNotNull(mainId);
		assertEquals(expectedExecutionId, mainId.toString());
	}

}
