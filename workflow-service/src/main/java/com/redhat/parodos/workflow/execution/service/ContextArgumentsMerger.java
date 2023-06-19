package com.redhat.parodos.workflow.execution.service;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.WorkContext;

public abstract class ContextArgumentsMerger {

	private ContextArgumentsMerger() {
	}

	/**
	 * Merge arguments from source workflow to target context by the following precedence:
	 * <ol>
	 * <li>Target workflow arguments</li>
	 * <li>Source workflow arguments from its context by key
	 * WORKFLOW_EXECUTION_ARGUMENTS</li>
	 * <li>Source workflow arguments</li>
	 * </ol>
	 * @param sourceWorkFlow source workflow
	 * @param targetContext target context
	 */
	public static void mergeArguments(WorkFlowExecution sourceWorkFlow, WorkContext targetContext) {
		Map<String, String> mergedArgs = new HashMap<>();
		mergeSourceWorkFlowArguments(sourceWorkFlow, mergedArgs);
		mergeSourceWorkFlowContextArguments(sourceWorkFlow, mergedArgs);
		mergeTargetContextArguments(targetContext, mergedArgs);

		// write merged arguments to target WorkContext
		WorkContextDelegate.write(targetContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ARGUMENTS, mergedArgs);
	}

	private static void mergeSourceWorkFlowArguments(WorkFlowExecution sourceWorkflow, Map<String, String> mergedArgs) {
		if (sourceWorkflow.getArguments() == null) {
			return;
		}

		Map<String, String> sourceArgs = WorkFlowDTOUtil.readStringAsObject(sourceWorkflow.getArguments(),
				new TypeReference<HashMap<String, String>>() {
				}, null);
		mergeArguments(mergedArgs, sourceArgs);
	}

	private static void mergeSourceWorkFlowContextArguments(WorkFlowExecution sourceWorkflow,
			Map<String, String> mergedArgs) {
		Map<String, String> sourceContextArgs = convertArguments(
				WorkContextDelegate.read(sourceWorkflow.getWorkFlowExecutionContext().getWorkContext(),
						WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ARGUMENTS));
		mergeArguments(mergedArgs, sourceContextArgs);
	}

	private static void mergeTargetContextArguments(WorkContext targetContext, Map<String, String> mergedArgs) {
		Map<String, String> targetContextArgs = convertArguments(WorkContextDelegate.read(targetContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ARGUMENTS));
		mergeArguments(mergedArgs, targetContextArgs);
	}

	private static void mergeArguments(Map<String, String> mergedArgs, Map<String, String> arguments) {
		if (arguments != null) {
			mergedArgs.putAll(arguments);
		}
	}

	private static HashMap<String, String> convertArguments(Object arguments) {
		return new ObjectMapper().convertValue(arguments, new TypeReference<>() {
		});
	}

}
