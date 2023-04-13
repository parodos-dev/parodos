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
	@Bean
	WorkFlowOption onboardingOption() {
		// @formatter:off
		return new WorkFlowOption.Builder("onboardingOption", "ocpOnboardingWorkFlow")
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
		return new WorkFlowOption.Builder("notSupportedOption", "simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.addToDetails("We do not support this option yet")
				.displayName("Not Supported Application")
				.setDescription("Review what patterns Parodos will be supporting and when")
				.build();
		// @formatter:on
	}

	// An AssessmentTask returns one or more WorkFlowOption wrapped in a WorkflowOptions
	@Bean
	PatternDetectionAssessmentTask onboardingAssessmentTask(
			@Qualifier("onboardingOption") WorkFlowOption onboardingOptions,
			@Qualifier("notSupportedOption") WorkFlowOption notSupportedOption, GitHub gitHub) {
		return new PatternDetectionAssessmentTask(gitHub, List.of(onboardingOptions, notSupportedOption));
	}

	// A Workflow designed to execute and return WorkflowOption(s) that can be executed
	// next. In this case there is only one.
	@Bean(name = "onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment
	WorkFlow assessmentWorkFlow(
			@Qualifier("onboardingAssessmentTask") PatternDetectionAssessmentTask onboardingAssessmentTask) {
		// @formatter:off
        return SequentialFlow.
        		Builder
        		.aNewSequentialFlow()
                .named("onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
                .execute(onboardingAssessmentTask)
                .build();
	    // @formatter:on
	}

}
