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
package com.redhat.parodos.workflow.consts;

/**
 * Represent Keys that can be put into the WorkContext to get out specific values that
 * might be used by WorkFlowService or WorkFlowEngine implementations
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class WorkFlowConstants {

	// Can be used in the name/id of a Workflow to indicate its an Infrastructure WorkFlow
	public static final String INFRASTRUCTURE_WORKFLOW = "_INFRASTRUCTURE_WORKFLOW";

	// Can be used in the name/id of a Workflow to indicate its a Checker WorkFlow
	public static final String CHECKER_WORKFLOW = "_CHECKER_WORKFLOW";

	// Name of the project that the WorkFlow was associated with
	public static final String PROJECT_NAME = "PROJECT_NAME";

	// Id of a workflow that can 'undo' the actions of a previously executed WorkFlow
	public static final String ROLL_BACK_WORKFLOW_NAME = "ROLL_BACK_WORKFLOW_NAME";

	// This should be appended to the name of the Workflow so it can be filtered correctly
	public static final String ASSESSMENT_WORKFLOW = "_ASSESSMENT_WORKFLOW";

	// Ensures this class is only used to obtain the constants
	private WorkFlowConstants() {
	}

}
