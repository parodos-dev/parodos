package com.redhat.parodos.workflow.execution.scheduler;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;

public interface WorkFlowSchedulerService {
    void schedule(WorkFlow workFlow, WorkContext workContext, String cronExpression);
    boolean stop(WorkFlow workFlow);
}
