package com.redhat.parodos.workflow.execution.service;

import java.util.Map;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "unchecked", "rawtypes" })
class ContextArgumentsMergerTest {

	@Test
	void mergeArguments_shouldMergeArgumentsCorrectly() {
		// given
		// prepare source context
		WorkFlowExecution sourceWorkFlow = mock(WorkFlowExecution.class);
		when(sourceWorkFlow.getArguments()).thenReturn("{\"key1\":\"value1\"}");
		WorkFlowExecutionContext sourceWorkFlowExecutionContext = mock(WorkFlowExecutionContext.class);
		when(sourceWorkFlow.getWorkFlowExecutionContext()).thenReturn(sourceWorkFlowExecutionContext);
		WorkContext sourceWorkContext = mock(WorkContext.class);
		when(sourceWorkFlowExecutionContext.getWorkContext()).thenReturn(sourceWorkContext);
		when(sourceWorkContext.get("WORKFLOW_EXECUTION_ARGUMENTS"))
				.thenReturn(Map.of("key1", "valueX", "key2", "value2"));

		// prepare target context
		WorkContext targetContext = new WorkContext();
		targetContext.put("WORKFLOW_EXECUTION_ARGUMENTS", Map.of("key1", "valueY"));

		// when
		ContextArgumentsMerger.mergeArguments(sourceWorkFlow, targetContext);

		// then
		Object workflowExecutionArguments = targetContext.getContext().get("WORKFLOW_EXECUTION_ARGUMENTS");
		assertThat(workflowExecutionArguments, is(instanceOf(Map.class)));
		Map<String, String> workflowExecutionArgumentsMap;
		workflowExecutionArgumentsMap = (Map) workflowExecutionArguments;
		assertThat(workflowExecutionArgumentsMap, allOf(hasEntry("key1", "valueY"), hasEntry("key2", "value2")));
	}

	@Test
	void mergeArguments_noArgumentsInSourceContext() {
		// given
		// prepare source context
		WorkFlowExecution sourceWorkFlow = mock(WorkFlowExecution.class);
		when(sourceWorkFlow.getArguments()).thenReturn(null);
		WorkFlowExecutionContext sourceWorkFlowExecutionContext = mock(WorkFlowExecutionContext.class);
		when(sourceWorkFlow.getWorkFlowExecutionContext()).thenReturn(sourceWorkFlowExecutionContext);
		WorkContext sourceWorkContext = mock(WorkContext.class);
		when(sourceWorkFlowExecutionContext.getWorkContext()).thenReturn(sourceWorkContext);
		when(sourceWorkContext.get("WORKFLOW_EXECUTION_ARGUMENTS")).thenReturn(null);

		// prepare target context
		WorkContext targetContext = new WorkContext();
		targetContext.put("WORKFLOW_EXECUTION_ARGUMENTS", Map.of("key1", "value1"));

		// when
		ContextArgumentsMerger.mergeArguments(sourceWorkFlow, targetContext);

		// then
		Object workflowExecutionArguments = targetContext.getContext().get("WORKFLOW_EXECUTION_ARGUMENTS");
		assertThat(workflowExecutionArguments, is(instanceOf(Map.class)));
		Map<String, String> workflowExecutionArgumentsMap;
		workflowExecutionArgumentsMap = (Map) workflowExecutionArguments;
		assertThat(workflowExecutionArgumentsMap, allOf(hasEntry("key1", "value1")));
	}

}
