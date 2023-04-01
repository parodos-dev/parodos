package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflows.work.WorkReport;

public interface WorkFlowPostInterceptor {

	WorkReport handlePostWorkFlowExecution();

}
