package com.redhat.parodos.workflow.task;

import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Base Class for a WorkFlowTask. 
 *
 * This includes the option for a @see WorkFlowChecker to be specified in the event that this WorkFlowTask triggers a long running process that will block further Workflows from being able to execute
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public abstract class BaseWorkFlowTask implements WorkFlowTask {
    
	// WorkFlowChecker check a process that has been initiated by a WorkFlow to see if its been completed
    private WorkFlow checkerWorkflow;

    public WorkFlow getGetWorkFlowChecker() {
        return checkerWorkflow;
    }

    public void setWorkFlowChecker(WorkFlow checkerWorkflow) {
        this.checkerWorkflow = checkerWorkflow;
    }
}
