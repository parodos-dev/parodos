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
package com.redhat.parodos.workflow.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.BeanNameAware;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base Class for a WorkFlowTask.
 * <p>
 * This includes logic for getting Parameter values from the WorkContext. The bean name is
 * used when Reading from the WorkContextDelegate as more than one WorkflowTask might have
 * the same ParameterName
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardW98)
 */
@Slf4j
public abstract class BaseWorkFlowTask implements WorkFlowTask, BeanNameAware {

	@Getter
	private String name;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	// WorkFlowChecker check a process that has been initiated by a WorkFlow to see if its
	// been completed
	private List<WorkFlow> workFlowCheckers;

	public List<WorkFlow> getWorkFlowCheckers() {
		return workFlowCheckers;
	}

	public void setWorkFlowCheckers(List<WorkFlow> workFlowCheckers) {
		this.workFlowCheckers = workFlowCheckers;
	}

	/**
	 * Get Parameters specific to this WorkFlowTask, this is a required parameter
	 * @param workContext
	 * @param parameterName
	 * @return String value for the Parameter name
	 * @throws MissingParameterException
	 */
	public String getRequiredParameterValue(WorkContext workContext, String parameterName)
			throws MissingParameterException {
		Map<String, String> parameters = Optional
				.ofNullable(new ObjectMapper().convertValue(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
								name, WorkContextDelegate.Resource.ARGUMENTS),
						new TypeReference<HashMap<String, String>>() {
						}))
				.orElse(new HashMap<>());
		parameters.putAll(getParentParameters(workContext, getName()));
		return parameters.entrySet().stream().filter(entry -> parameterName.equals(entry.getKey()))
				.map(Map.Entry::getValue).findFirst().orElseThrow(() -> {
					log.error(String.format("parameter %s is not provided for task %s!", parameterName, name));
					return new MissingParameterException("missing parameter(s) for ParameterName: " + parameterName);
				});
	}

	/**
	 * Gets an optional parameter. Returns the defaultValue if not found
	 * @param workContext
	 * @param parameterName
	 * @param defaultValue
	 * @return
	 * @throws MissingParameterException
	 */
	public String getOptionalParameterValue(WorkContext workContext, String parameterName, String defaultValue) {
		Map<String, String> parameters = Optional
				.ofNullable(new ObjectMapper().convertValue(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
								name, WorkContextDelegate.Resource.ARGUMENTS),
						new TypeReference<HashMap<String, String>>() {
						}))
				.orElse(new HashMap<>());
		parameters.putAll(getParentParameters(workContext, getName()));
		return parameters.entrySet().stream().filter(entry -> parameterName.equals(entry.getKey()))
				.map(Map.Entry::getValue).findFirst().orElse(defaultValue);
	}

	/**
	 * Get Parameters that are set at the WorkFlow level
	 * @param workContext
	 * @param workName
	 * @return
	 */
	private Map<String, String> getParentParameters(WorkContext workContext, String workName) {
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