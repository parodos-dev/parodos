package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.WorkFlow;

public interface WorkFlowInterceptor {

	WorkFlowExecution handlePreWorkFlowExecution();

	WorkReport handlePostWorkFlowExecution(WorkReport report, WorkFlow target);

}
