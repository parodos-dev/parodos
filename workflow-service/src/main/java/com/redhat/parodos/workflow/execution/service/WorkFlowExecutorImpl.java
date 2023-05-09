package com.redhat.parodos.workflow.execution.service;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkFlowExecutorImpl implements WorkFlowExecutor {

	private final WorkFlowDelegate workFlowDelegate;

	private final WorkFlowRepository workFlowRepository;

	public WorkFlowExecutorImpl(WorkFlowDelegate workFlowDelegate, WorkFlowRepository workFlowRepository) {
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowRepository = workFlowRepository;
	}

	@Override
	public void executeAsync(UUID projectId, String workflowName, WorkContext workContext, UUID executionId,
			String rollbackWorkflowName) {
		execute(projectId, workflowName, workContext, executionId, rollbackWorkflowName);
	}

	@Override
	public WorkReport execute(UUID projectId, String workflowName, WorkContext workContext, UUID executionId,
			String rollbackWorkflowName) {
		WorkFlow workFlow = workFlowDelegate.getWorkFlowByName(workflowName);
		log.info("execute workFlow {}", workflowName);
		WorkContextUtils.updateWorkContextPartially(workContext, projectId, workflowName, executionId);
		WorkReport report = WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, workContext);
		// need to use the status from db to avoid of repetitive execution on rollback
		if (workFlowRepository.findById(executionId).map(execution -> execution.getStatus() == WorkStatus.FAILED)
				.orElse(false)) {
			Optional.ofNullable(workFlowDelegate.getWorkFlowByName(rollbackWorkflowName))
					.ifPresentOrElse(rollbackWorkFlow -> {
						log.error(
								"The Infrastructure  workflow failed. Check the logs for errors coming for the Tasks in this workflow. Checking if there is a Rollback");
						WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(rollbackWorkFlow, workContext);
					}, () -> log.error(
							"A rollback workflow could not be found for failed workflow: {} in execution: {}",
							workflowName, executionId));
		}
		return report;
	}

}
