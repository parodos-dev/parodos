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
package com.redhat.parodos.examples.ocponboarding.task.assessment;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.option.WorkFlowOptions;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Simple Assessment to determine if an Onboarding Workflow is appropriate. It returns a
 * WorkflowOption to represent the Onboarding Workflow
 *
 * @author Richard Wang (Github: richardW98)
 */
@Slf4j
public class OnboardingOcpAssessmentTask extends BaseAssessmentTask {

	private static final String INPUT = "INPUT";

	public OnboardingOcpAssessmentTask(List<WorkFlowOption> workflowOptions) {
		super(workflowOptions);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
//		try {
//			log.info("parameter {} value: {}", INPUT, getRequiredParameterValue(workContext, INPUT));
//		} catch (MissingParameterException e) {
//			log.error("can't get parameter {} value", INPUT);
//			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
//		}

		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
				// @formatter:off
				new WorkFlowOptions.Builder()
				.addNewOption(getWorkFlowOptions().get(0))
				.build());
				// @formatter:on
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return List.of(WorkFlowTaskParameter.builder().key(INPUT)
				.description("Enter some information to use for the Assessment to determine if they can onboard")
				.optional(false).type(WorkFlowTaskParameterType.TEXT).build());
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return Collections.emptyList();
	}

}
