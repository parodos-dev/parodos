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
package com.redhat.parodos.workflow.context;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;

/**
 *
 * Contains useful logic that is valuable for any WorkFlowTask implementation
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
public class WorkContextDelegate {

	private WorkContextDelegate() {
	}

	private static final String spaceChar = " ";

	private static final String underscoreChar = "_";

	public enum ProcessType {

		PROJECT, WORKFLOW_DEFINITION, WORKFLOW_TASK_DEFINITION, WORKFLOW_EXECUTION, WORKFLOW_TASK_EXECUTION

	}

	public enum Resource {

		ID, NAME, PARAMETERS, ARGUMENTS, STATUS, WORKFLOW_OPTIONS

	}

	/**
	 * Used for creating a Key to store a value in the WorkFlowContext
	 * @param processType the type of Workflow object being persisted (ie:
	 * WORKFLOW_TASK_DEFINITION)
	 * @param resource the object related to the processType (ie: PARAMETERS)
	 * @return the generated key
	 */
	public static String buildKey(ProcessType processType, Resource resource) {
		return String.format("%s%s%s", processType.name(), underscoreChar, resource.name()).toUpperCase();
	}

	/**
	 * Used for creating a Key to store a value in the WorkFlowContext
	 * @param processType the type of Workflow object being persisted (ie:
	 * WORKFLOW_TASK_DEFINITION)
	 * @param workflowTaskName a unique identifier of a WorkflowTask in the event that
	 * multiple WorkflowTasks are persisting values for the the same processTpe and
	 * resource
	 * @param resource resource the object related to the processType (ie: PARAMETERS)
	 * @return
	 */
	public static String buildKey(ProcessType processType, String workflowTaskName, Resource resource) {
		return String
				.format("%s%s%s%s%s", processType.name(), underscoreChar,
						workflowTaskName.replace(spaceChar, underscoreChar), underscoreChar, resource.name())
				.toUpperCase();
	}

	/**
	 * Gets a value from the WorkflowContext by generating a Key based on the
	 * characteristics of the value supplied to this method
	 * @param workContext reference to the context
	 * @param processType the type of Workflow object being persisted (ie:
	 * WORKFLOW_TASK_DEFINITION)
	 * @param workflowTaskName a unique identifier of a WorkflowTask in the event that
	 * multiple WorkflowTasks have persisted this information
	 * @param resource the object related to the processType (ie: PARAMETERS)
	 * @return the object obtained from the WorkflowContext using the generated key. A
	 * null is returned if the key does not return a value
	 */
	public static Object read(WorkContext workContext, ProcessType processType, String workflowTaskName,
			Resource resource) {
		return workContext.get(buildKey(processType, workflowTaskName, resource));
	}

	/**
	 * Writes a value to the WorkflowContext. A Key will be generated based on the
	 * characteristics of the value supplied to this method
	 * @param workContext reference to the context
	 * @param processType the type of Workflow object being persisted (ie:
	 * WORKFLOW_TASK_DEFINITION)
	 * @param workflowTaskName a unique identifier of a WorkflowTask in the event that
	 * multiple WorkflowTasks have persisted this information
	 * @param resource the object related to the processType (ie: PARAMETERS)
	 * @param object the reference to store
	 */
	public static void write(WorkContext workContext, ProcessType processType, String workFlowTaskName,
			Resource resource, Object object) {
		workContext.put(buildKey(processType, workFlowTaskName, resource), object);
	}

	/**
	 * Gets a value from the WorkflowContext by generating a Key based on the
	 * characteristics of the value supplied to this method
	 * @param workContext reference to the context
	 * @param processType the type of Workflow object being persisted (ie:
	 * WORKFLOW_TASK_DEFINITION)
	 * @param resource the object related to the processType (ie: PARAMETERS)
	 * @return the object obtained from the WorkflowContext using the generated key. A
	 * null is returned if the key does not return a value
	 */
	public static Object read(WorkContext workContext, ProcessType processType, Resource resource) {
		return workContext.get(buildKey(processType, resource));
	}

	/**
	 * Gets a value from the WorkflowContext by generating a Key based on the
	 * characteristics of the value supplied to this method
	 * @param workContext reference to the context
	 * @param processType the type of Workflow object being persisted (ie:
	 * WORKFLOW_TASK_DEFINITION)
	 * @param resource the object related to the processType (ie: PARAMETERS)
	 * @return the object obtained from the WorkflowContext using the generated key. A
	 * null is returned if the key does not return a value
	 */
	public static void write(WorkContext workContext, ProcessType processType, Resource resource, Object object) {
		workContext.put(buildKey(processType, resource), object);
	}

	/**
	 *
	 * Gets a required value from the WorkContext with a known key
	 * @param workContext reference from the workflow-engine that is shared across
	 * WorkFlowTasks
	 * @param key used to put/get values from the WorkContext
	 * @return String value from the Map
	 * @throws MissingParameterException if the value not found
	 */
	public static String getRequiredValueFromRequestParams(WorkContext workContext, String key)
			throws MissingParameterException {
		if (workContext.get(key) == null) {
			throw new MissingParameterException(
					"For this task the WorkContext required key: " + key + " and a corresponding value");
		}
		return (String) workContext.get(key);
	}

	/**
	 * Gets an optional String value from the WorkContext with a known key
	 * @param workContext reference from the workflow-engine that is shared across
	 * WorkFlowTasks
	 * @param key used to put/get values from the WorkContext
	 * @return String value from the Map or a null if that key does not exist
	 *
	 */
	public static String getOptionalValueFromRequestParams(WorkContext workContext, String key, String defaultValue) {
		if (workContext.get(key) == null) {
			return defaultValue;
		}
		return (String) workContext.get(key);
	}

}
