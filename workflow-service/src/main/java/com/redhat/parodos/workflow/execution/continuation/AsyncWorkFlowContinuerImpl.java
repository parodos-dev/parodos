package com.redhat.parodos.workflow.execution.continuation;

import java.util.UUID;

import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;

import org.springframework.stereotype.Service;

@Service
public class AsyncWorkFlowContinuerImpl implements AsyncWorkFlowContinuer {

	private final WorkFlowServiceImpl workFlowService;

	public AsyncWorkFlowContinuerImpl(WorkFlowServiceImpl workFlowService) {
		this.workFlowService = workFlowService;
	}

	@Override
	public void executeAsync(UUID projectId, String workflowName, WorkContext workContext, UUID executionId) {
		workFlowService.execute(projectId, workflowName, workContext, executionId);
	}

}
