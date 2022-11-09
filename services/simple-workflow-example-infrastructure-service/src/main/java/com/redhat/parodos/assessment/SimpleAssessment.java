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
package com.redhat.parodos.assessment;

import java.util.List;
import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.infrastructure.option.InfrastructureOptions;
import com.redhat.parodos.workflows.BaseWorkFlowTask;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * This is an Assessment to determine if an application is suitable for a 
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class SimpleAssessment implements BaseWorkFlowTask {
	
	//We only have one InfrastructureOption to recommend - but this could also be a list or a Map of Options
	private final InfrastructureOption myOption;
	
	public SimpleAssessment(InfrastructureOption myOption) {
		this.myOption = myOption;
	}

	//This will return the same InfrastructureOptions
	public WorkReport execute(WorkContext workContext) {
		log.info("This is my assessment - it always recommends the InfrastructureOption 'Awesome Tool Stack With Correct Permissions And Config'");
		log.info("Putting the recommended InfrastructureOption in the InfrastructureOptions wrapper and placing it in the WorkContext");
		workContext.put(WorkFlowConstants.RESULTING_INFRASTRUCTURE_OPTIONS, 
				new InfrastructureOptions.Builder()
				.addNewOption(myOption)
				.build());
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	//Supply the a parameter to describe what is needed for this workflow, but also for the UI to render a control to capture it
	@Override
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return List.of(WorkFlowTaskParameter.builder().key("DOG_NAME").description("String value with the name of a cool dog").build());
	}
	
}