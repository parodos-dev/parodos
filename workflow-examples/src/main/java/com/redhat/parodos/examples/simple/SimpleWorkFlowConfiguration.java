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
package com.redhat.parodos.examples.simple;

import java.util.concurrent.Executors;

import com.redhat.parodos.examples.simple.task.LoggingWorkFlowTask;
import com.redhat.parodos.examples.simple.task.RestAPIWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.WorkFlowProperties;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
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
public class SimpleWorkFlowConfiguration {

	// START Sequential Example (WorkflowTasks and Workflow Definitions)
	@Bean
	RestAPIWorkFlowTask restCallTask() {
		return new RestAPIWorkFlowTask();
	}

	@Bean
	LoggingWorkFlowTask loggingTask() {
		return new LoggingWorkFlowTask();
	}

	@Bean(name = "simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	@WorkFlowProperties(version = "${git.commit.id}")
	WorkFlow simpleSequentialWorkFlowTask(@Qualifier("restCallTask") RestAPIWorkFlowTask restCallTask,
			@Qualifier("loggingTask") LoggingWorkFlowTask loggingTask) {
		// @formatter:off
		return SequentialFlow
				.Builder.aNewSequentialFlow()
				.named("simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(restCallTask)
				.then(loggingTask)
				.build();
		// @formatter:on
	}

	// END Sequential Example (WorkflowTasks and Workflow Definitions)

	// START Parallel Example (WorkflowTasks and Workflow Definitions)
	@Bean
	LoggingWorkFlowTask simpleParallelTask1() {
		return new LoggingWorkFlowTask();
	}

	@Bean
	LoggingWorkFlowTask simpleParallelTask2() {
		return new LoggingWorkFlowTask();
	}

	@Bean
	LoggingWorkFlowTask simpleParallelTask3() {
		return new LoggingWorkFlowTask();
	}

	@Bean(name = "simpleParallelWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow simpleParallelWorkFlowTask(@Qualifier("simpleParallelTask1") LoggingWorkFlowTask simpleParallelTask1,
			@Qualifier("simpleParallelTask2") LoggingWorkFlowTask simpleParallelTask2,
			@Qualifier("simpleParallelTask3") LoggingWorkFlowTask simpleParallelTask3) {
		// @formatter:off
		return ParallelFlow
				.Builder.aNewParallelFlow()
				.named("simple Parallel WorkFlow")
				.execute(simpleParallelTask1, simpleParallelTask2, simpleParallelTask3)
				.with(Executors.newFixedThreadPool(3))
				.build();
		// @formatter:on
	}
	// END Parallel Example (WorkflowTasks and Workflow Definitions)

}
