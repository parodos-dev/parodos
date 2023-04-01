package com.redhat.parodos.workflow.execution.aspect;

import static com.redhat.parodos.workflow.execution.aspect.WorkFlowExecutionFactory.getMasterWorkFlowExecutionId;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.exceptions.WorkflowExecutionNotFoundException;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;

public class MasterWorkFlowExecutionInterceptor extends WorkFlowExecutionInterceptor {

	private WorkFlowExecution masterWorkFlowExecution;

	public MasterWorkFlowExecutionInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		super(workFlowDefinition, workContext, workFlowService, workFlowRepository, workFlowSchedulerService,
				workFlowContinuationServiceImpl);
	}

	@Override
	protected WorkFlowExecution doPreWorkFlowExecution() {
		masterWorkFlowExecution = workFlowRepository.findById(getMasterWorkFlowExecutionId(workContext))
				.orElseThrow(() -> new WorkflowExecutionNotFoundException(
						"masterWorkFlow not found for sub-workflow: " + workFlowDefinition.getName()));
		return masterWorkFlowExecution;
	}

	@Override
	protected WorkFlowExecution getMasterWorkFlowExecution() {
		return masterWorkFlowExecution;
	}

}
