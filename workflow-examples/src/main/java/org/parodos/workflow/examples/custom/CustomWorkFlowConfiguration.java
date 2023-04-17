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
package org.parodos.workflow.examples.custom;

import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.parodos.workflow.examples.custom.task.CustomWorkFlowTask;
import org.parodos.workflow.examples.custom.task.SimpleWorkFlowCheckerTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Very simple workflow configurations in different package from Parodos main class
 *
 * @author Richard Wang (Github: richardW98)
 */

@Configuration
public class CustomWorkFlowConfiguration {

	// START Custom Sequential Example (WorkflowTasks and Workflow Definitions)
	@Bean
	CustomWorkFlowTask customWorkFlowTaskOne(@Qualifier("simpleWorkFlowChecker") WorkFlow simpleWorkFlowChecker) {
		CustomWorkFlowTask customWorkFlowTaskOne = new CustomWorkFlowTask();
		customWorkFlowTaskOne.setWorkFlowCheckers(List.of(simpleWorkFlowChecker));
		return customWorkFlowTaskOne;
	}

	@Bean
	CustomWorkFlowTask customWorkFlowTaskTwo() {
		return new CustomWorkFlowTask();
	}

	@Bean(name = "customWorkflow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure(parameters = {
			@Parameter(key = "workloadId", description = "The workload id", type = WorkParameterType.TEXT,
					optional = false),
			@Parameter(key = "projectUrl", description = "The project url", type = WorkParameterType.URL,
					optional = true) })
	WorkFlow customWorkflow(@Qualifier("customWorkFlowTaskOne") CustomWorkFlowTask customWorkFlowTaskOne,
			@Qualifier("customWorkFlowTaskTwo") CustomWorkFlowTask customWorkFlowTaskTwo) {
		// @formatter:off
        return SequentialFlow
                .Builder.aNewSequentialFlow()
                .named("customWorkflow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
                .execute(customWorkFlowTaskOne)
                .then(customWorkFlowTaskTwo)
                .build();
        // @formatter:on
	}
	// END Custom Sequential Example (WorkflowTasks and Workflow Definitions)

	@Bean
	SimpleWorkFlowCheckerTask simpleCustomCheckerTask() {
		return new SimpleWorkFlowCheckerTask();
	}

	@Bean(name = "simpleWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow simpleWorkFlowChecker(
			@Qualifier("simpleCustomCheckerTask") SimpleWorkFlowCheckerTask simpleCustomCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("simpleWorkFlowChecker")
				.execute(simpleCustomCheckerTask).build();
	}

}
