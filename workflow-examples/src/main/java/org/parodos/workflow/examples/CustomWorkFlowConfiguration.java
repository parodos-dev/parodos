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
package org.parodos.workflow.examples;

import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.parodos.workflow.examples.task.CustomWorkFlowTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Very simple workflow configurations in different package from Parodos main class
 *
 * @author Richard Wang (Github: richardW98)
 */

@Configuration
public class CustomWorkFlowConfiguration {

	// START Custom Sequential Example (WorkflowTasks and Workflow Definitions)
	@Bean
	CustomWorkFlowTask customWorkFlowTask() {
		return new CustomWorkFlowTask();
	}

	@Bean(name = "customWorkflow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow customWorkflow(@Qualifier("customWorkFlowTask") CustomWorkFlowTask customWorkFlowTask) {
		// @formatter:off
        return SequentialFlow
                .Builder.aNewSequentialFlow()
                .named("customWorkflow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
                .execute(customWorkFlowTask)
                .build();
        // @formatter:on
	}
	// END Custom Sequential Example (WorkflowTasks and Workflow Definitions)

}
