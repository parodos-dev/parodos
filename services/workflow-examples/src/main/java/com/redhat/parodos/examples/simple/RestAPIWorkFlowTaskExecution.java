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
public class RestAPIWorkFlowTaskExecution extends BaseWorkFlowTaskExecution {
    private final WorkFlowTaskDefinition restAPIWorkFlowTaskDefinition;

    public RestAPIWorkFlowTaskExecution(final WorkFlowTaskDefinition restAPIWorkFlowTaskDefinition) {
        this.restAPIWorkFlowTaskDefinition = restAPIWorkFlowTaskDefinition;
    }

    @Override
    public String getName() {
        return this.restAPIWorkFlowTaskDefinition.getName();
    }
    /**
     * Executed by the InfrastructureTask engine as part of the Workflow
     */
    @Override
    public WorkReport execute(WorkContext workContext) {
        log.info("### Mocking a RestAPIWorkFlowTaskExecution");
        log.info("### Getting in workContext arguments for the task: {}", workContext.get(restAPIWorkFlowTaskDefinition.getName()));
        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }
}
