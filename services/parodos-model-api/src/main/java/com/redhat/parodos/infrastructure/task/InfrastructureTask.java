package com.redhat.parodos.infrastructure.task;

import com.redhat.parodos.workflows.work.Work;

/**
 * When create/updating infrastructure there will be series of tasks that get executed in parallel or in series. To create a InfrastructureTask,
 * implement this interface and the custom logic for the step in the execute method
 * 
 * 
 * @author lukeshannon
 *
 */
public interface InfrastructureTask extends Work {
	
	static final String WORK_FLOW_TYPE = "INFRA_TASK";
}
