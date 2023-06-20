package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.exceptions.WorkflowExecutionNotFoundException;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;

public class RollbackWorkFlowExecutionInterceptor extends WorkFlowExecutionInterceptor {

	public RollbackWorkFlowExecutionInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		super(workFlowDefinition, workContext, workFlowService, workFlowRepository, workFlowSchedulerService,
				workFlowContinuationServiceImpl);
	}

	@Override
	protected WorkFlowExecution doPreWorkFlowExecution() {
		var mainWorkFlowExecution = workFlowRepository
				.findById(WorkFlowExecutionFactory.getMainWorkFlowExecutionId(workContext))
				.orElseThrow(() -> new WorkflowExecutionNotFoundException(
						"mainWorkFlow not found for rollback workflow: " + workFlowDefinition.getName()));

		// get the workflow execution if this is triggered by continuation service
		WorkFlowExecution rollbackWorkFlowExecution = saveWorkFlow(mainWorkFlowExecution);
		WorkContextUtils.setRollbackWorkFlowId(workContext, rollbackWorkFlowExecution.getId());
		WorkContextUtils.setRollbackWorkFlowId(mainWorkFlowExecution.getWorkFlowExecutionContext().getWorkContext(),
				rollbackWorkFlowExecution.getId());
		workFlowRepository.save(mainWorkFlowExecution);
		return rollbackWorkFlowExecution;
	}

}
