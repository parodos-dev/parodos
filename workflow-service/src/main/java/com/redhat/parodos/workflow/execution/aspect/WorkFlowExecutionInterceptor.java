package com.redhat.parodos.workflow.execution.aspect;

import java.util.Date;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
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

	protected WorkFlowExecution saveWorkFlow(WorkFlowExecution masterWorkFlowExecution) {
		String arguments = WorkFlowDTOUtil.writeObjectValueAsString(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
						workFlowDefinition.getName(), WorkContextDelegate.Resource.ARGUMENTS));
		UUID projectId = UUID.fromString(WorkContextDelegate
				.read(workContext, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID)
				.toString());
		return workFlowService.saveWorkFlow(projectId, workFlowDefinition.getId(), WorkFlowStatus.IN_PROGRESS,
				masterWorkFlowExecution, arguments);
	}

	protected abstract WorkFlowExecution doPreWorkFlowExecution();

	public WorkFlowExecution handlePreWorkFlowExecution() {
		this.workFlowExecution = doPreWorkFlowExecution();
		return this.workFlowExecution;
	}

	protected WorkFlowExecution getMasterWorkFlowExecution() {
		return null;
	}

	public WorkReport handlePostWorkFlowExecution(WorkReport report, WorkFlow workFlow) {
		// update workflow execution entity
		workFlowExecution.setStatus(WorkFlowStatus.valueOf(report.getStatus().name()));
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
						workFlowService, workFlowRepository, workFlowExecution, getMasterWorkFlowExecution());
			case CHECKER:
				return new CheckerWorkFlowPostInterceptor(workFlowDefinition, workContext, workFlowService,
						workFlowSchedulerService, workFlowContinuationServiceImpl, workFlowExecution,
						getMasterWorkFlowExecution(), workFlow, workStatus);
			default:
				break;
		}
		return null;
	}

}
