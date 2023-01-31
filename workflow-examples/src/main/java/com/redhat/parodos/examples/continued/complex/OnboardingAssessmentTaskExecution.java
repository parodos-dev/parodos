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

import com.redhat.parodos.assessment.BaseAssessmentTask;
import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.infrastructure.option.InfrastructureOptions;
import com.redhat.parodos.workflows.common.context.WorkContextUtil;
import com.redhat.parodos.workflows.definition.task.WorkFlowTaskDefinition;
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

    public OnboardingAssessmentTaskExecution(InfrastructureOption infrastructureOption, WorkFlowTaskDefinition onboardingAssessmentTaskDefinition) {
        super(infrastructureOption);
        this.onboardingAssessmentTaskDefinition = onboardingAssessmentTaskDefinition;
    }

    @Override
    public String getName() {
        return this.onboardingAssessmentTaskDefinition.getName();
    }

    @Override
    public WorkReport execute(WorkContext workContext) {
        WorkContextUtil.write(workContext,
                WorkContextUtil.ProcessType.WORKFLOW_EXECUTION,
                WorkContextUtil.Resource.INFRASTRUCTURE_OPTIONS,
                new InfrastructureOptions.Builder()
                        .addNewOption(getInfrastructureOptions())
                        .build());
        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }
}
