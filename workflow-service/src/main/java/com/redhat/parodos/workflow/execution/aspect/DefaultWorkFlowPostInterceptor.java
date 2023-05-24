package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.service.WorkFlowService;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkReport;

public class DefaultWorkFlowPostInterceptor implements WorkFlowPostInterceptor {

	private WorkFlowService workFlowService;

	private WorkFlowExecution workFlowExecution;

	public DefaultWorkFlowPostInterceptor(WorkFlowServiceImpl workFlowService, WorkFlowExecution workFlowExecution) {
		this.workFlowService = workFlowService;
		this.workFlowExecution = workFlowExecution;
	}

	@Override
	public WorkReport handlePostWorkFlowExecution() {
		workFlowService.updateWorkFlow(workFlowExecution);
		return null;
	}

}
