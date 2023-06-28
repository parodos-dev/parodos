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
package com.redhat.parodos.workflow.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.NonNull;

/**
 * Util Class to parse WorkContext
 *
 * @author Richard Wang (Github: richardW98)
 */

public abstract class WorkContextUtils {

	private WorkContextUtils() {
	}

	/**
	 * method to get user id from workContext
	 * @param workContext work context
	 * @return user id
	 */
	public static UUID getUserId(WorkContext workContext) {
		Object userId = WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.USER,
				WorkContextDelegate.Resource.ID);
		userId = Optional.ofNullable(userId)
				.orElseThrow(() -> new NoSuchElementException("User id is missing from workContext."));
		return UUID.fromString(userId.toString());
	}

	/**
	 * method to set user id to workContext
	 * @param workContext work context
	 * @param userId user id
	 */
	public static void setUserId(WorkContext workContext, @NonNull UUID userId) {
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.USER, WorkContextDelegate.Resource.ID,
				userId.toString());
	}

	/**
	 * method to get project id from workContext
	 * @param workContext work context
	 * @return project id
	 */
	public static UUID getProjectId(WorkContext workContext) {
		Object projectId = WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.PROJECT,
				WorkContextDelegate.Resource.ID);
		projectId = Optional.ofNullable(projectId)
				.orElseThrow(() -> new NoSuchElementException("Project id is missing from workContext."));
		return UUID.fromString(projectId.toString());
	}

	/**
	 * method to set project id to workContext
	 * @param workContext work context
	 * @param projectId project id
	 */
	public static void setProjectId(WorkContext workContext, @NonNull UUID projectId) {
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID,
				projectId.toString());
	}

	/**
	 * get main workflow execution id from workContext
	 * @param workContext work context
	 * @return main workflow execution id
	 */
	public static UUID getMainExecutionId(WorkContext workContext) {
		Object workflowExecutionId = WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID);
		workflowExecutionId = Optional.ofNullable(workflowExecutionId)
				.orElseThrow(() -> new NoSuchElementException("Workflow execution id is missing from workContext."));
		return UUID.fromString(workflowExecutionId.toString());
	}

	public static void setMainExecutionId(WorkContext workContext, UUID executionId) {
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, executionId.toString());
	}

	/**
	 * add a new common parameter to workContext
	 * @param workContext work context
	 * @param key parameter key (name)
	 * @param value parameter value
	 */
	public static void addParameter(WorkContext workContext, String key, String value) {
		Map<String, String> parameterMap = Optional
				.ofNullable(new ObjectMapper().convertValue(WorkContextDelegate.read(workContext,
						WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ARGUMENTS),
						new TypeReference<HashMap<String, String>>() {
						}))
				.orElse(new HashMap<>());
		parameterMap.put(key, value);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ARGUMENTS, parameterMap);
	}

	/**
	 * add additional info for the workflow, e.g. result links
	 * @param workContext
	 * @param key
	 * @param value
	 */
	public static void addAdditionalInfo(WorkContext workContext, String key, String value) {
		Map<String, String> additionalInfoMap = Optional
				.ofNullable(new ObjectMapper().convertValue(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
								WorkContextDelegate.Resource.ADDITIONAL_INFO),
						new TypeReference<HashMap<String, String>>() {
						}))
				.orElse(new HashMap<>());
		additionalInfoMap.put(key, value);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ADDITIONAL_INFO, additionalInfoMap);
	}

	public static Map<String, String> getAdditionalInfo(WorkContext workContext) {
		return new ObjectMapper().convertValue(WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ADDITIONAL_INFO),
				new TypeReference<>() {
				});
	}

	/**
	 * get all available parameters for a task
	 * @param workContext work context
	 * @param name task name
	 * @return Map of parameters
	 */
	public static Map<String, String> getAllParameters(WorkContext workContext, String name) {
		Map<String, String> parameters = Optional
				.ofNullable(new ObjectMapper().convertValue(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
								name, WorkContextDelegate.Resource.ARGUMENTS),
						new TypeReference<HashMap<String, String>>() {
						}))
				.orElse(new HashMap<>());
		parameters.putAll(Optional.ofNullable(new ObjectMapper().convertValue(WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ARGUMENTS),
				new TypeReference<HashMap<String, String>>() {
				})).orElse(new HashMap<>()));
		parameters.putAll(getParentParameters(workContext, name));
		return parameters;
	}

	private static Map<String, String> getParentParameters(WorkContext workContext, String workName) {
		String parentWorkflowName = (String) WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, workName,
				WorkContextDelegate.Resource.PARENT_WORKFLOW);
		Map<String, String> map = new HashMap<>();
		Optional.ofNullable(
				new ObjectMapper().convertValue(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
								workName, WorkContextDelegate.Resource.ARGUMENTS),
						new TypeReference<HashMap<String, String>>() {
						}))
				.ifPresent(map::putAll);
		if (parentWorkflowName != null) {
			map.putAll(getParentParameters(workContext, parentWorkflowName));
		}
		return map;
	}

	public static void updateWorkContextPartially(WorkContext workContext, UUID projectId, UUID userId,
			String workflowName, UUID executionId) {
		if (executionId != null) {
			setMainExecutionId(workContext, executionId);
		}
		WorkContextUtils.setProjectId(workContext, projectId);
		WorkContextUtils.setUserId(workContext, userId);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME, workflowName);
	}

}
