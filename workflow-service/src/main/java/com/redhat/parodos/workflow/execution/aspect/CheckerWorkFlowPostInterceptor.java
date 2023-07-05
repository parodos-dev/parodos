package com.redhat.parodos.workflow.execution.aspect;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowExecutor.ExecutionContext;
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

	private final WorkFlowExecution mainWorkFlowExecution;

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	private final WorkFlow workFlow;

	private final WorkStatus workStatus;

	public CheckerWorkFlowPostInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl, WorkFlowExecution workFlowExecution,
			WorkFlowExecution mainWorkFlowExecution, WorkFlow workFlow, WorkStatus workStatus) {
		this.workFlowDefinition = workFlowDefinition;
		this.workContext = workContext;
		this.workFlowService = workFlowService;
		this.workFlowExecution = workFlowExecution;
		this.mainWorkFlowExecution = mainWorkFlowExecution;
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
				workContext, workFlowExecution.getProjectId(), workFlowExecution.getUser().getId(),
				mainWorkFlowExecution);
		return null;
	}

	private void startOrStopWorkFlowCheckerOnSchedule(WorkFlow workFlow,
			WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition, WorkStatus workStatus,
			WorkContext workContext, UUID projectId, UUID userId, WorkFlowExecution mainWorkFlowExecution) {
		/*
		 * if this workflow is a checker, schedule workflow checker for dynamic run on
		 * cron expression or stop if done
		 */
		if (workStatus != WorkStatus.COMPLETED) {
			/*
			 * decide if checker-workflow is rejected by filtering rejected checker-task
			 */
			if (workStatus != WorkStatus.REJECTED) {
				log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlow.getName(),
						workFlowCheckerMappingDefinition.getCronExpression());
				workFlowSchedulerService.schedule(projectId, userId, workFlow, workContext,
						workFlowCheckerMappingDefinition.getCronExpression());

			}
			else {
				log.info("Stop rejected workflow checker: {} schedule", workFlow.getName());
				workFlowSchedulerService.stop(projectId, userId, workFlow);

				mainWorkFlowExecution.setStatus(WorkStatus.FAILED);
				workFlowService.updateWorkFlow(mainWorkFlowExecution);
			}
			return;
		}

		log.info("Stop workflow checker: {} schedule", workFlow.getName());
		workFlowSchedulerService.stop(projectId, userId, workFlow);

		String mainWorkFlowName = WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION, WorkContextDelegate.Resource.NAME).toString();

		/*
		 * if this workflow is checker and it's successful, call continuation service to
		 * restart main workflow execution with same execution Id when there is no other
		 * active checkers
		 */
		if (workFlowService.findRunningChecker(mainWorkFlowExecution).isEmpty()) {
			workFlowContinuationServiceImpl.continueWorkFlow(ExecutionContext.builder().projectId(projectId)
					.userId(userId).workFlowName(mainWorkFlowName).workContext(workContext)
					.executionId(mainWorkFlowExecution.getId())
					.fallbackWorkFlowName(Optional
							.ofNullable(mainWorkFlowExecution.getWorkFlowDefinition().getFallbackWorkFlowDefinition())
							.map(WorkFlowDefinition::getName).orElse(null))
					.build());
		}
	}

}
