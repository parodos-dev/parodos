package com.redhat.parodos.workflow.execution.aspect;

import java.util.Optional;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.exceptions.WorkflowExecutionNotFoundException;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;

public class ContinuedWorkFlowExecutionInterceptor extends WorkFlowExecutionInterceptor {

	private WorkFlowExecution mainWorkFlowExecution;

	public ContinuedWorkFlowExecutionInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		super(workFlowDefinition, workContext, workFlowService, workFlowRepository, workFlowSchedulerService,
				workFlowContinuationServiceImpl);
	}

	@Override
	protected WorkFlowExecution doPreWorkFlowExecution() {
		this.mainWorkFlowExecution = workFlowRepository
				.findById(WorkFlowExecutionFactory.getMainWorkFlowExecutionId(workContext))
				.orElseThrow(() -> new WorkflowExecutionNotFoundException(
						"mainWorkFlow not found for sub-workflow: " + workFlowDefinition.getName()));

		// get the workflow execution if this is triggered by continuation service
		return Optional
				.ofNullable(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
						workFlowDefinition.getId(), mainWorkFlowExecution))
				.orElseGet(() -> this.saveWorkFlow(mainWorkFlowExecution));
	}

	protected WorkFlowExecution getMainWorkFlowExecution() {
		return mainWorkFlowExecution;
	}

}
