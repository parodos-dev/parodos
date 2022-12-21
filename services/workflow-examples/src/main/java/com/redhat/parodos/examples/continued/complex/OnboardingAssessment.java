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
package com.redhat.parodos.examples.continued.complex;

import java.util.List;
import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.infrastructure.option.InfrastructureOptions;
import com.redhat.parodos.workflows.BaseAssessmentTask;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

/**
 * Simple Assessment to determine if Onboarding is appropriate
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class OnboardingAssessment extends BaseAssessmentTask {

	private static final String INPUT = "INPUT";

	public OnboardingAssessment(InfrastructureOption infrastructureOption, String name) {
		super(infrastructureOption, name);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		workContext.put(WorkFlowConstants.RESULTING_INFRASTRUCTURE_OPTIONS, 
				new InfrastructureOptions.Builder()
				.addNewOption(getInfrastructureOptions())
				.build());
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}
	
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkFlowTaskParameter.builder()
					.key(INPUT)
					.description("Enter some information to use for the Assessment to determine if they can onboard")
					.optional(false)
					.type(WorkFlowTaskParameterType.TEXT)
					.build());
	}
	

}
