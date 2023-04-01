package com.redhat.parodos.workflow.execution.aspect;

import java.util.Optional;

import static com.redhat.parodos.workflow.execution.aspect.WorkFlowExecutionFactory.getMasterWorkFlowExecutionId;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.exceptions.WorkflowExecutionNotFoundException;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;

public class ContinuedWorkFlowExecutionInterceptor extends WorkFlowExecutionInterceptor {

	private WorkFlowExecution masterWorkFlowExecution;

	public ContinuedWorkFlowExecutionInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		super(workFlowDefinition, workContext, workFlowService, workFlowRepository, workFlowSchedulerService,
				workFlowContinuationServiceImpl);
	}

	@Override
	protected WorkFlowExecution doPreWorkFlowExecution() {
		this.masterWorkFlowExecution = workFlowRepository.findById(getMasterWorkFlowExecutionId(workContext))
				.orElseThrow(() -> new WorkflowExecutionNotFoundException(
						"masterWorkFlow not found for sub-workflow: " + workFlowDefinition.getName()));

		// get the workflow execution if this is triggered by continuation service
		return Optional
				.ofNullable(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(
						workFlowDefinition.getId(), masterWorkFlowExecution))
				.orElseGet(() -> this.saveWorkFlow(masterWorkFlowExecution));
	}

	protected WorkFlowExecution getMasterWorkFlowExecution() {
		return masterWorkFlowExecution;
	}

}
