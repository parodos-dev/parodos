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
package com.redhat.parodos.examples.complex.task;

import java.util.Collections;
import java.util.List;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.option.WorkFlowOptions;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

/**
 * Simple Assessment to determine if an Onboarding Workflow is appropriate. It returns a
 * WorkflowOption to represent the Onboarding Workflow
 *
 * @author Luke Shannon (Github: lshannon)
 */
public class OnboardingAssessmentTask extends BaseAssessmentTask {

	private static final String INPUT = "INPUT";

	public OnboardingAssessmentTask(WorkFlowOption workflowOption) {
		super(List.of(workflowOption));
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
				new WorkFlowOptions.Builder().addNewOption(getWorkFlowOptions().get(0)).build());
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	public List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(INPUT)
						.description(
								"Enter some information to use for the Assessment to determine if they can onboard")
						.optional(false).type(WorkParameterType.TEXT).build(),
				WorkParameter.builder().key("SELECT_SAMPLE").description("select sample").optional(true)
						.type(WorkParameterType.SELECT).selectOptions(List.of("projectA", "projectB", "projectC"))
						.build(),
				WorkParameter.builder().key("MULTI_SELECT_SAMPLE").description("multi select sample").optional(true)
						.type(WorkParameterType.MULTI_SELECT).selectOptions(List.of("select1", "select2")).build());
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return Collections.emptyList();
	}

}
