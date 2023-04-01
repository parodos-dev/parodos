package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckerWorkFlowPostInterceptor implements WorkFlowPostInterceptor {

	private final WorkFlowDefinition workFlowDefinition;

	private final WorkContext workContext;

	private final WorkFlowExecution workFlowExecution;

	private final WorkFlowExecution masterWorkFlowExecution;

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	private final WorkFlow workFlow;

	private final WorkStatus workStatus;

	public CheckerWorkFlowPostInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl, WorkFlowExecution workFlowExecution,
			WorkFlowExecution masterWorkFlowExecution, WorkFlow workFlow, WorkStatus workStatus) {
		this.workFlowDefinition = workFlowDefinition;
		this.workContext = workContext;
		this.workFlowService = workFlowService;
		this.workFlowExecution = workFlowExecution;
		this.masterWorkFlowExecution = masterWorkFlowExecution;
		this.workFlowSchedulerService = workFlowSchedulerService;
		this.workFlowContinuationServiceImpl = workFlowContinuationServiceImpl;
		this.workFlow = workFlow;
		this.workStatus = workStatus;
	}

	@Override
	public WorkReport handlePostWorkFlowExecution() {
		workFlowService.updateWorkFlow(workFlowExecution);
		/*
		 * if this workflow is a checker, schedule workflow checker for dynamic run on
		 * cron expression or stop if done
		 */
		startOrStopWorkFlowCheckerOnSchedule(workFlow, workFlowDefinition.getCheckerWorkFlowDefinition(), workStatus,
				workContext, workFlowExecution.getProjectId().toString(), masterWorkFlowExecution);
		return null;
	}

	private void startOrStopWorkFlowCheckerOnSchedule(WorkFlow workFlow,
			WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition, WorkStatus workStatus,
			WorkContext workContext, String projectId, WorkFlowExecution masterWorkFlowExecution) {
		if (workStatus != WorkStatus.COMPLETED) {
			log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlow.getName(),
					workFlowCheckerMappingDefinition.getCronExpression());
			workFlowSchedulerService.schedule(workFlow, workContext,
					workFlowCheckerMappingDefinition.getCronExpression());
			return;
		}

		log.info("Stop workflow checker: {} schedule", workFlow.getName());
		workFlowSchedulerService.stop(workFlow);

		String masterWorkFlowName = WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION, WorkContextDelegate.Resource.NAME).toString();
		/*
		 * if this workflow is checker, and it's successful, call continuation service to
		 * restart master workflow execution with same execution ID
		 */
		workFlowContinuationServiceImpl.continueWorkFlow(projectId, masterWorkFlowName, workContext,
				masterWorkFlowExecution.getId());
	}

}
