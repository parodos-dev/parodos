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
package com.redhat.parodos.examples.continued.complex;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.parodos.examples.simple.LoggingWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * A more complex WorkFlow
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Configuration
public class ComplexWorkFlowConfiguration {

	// START assessmentWorkFlow definition

	// A WorkflowOption describing in user-friendly terms what a Workflow does - this can
	// be used in a UI to provide choices to a User on which Workflow to run
	@Bean
	WorkFlowOption onboardingOption() {
		return new WorkFlowOption.Builder("onboardingOption",
				"onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
						.addToDetails("An example of a complex WorkFlow with Status checks").displayName("Onboarding")
						.setDescription("An example of a complex WorkFlow").build();
	}

	// An AssessmentTask returns one or more WorkFlowOption wrapped in a WorkflowOptions
	@Bean
	OnboardingAssessmentTask onboardingAssessmentTask(
			@Qualifier("onboardingOption") WorkFlowOption awesomeToolsOption) {
		return new OnboardingAssessmentTask(awesomeToolsOption);
	}

	// A Workflow designed to execute and return WorkflowOption(s) that can be executed
	// next. In this case there is only one.
	@Bean(name = "onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment
	WorkFlow assessmentWorkFlow(
			@Qualifier("onboardingAssessmentTask") OnboardingAssessmentTask onboardingAssessmentTask) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("onboarding Assessment WorkFlow")
				.execute(onboardingAssessmentTask)
				.build();
		// @formatter:on
	}

	// END assessmentWorkFlow definition

	// Start onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW definition
	// (this is the Workflow described in the WorkflowOption above)
	@Bean
	LoggingWorkFlowTask certWorkFlowTask(@Qualifier("namespaceWorkFlow"
			+ WorkFlowConstants.CHECKER_WORKFLOW) WorkFlow namespaceWorkFlowCheckerWorkFlow) {
		LoggingWorkFlowTask loggingWorkFlow = new LoggingWorkFlowTask();
		loggingWorkFlow.setWorkFlowChecker(namespaceWorkFlowCheckerWorkFlow);
		return loggingWorkFlow;
	}

	@Bean
	LoggingWorkFlowTask adGroupWorkFlowTask(@Qualifier("onboardingWorkFlow"
			+ WorkFlowConstants.CHECKER_WORKFLOW) WorkFlow onboardingWorkFlowCheckerWorkFlow) {
		LoggingWorkFlowTask loggingWorkFlow = new LoggingWorkFlowTask();
		loggingWorkFlow.setWorkFlowChecker(onboardingWorkFlowCheckerWorkFlow);
		return loggingWorkFlow;
	}

	@Bean
	LoggingWorkFlowTask dynatraceWorkFlowTask(@Qualifier("onboardingWorkFlow"
			+ WorkFlowConstants.CHECKER_WORKFLOW) WorkFlow onboardingWorkFlowCheckerWorkFlow) {
		LoggingWorkFlowTask loggingWorkFlow = new LoggingWorkFlowTask();
		loggingWorkFlow.setWorkFlowChecker(onboardingWorkFlowCheckerWorkFlow);
		return loggingWorkFlow;
	}

	// runs the set of Tasks associated with "Onboarding"
	@Bean(name = "onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow onboardingWorkflow(@Qualifier("certWorkFlowTask") LoggingWorkFlowTask certWorkFlowTask,
			@Qualifier("adGroupWorkFlowTask") LoggingWorkFlowTask adGroupWorkFlowTask,
			@Qualifier("dynatraceWorkFlowTask") LoggingWorkFlowTask dynatraceWorkFlowTask) {
		// @formatter:off
		return ParallelFlow.Builder.aNewParallelFlow()
				.named("onboarding Infrastructure WorkFlow")
				.execute(certWorkFlowTask, adGroupWorkFlowTask, dynatraceWorkFlowTask)
				.with(Executors.newFixedThreadPool(3))
				.build();
		// @formatter:on
	}
	// End onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW definition

	// Start Name Space Logic
	@Bean
	LoggingWorkFlowTask nameSpaceWorkFlowTask(@Qualifier("namespaceWorkFlow"
			+ WorkFlowConstants.CHECKER_WORKFLOW) WorkFlow namespaceWorkFlowCheckerWorkFlow) {
		LoggingWorkFlowTask loggingWorkFlow = new LoggingWorkFlowTask();
		loggingWorkFlow.setWorkFlowChecker(namespaceWorkFlowCheckerWorkFlow);
		return loggingWorkFlow;
	}

	@Bean(name = "nameSpaceWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow nameSpaceWorkFlow(@Qualifier("nameSpaceWorkFlowTask") LoggingWorkFlowTask nameSpaceWorkFlowTask) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("nameSpace Infrastructure WorkFlow")
				.execute(nameSpaceWorkFlowTask)
				.build();
		// @formatter:on
	}
	// End Name Space Logic

	// Start networking workflow Logic
	@Bean
	LoggingWorkFlowTask loadBalancerFlowTask() {
		return new LoggingWorkFlowTask();
	}

	@Bean
	LoggingWorkFlowTask failOverWorkFlowTask() {
		return new LoggingWorkFlowTask();
	}

	@Bean(name = "networkingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow networkingWorkFlow(@Qualifier("loadBalancerFlowTask") LoggingWorkFlowTask networkingFlowTask,
			@Qualifier("failOverWorkFlowTask") LoggingWorkFlowTask failOverWorkFlowTask) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("networking Infrastructure WorkFlow")
				.execute(networkingFlowTask)
				.then(failOverWorkFlowTask)
				.build();
		// @formatter:on
	}
	// End networking workflow Logic

	// Start onboardingWorkFlowCheck Logic
	@Bean
	MockApprovalWorkFlowCheckerTask approvalChecker() {
		return new MockApprovalWorkFlowCheckerTask();
	}

	@Bean("onboardingWorkFlow" + WorkFlowConstants.CHECKER_WORKFLOW)
	@Checker(cronExpression = "0 0/1 * * * ?")
	WorkFlow onboardingWorkFlowCheckerWorkFlow(
			@Qualifier("approvalChecker") MockApprovalWorkFlowCheckerTask approvalChecker) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("onboarding Checker WorkFlow")
				.execute(approvalChecker)
				.build();
		// @formatter:on
	}
	// End onboardingWorkFlowCheck Logic

	// Start namespaceWorkFlowCheck Logic
	@Bean
	MockApprovalWorkFlowCheckerTask processCompletionChecker() {
		return new MockApprovalWorkFlowCheckerTask();
	}

	@Bean("namespaceWorkFlow" + WorkFlowConstants.CHECKER_WORKFLOW)
	@Checker(cronExpression = "0 0/1 * * * ?")
	WorkFlow namespaceWorkFlowCheckerWorkFlow(
			@Qualifier("processCompletionChecker") MockApprovalWorkFlowCheckerTask processCompletionChecker) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("namespace Checker WorkFlow")
				.execute(processCompletionChecker)
				.build();
		// @formatter:on
	}
	// End namespaceWorkFlowCheck Logic

}
