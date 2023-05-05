package com.redhat.parodos.workflow.execution.service;

import java.util.UUID;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkFlowExecutorImpl implements WorkFlowExecutor {

	private final WorkFlowDelegate workFlowDelegate;

	public WorkFlowExecutorImpl(WorkFlowDelegate workFlowDelegate) {
		this.workFlowDelegate = workFlowDelegate;
	}

	@Override
	public void executeAsync(UUID projectId, String workflowName, WorkContext workContext, UUID executionId) {
		execute(projectId, workflowName, workContext, executionId);
	}

	@Override
	public WorkReport execute(UUID projectId, String workflowName, WorkContext workContext, UUID executionId) {
		WorkFlow workFlow = workFlowDelegate.getWorkFlowExecutionByName(workflowName);
		log.info("execute workFlow '{}': {}", workflowName, workFlow);
		WorkContextUtils.updateWorkContextPartially(workContext, projectId, workflowName, executionId);
		return WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, workContext);
	}

}
