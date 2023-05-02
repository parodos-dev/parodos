package com.redhat.parodos.workflow.execution.continuation;

import java.util.UUID;

import com.redhat.parodos.workflows.work.WorkContext;
import org.springframework.scheduling.annotation.Async;

public interface AsyncWorkFlowContinuer {

	@Async
	void executeAsync(UUID projectId, String workflowName, WorkContext workContext, UUID executionId);

}
