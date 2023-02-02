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

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.option.WorkFlowOptions;
import com.redhat.parodos.workflow.task.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

/**
 * Simple Assessment to determine if Onboarding is appropriate
 *
 * @author Luke Shannon (Github: lshannon)
 */
public class OnboardingAssessmentTaskExecution extends BaseAssessmentTask {
    private final WorkFlowTaskDefinition onboardingAssessmentTaskDefinition;

    public OnboardingAssessmentTaskExecution(WorkFlowOption infrastructureOption, WorkFlowTaskDefinition onboardingAssessmentTaskDefinition) {
        super(infrastructureOption);
        this.onboardingAssessmentTaskDefinition = onboardingAssessmentTaskDefinition;
    }

    @Override
    public String getName() {
        return this.onboardingAssessmentTaskDefinition.getName();
    }

    @Override
    public WorkReport execute(WorkContext workContext) {
    	WorkContextDelegate.write(workContext,
    			WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
    			WorkContextDelegate.Resource.INFRASTRUCTURE_OPTIONS,
                new WorkFlowOptions.Builder()
                        .addNewOption(getInfrastructureOptions())
                        .build());
        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }
}
