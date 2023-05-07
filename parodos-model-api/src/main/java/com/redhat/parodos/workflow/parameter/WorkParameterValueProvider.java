/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.parameter;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Workflow Parameter Value Provider
 *
 * @author Richard Wang (Github: richardW98)
 */
@Slf4j
public abstract class WorkParameterValueProvider {

	private final String workflowName;

	protected WorkParameterValueProvider(String workflowName) {
		this.workflowName = workflowName;
	}

	/**
	 * get parameter dynamic value
	 * @param workParameterValueRequests
	 * @return
	 */
	protected abstract List<WorkParameterValueResponse> getValues(
			List<WorkParameterValueRequest> workParameterValueRequests);

	public List<WorkParameterValueResponse> getValuesForWorkflow(String workFlowName,
			List<WorkParameterValueRequest> workParameterValueRequests) {
		if (!this.workflowName.equals(workFlowName)) {
			log.error("can't update value for workflow {} inside workflow {}", workFlowName, this.workflowName);
			return Collections.emptyList();
		}
		return getValues(workParameterValueRequests);
	}

}
