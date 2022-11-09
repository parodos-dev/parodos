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
package com.redhat.parodos.infrastructure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * This is a Sample InfrastructureOption. It represents everything a developer
 * needs to get coding (ie: CI/CD, AD groups, environments, etc). The logic for
 * how this is all provision lives outside of Parodos. The goal of Parodos is to
 * help determine when this stack should be provisioned and stream line the
 * process for the developers hoping to use all these tools
 * 
 * Also included in this configuration is the Workflow to create this option
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Configuration
public class InfrastructureConfig {

	// There will be lots of beans of InfrastructureOption types - names and
	// qualifiers will make sure the right tasks are put into the workflow
	@Bean(name = "awesomeToolStack")
	InfrastructureOption awesomeToolStack() {
		return new InfrastructureOption
				.Builder("Awesome Tool Stack With Correct Permissions And Config", "awesomeToolStackWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.displayName("Managed Environment")
				.addToDetails("Charge Back: .30/hr")
				.addToDetails("Team must supply their own logging solution")
				.addToDetails("SLA: 99.999 (based on Azure's SLA)")
				.addToDetails("24/7 support with a 2 hour response time")
				.addToDetails("After developing with these tools for 1 hour you will feel better about your life")
				.setDescription("Managed Environment complete with CI/CD and configured Pipelines").build();
	}

	// There will be lots of beans of InfrastructureTask type - names and qualifiers
	// will make sure the right tasks are put into the workflow
	@Bean(name = "restApiTask")
	CallCustomRestAPITask restApiTask() {
		return new CallCustomRestAPITask();
	}

	// There will be lots of beans of InfrastructureTask type - names and qualifiers
	// will make sure the right tasks are put into the workflow
	@Bean(name = "anotherTask")
	AnotherTask anotherTask() {
		return new AnotherTask();
	}

	// There will be lots of beans of InfrastructureTask type - names and qualifiers
	// will make sure the right tasks are put into the workflow
	@Bean(name= "awesomeToolStackWorkFlow" +  WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	WorkFlow superAwesomeTechnologyStackWorkflow(@Qualifier("restApiTask") CallCustomRestAPITask restApiTask, @Qualifier("anotherTask") AnotherTask anotherTask) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("AwesomeToolsAndEnvironment_" +  WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(restApiTask)
				.then(anotherTask)
				.build();
	}
}
