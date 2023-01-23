package com.redhat.parodos.examples.simple;

import com.redhat.parodos.workflows.definition.task.WorkFlowTaskDefinition;
import com.redhat.parodos.workflows.execution.task.BaseWorkFlowTaskExecution;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class LoggingWorkFlowTaskExecution extends BaseWorkFlowTaskExecution {
    private final WorkFlowTaskDefinition loggingWorkFlowTaskDefinition;

    public LoggingWorkFlowTaskExecution(final WorkFlowTaskDefinition loggingWorkFlowTaskDefinition) {
        this.loggingWorkFlowTaskDefinition = loggingWorkFlowTaskDefinition;
    }

    @Override
    public String getName() {
        return this.loggingWorkFlowTaskDefinition.getName();
    }

    @Override
	public WorkReport execute(WorkContext workContext) {
        log.info(">>> Executing loggingWorkFlowTaskExecution");
        log.info(">>> Get in workContext arguments for the task: {}", workContext.get(loggingWorkFlowTaskDefinition.getName()));
        if (getGetWorkFlowChecker() != null) {
                log.info(">>> workflow task has a workflow checker");
        }
        log.info("Mocking a failed workflow checker task");
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}
}
