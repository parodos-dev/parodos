package com.redhat.parodos.assessment;

/**
 * Constants used specify a Workflow is an AssessmentWorkflow and to establish AssessmentWorkflow specific inputs/outputs
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public interface AssessmentWorkFlowAware {
	
	// Put this value in a WorkContext to be able to locate the AsssessmentRequest
	String ASSESSMENT_REQUEST = "ASSESSMENT_REQUEST";
	
	// The results of the Assessment will be stored in the WorkContext using this label
	String RESULTING_INFRASTRUCTURE_OPTIONS = "RESULTING_INFRASTRUCTURE_OPTIONS";

	// This should be appended to the name of the Workflow so it can be filtered correctly
	String ASSESSMENT_WORKFLOW = "_ASSESSMENT_WORKFLOW";
}
