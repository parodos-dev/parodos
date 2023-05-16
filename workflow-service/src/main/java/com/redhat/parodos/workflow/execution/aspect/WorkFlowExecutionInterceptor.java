package com.redhat.parodos.workflow.execution.aspect;

import java.util.Date;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;

public abstract class WorkFlowExecutionInterceptor implements WorkFlowInterceptor {

	protected final WorkFlowServiceImpl workFlowService;

	protected final WorkFlowRepository workFlowRepository;

	protected final WorkContext workContext;

	protected final WorkFlowDefinition workFlowDefinition;

	protected WorkFlowExecution workFlowExecution;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	public WorkFlowExecutionInterceptor(WorkFlowDefinition workFlowDefinition, WorkContext workContext,
			WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		this.workFlowDefinition = workFlowDefinition;
		this.workContext = workContext;
		this.workFlowService = workFlowService;
		this.workFlowRepository = workFlowRepository;
		this.workFlowSchedulerService = workFlowSchedulerService;
		this.workFlowContinuationServiceImpl = workFlowContinuationServiceImpl;
	}

	protected WorkFlowExecution saveWorkFlow(WorkFlowExecution mainWorkFlowExecution) {
		String arguments = WorkFlowDTOUtil.writeObjectValueAsString(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
						workFlowDefinition.getName(), WorkContextDelegate.Resource.ARGUMENTS));
		UUID projectId = WorkContextUtils.getProjectId(workContext);
		UUID userId = WorkContextUtils.getUserId(workContext);
		return workFlowService.saveWorkFlow(projectId, userId, workFlowDefinition, WorkStatus.IN_PROGRESS,
				mainWorkFlowExecution, arguments);
	}

	protected abstract WorkFlowExecution doPreWorkFlowExecution();

	public WorkFlowExecution handlePreWorkFlowExecution() {
		this.workFlowExecution = doPreWorkFlowExecution();
		return this.workFlowExecution;
	}

	protected WorkFlowExecution getMainWorkFlowExecution() {
		return null;
	}

	public WorkReport handlePostWorkFlowExecution(WorkReport report, WorkFlow workFlow) {
		// update workflow execution entity
		workFlowExecution.setStatus(WorkStatus.valueOf(report.getStatus().name()));
		workFlowExecution.setEndDate(new Date());

		WorkFlowPostInterceptor postExecutor = createPostExecutor(workFlow, report.getStatus());
		WorkReport workReport = null;
		if (postExecutor != null) {
			workReport = postExecutor.handlePostWorkFlowExecution();
		}
		return workReport == null ? report : workReport;
	}

	private WorkFlowPostInterceptor createPostExecutor(WorkFlow workFlow, WorkStatus workStatus) {
		switch (workFlowDefinition.getType()) {
			case INFRASTRUCTURE:
			case ASSESSMENT:
				return new AssessmentInfrastructureWorkFlowPostInterceptor(workFlowDefinition, workContext,
						workFlowService, workFlowRepository, workFlowExecution, getMainWorkFlowExecution());
			case CHECKER:
				return new CheckerWorkFlowPostInterceptor(workFlowDefinition, workContext, workFlowService,
						workFlowSchedulerService, workFlowContinuationServiceImpl, workFlowExecution,
						getMainWorkFlowExecution(), workFlow, workStatus);
			default:
				workFlowService.updateWorkFlow(workFlowExecution);
				break;
		}
		return null;
	}

}
