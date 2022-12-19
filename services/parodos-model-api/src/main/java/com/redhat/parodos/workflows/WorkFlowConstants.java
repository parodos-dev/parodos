/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflows;

/**
 * Represent Keys that can be put into the WorkContext to get out specific values that might be used by WorkFlowService or WorkFlowEngine implementations
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class WorkFlowConstants {
	
	//Can be used in the name/id of a Workflow to indicate its an Infrastructure Task WorkFlow
	public static final String INFRASTRUCTURE_WORKFLOW = "_INFRASTRUCTURE_WORKFLOW";
	
	//Name of the project that the WorkFlow was associated with
	public static final String PROJECT_NAME = "PROJECT_NAME";
	
	//Id of a workflow that can 'undo' the actions of a previously executed WorkFlow
	public static final String ROLL_BACK_WORKFLOW_NAME = "ROLL_BACK_WORKFLOW_NAME";
	
	// The results of the Assessment will be stored in the WorkContext using this label
	public static final String RESULTING_INFRASTRUCTURE_OPTIONS = "RESULTING_INFRASTRUCTURE_OPTIONS";

	// This should be appended to the name of the Workflow so it can be filtered correctly
	public static final String ASSESSMENT_WORKFLOW = "_ASSESSMENT_WORKFLOW";

	//An entity generated with all WorkFlow executions
	public static final String WORKFLOW_EXECUTION_ENTITY_REFERENCES = "WORKFLOW_EXECUTION_ENTITY_REFERENCES";
	
	//The type of the WorkFlow
	public static final String WORKFLOW_TYPE = "WORKFLOW_TYPE";
	
	//This is used when you need to specify an assessment to determine if follow on workflow can be executed
	public static final String WORKFLOW_CHECKER_ID = "WORKFLOW_CHECKER_ID";

	//The arguments for the WorkFlow Checker to run. As these might be runtime values they will be persisted in the WorkFlowTransactionDTO
	public static final String WORKFLOW_CHECKER_ARGUMENTS = "WORKFLOW_CHECKER_ARGUMENTS";

	public static final String CURRENT_WORKFLOW_ID = "CURRENT_WORKFLOW_ID";

	//This is for an WorkFlowChecker to store the value of the next WorkFlow to run in a WorkFlowTransactionDTO
	public static final String NEXT_WORKFLOW_ID = "NEXT_WORKFLOW_ID";

	//These arguments are for passing values from the WorkFlowChecker to the next WorkFlow to run
	public static final String NEXT_WORKFLOW_ARGUMENTS = "NEXT_WORKFLOW_ARGUMENTS";

	//TransactionRepository
	public static final String TRANSACTION_REPOSITORY = "TRANSACTION_REPOSITORY";

	//Ensures this class is only used to obtain the constants
	private WorkFlowConstants() {
	}

}
