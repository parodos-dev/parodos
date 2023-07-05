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
package com.redhat.parodos.examples.fallback;

import com.redhat.parodos.examples.complex.fallback.FallbackWorkFlowTask;
import com.redhat.parodos.examples.fallback.task.SimpleFailedWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.WorkFlowProperties;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Very simple workflow configurations
 *
 * @author Luke Shannon (Github: lshannon)
 */

@Configuration
@Slf4j
@PropertySource("classpath:git.properties")
public class SimpleFallbackWorkFlowConfiguration {

	// START Sequential Example (WorkflowTasks and Workflow Definitions)
	@Bean
	SimpleFailedWorkFlowTask simpleFailedWorkFlowTask() {
		return new SimpleFailedWorkFlowTask();
	}

	@Bean(name = "simpleFailedWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure(fallbackWorkflow = "simpleFallbackWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@WorkFlowProperties(version = "${git.commit.id}")
	WorkFlow simpleFailedWorkFlow(
			@Qualifier("simpleFailedWorkFlowTask") SimpleFailedWorkFlowTask simpleFailedWorkFlowTask) {
		// @formatter:off
		return SequentialFlow
				.Builder.aNewSequentialFlow()
				.named("simpleFailedWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(simpleFailedWorkFlowTask)
				.build();
		// @formatter:on
	}

	// END Sequential Example (WorkflowTasks and Workflow Definitions)

	@Bean
	FallbackWorkFlowTask fallbackWorkFlowTask() {
		return new FallbackWorkFlowTask();
	}

	@Bean("simpleFallbackWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow simpleFallbackWorkFlow(@Qualifier("fallbackWorkFlowTask") FallbackWorkFlowTask fallbackWorkFlowTask) {
		// @formatter:off
		return SequentialFlow
				.Builder.aNewSequentialFlow()
				.named("simpleFallbackWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(fallbackWorkFlowTask)
				.build();
		// @formatter:on
	}

}
