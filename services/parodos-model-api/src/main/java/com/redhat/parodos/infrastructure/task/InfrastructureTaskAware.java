package com.redhat.parodos.infrastructure.task;


/**
 * 
 * Constants used specify a Workflow related to Infrastructure Tasks
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public interface InfrastructureTaskAware {
	
	String INFRASTRUCTURE_TASK_WORKFLOW = "INFRASTRUCTURE_TASK_WORKFLOW";
	String INFRASTRUCTURE_TASK_WORKFLOW_DETAILS = "INFRASTRUCTURE_TASK_WORKFLOW_DETAILS";
	String EXISTING_INFRASTRUCTURE_DETAILS = "EXISTING_INFRASTRUCTURE_DETAILS";
	String INFRASTRUCTURE_DISPLAY_VALUE = "INFRASTRUCTURE_DISPLAY_VALUE";
	String PROJECT_NAME = "PROJECT_NAME";
	String ROLL_BACK_WORKFLOW_NAME = "ROLL_BACK_WORKFLOW_NAME";
}
