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
package com.redhat.parodos.examples.complex.parameter;

import com.redhat.parodos.workflow.parameter.WorkParameterValueRequest;
import com.redhat.parodos.workflow.parameter.WorkParameterValueProvider;
import com.redhat.parodos.workflow.parameter.WorkParameterValueResponse;

import java.util.Collections;
import java.util.List;

/**
 * Complex Workflow Parameter Value Provider
 *
 * @author Richard Wang (Github: richardW98)
 */
public class ComplexWorkParameterValueProvider extends WorkParameterValueProvider {

	public ComplexWorkParameterValueProvider(String workFlowName) {
		super(workFlowName);
	}

	@Override
	public List<WorkParameterValueResponse> getValues(List<WorkParameterValueRequest> workParameterValueRequests) {

		if (!workParameterValueRequests.isEmpty()
				&& workParameterValueRequests.get(0).getKey().equalsIgnoreCase("WORKFLOW_SELECT_SAMPLE")
				&& workParameterValueRequests.get(0).getValue().equalsIgnoreCase("option2"))
			return List.of(
					WorkParameterValueResponse.builder().key("WORKFLOW_MULTI_SELECT_SAMPLE")
							.options(List.of("option5", "option4", "option3")).value("option5")
							.workName("complexWorkFlow").build(),
					WorkParameterValueResponse.builder().key("dynamic-options")
							.options(List.of("option15", "option14", "option13")).value("option13")
							.workName("adGroupsWorkFlowTask").build());
		return Collections.emptyList();
	}

}
