package com.redhat.parodos.workflows.execution.task;

import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Base Class for an InfrastrcutureWorkFlowTask.
 *
 * If the infrastructure WorkTask ends with a long running task outside of Parodos (i.e: waiting for ticket approval), a @see WorkFlowChecker can be specified with the
 * logic required to check the status of this external tasks
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public abstract class BaseWorkFlowTaskExecution implements WorkFlowTask {
    // WorkFlowChecker check a process that has been initiated by a WorkFlow to see if its been completed
    private WorkFlow workFlowChecker;

    public WorkFlow getGetWorkFlowChecker() {
        return workFlowChecker;
    }

    public void setWorkFlowChecker(WorkFlow gateTwo) {
        this.workFlowChecker = gateTwo;
    }
}
