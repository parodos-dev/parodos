package com.redhat.parodos.workflow.execution.service;

import java.util.UUID;

import com.redhat.parodos.workflows.work.WorkContext;
import lombok.Builder;

import org.springframework.scheduling.annotation.Async;

public interface WorkFlowExecutor {

	@Async
	void execute(ExecutionContext context, WorkFlowService workFlowService);

	@Builder
	record ExecutionContext(UUID projectId, UUID userId, String workFlowName, WorkContext workContext, UUID executionId,
			String fallbackWorkFlowName) {
	}

}
