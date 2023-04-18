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
package com.redhat.parodos.examples.assessment;

import java.util.List;

import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * A simple demo showing an assessment that can read code through the GitHub api and
 * suggest workflows based on patterns detected in the code
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Configuration
public class AssessmentDemoConfig {

	// Assessment workflow option for a positive case
	@Bean(name = "ocpAssessmentResult")
	WorkFlowOption onboardingOption() {
		// @formatter:off
		return new WorkFlowOption.Builder("onboardingOption", "masterWorkFlow")
				.addToDetails("This application can be onboarded into OCP")
				.displayName("OCP Onboarding")
				.setDescription("Start OCP Onboarding")
				.build();
		// @formatter:on
	}

	// Assessment workflow option for negative case
	@Bean
	WorkFlowOption notSupportedOption() {
		// @formatter:off
		return new WorkFlowOption.Builder("notSupportedOption", "masterWorkFlow")
				.addToDetails("We do not support this option yet")
				.displayName("Not Supported Application")
				.setDescription("Review what patterns Parodos will be supporting and when")
				.build();
		// @formatter:on
	}

	// An AssessmentTask returns one or more WorkFlowOption wrapped in a WorkflowOptions
	@Bean
	PatternDetectionAssessmentTask patternDetectionAssessment(
			@Qualifier("ocpAssessmentResult") WorkFlowOption onboardingOptions,
			@Qualifier("notSupportedOption") WorkFlowOption notSupportedOption, GitHub gitHub) {
		return new PatternDetectionAssessmentTask(gitHub, List.of(onboardingOptions, notSupportedOption));
	}

	// A Workflow designed to execute and return WorkflowOption(s) that can be executed
	// next. In this case there is only one.
	@Bean(name = "ocpOnboarding" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment
	WorkFlow assessmentWorkFlow(
			@Qualifier("patternDetectionAssessment") PatternDetectionAssessmentTask patternDetectionAssessmentTask) {
		// @formatter:off
        return SequentialFlow.
        		Builder
        		.aNewSequentialFlow()
                .named("ocpOnboarding" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
                .execute(patternDetectionAssessmentTask)
                .build();
	    // @formatter:on
	}

}
