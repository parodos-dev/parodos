package com.redhat.parodos.workflow.execution.aspect;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;

import org.springframework.stereotype.Service;

@Service
public class WorkFlowExecutionFactory {

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	public WorkFlowExecutionFactory(WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		this.workFlowService = workFlowService;
		this.workFlowRepository = workFlowRepository;
		this.workFlowSchedulerService = workFlowSchedulerService;
		this.workFlowContinuationServiceImpl = workFlowContinuationServiceImpl;
	}

	public WorkFlowExecutionInterceptor createExecutionHandler(WorkFlowDefinition definition, WorkContext workContext) {
		// get main WorkFlowExecution, this is the first time execution for main
		// workflow if return null
		UUID mainWorkFlowExecutionId = getMainWorkFlowExecutionId(workContext);
		if (mainWorkFlowExecutionId == null) {
			return new InitialMainWorkflowInterceptor(definition, workContext, workFlowService, workFlowRepository,
					workFlowSchedulerService, workFlowContinuationServiceImpl);
		}

		if (isMainWorkFlow(definition, workContext)) {
			return new MainWorkFlowExecutionInterceptor(definition, workContext, workFlowService, workFlowRepository,
					workFlowSchedulerService, workFlowContinuationServiceImpl);
		}
		else {
			return new ContinuedWorkFlowExecutionInterceptor(definition, workContext, workFlowService,
					workFlowRepository, workFlowSchedulerService, workFlowContinuationServiceImpl);
		}
	}

	static boolean isMainWorkFlow(WorkFlowDefinition workflow, WorkContext workContext) {
		String mainWorkflowName = WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION, WorkContextDelegate.Resource.NAME).toString();
		return workflow.getName().equals(mainWorkflowName);
	}

	static UUID getMainWorkFlowExecutionId(WorkContext workContext) {
		return Optional.ofNullable(WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID))
				.map(id -> UUID.fromString(id.toString())).orElse(null);
	}

}
