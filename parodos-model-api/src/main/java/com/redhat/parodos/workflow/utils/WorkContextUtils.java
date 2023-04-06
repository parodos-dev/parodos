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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Util Class to parse WorkContext
 *
 * @author Richard Wang (Github: richardW98)
 */

public class WorkContextUtils {

	private WorkContextUtils() {
	}

	public static String getProjectId(WorkContext workContext) {
		return WorkContextDelegate
				.read(workContext, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID).toString();
	}

	public static String getMainExecutionId(WorkContext workContext) {
		return WorkContextDelegate
				.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID)
				.toString();
	}

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

	/**
	 * Get Parameters that are set at the WorkFlow level
	 * @param workContext
	 * @param workName
	 * @return
	 */
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

}
