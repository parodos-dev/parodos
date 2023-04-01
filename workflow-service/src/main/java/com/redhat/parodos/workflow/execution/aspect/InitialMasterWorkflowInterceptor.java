package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;

public class InitialMasterWorkflowInterceptor extends WorkFlowExecutionInterceptor {

	public InitialMasterWorkflowInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		super(workFlowDefinition, workContext, workFlowService, workFlowRepository, workFlowSchedulerService,
				workFlowContinuationServiceImpl);
	}

	@Override
	protected WorkFlowExecution doPreWorkFlowExecution() {
		/*
		 * if this is the first time execution for master workflow, persist it and write
		 * its execution id to workContext
		 */
		WorkFlowExecution workFlowExecution = saveWorkFlow(null);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, workFlowExecution.getId());
		return workFlowExecution;
	}

}
