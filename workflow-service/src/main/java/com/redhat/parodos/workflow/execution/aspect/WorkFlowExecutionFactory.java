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
		// get master WorkFlowExecution, this is the first time execution for master
		// workflow if return null
		UUID masterWorkFlowExecutionId = getMasterWorkFlowExecutionId(workContext);
		if (masterWorkFlowExecutionId == null) {
			return new InitialMasterWorkflowInterceptor(definition, workContext, workFlowService, workFlowRepository,
					workFlowSchedulerService, workFlowContinuationServiceImpl);
		}

		if (isMasterWorkFlow(definition, workContext)) {
			return new MasterWorkFlowExecutionInterceptor(definition, workContext, workFlowService, workFlowRepository,
					workFlowSchedulerService, workFlowContinuationServiceImpl);
		}
		else {
			return new ContinuedWorkFlowExecutionInterceptor(definition, workContext, workFlowService,
					workFlowRepository, workFlowSchedulerService, workFlowContinuationServiceImpl);
		}
	}

	static boolean isMasterWorkFlow(WorkFlowDefinition workflow, WorkContext workContext) {
		String masterWorkflowName = WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION, WorkContextDelegate.Resource.NAME).toString();
		return workflow.getName().equals(masterWorkflowName);
	}

	static UUID getMasterWorkFlowExecutionId(WorkContext workContext) {
		return Optional.ofNullable(WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID))
				.map(id -> UUID.fromString(id.toString())).orElse(null);
	}

}
