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
package com.redhat.parodos.examples.simple;

import java.util.Date;
import java.util.List;

import com.redhat.parodos.workflow.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.WorkFlowDefinition;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.task.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.task.checker.WorkFlowCheckerTaskDefinition;
import com.redhat.parodos.workflow.task.infrastructure.InfrastructureTaskDefinition;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;


/**
 * Very simple workflow configurations
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Configuration
public class SimpleWorkFlowConfiguration {

	//Task 1
	@Bean(name = "restAPIWorkFlowTaskDefinition")
	InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition() {
		return InfrastructureTaskDefinition.builder()
				.name("restAPIWorkFlowTaskDefinition")
				.description("A rest api workflow task test")
				.parameters(List.of(WorkFlowTaskParameter.builder()
						.key("username")
						.type(WorkFlowTaskParameterType.TEXT)
						.optional(false)
						.description("The username of the requester")
						.build()))
				.outputs(null)
				.previousTask(null)
				.build();
	}

	@Bean(name = "restAPIWorkFlowTaskExecution")
	RestAPIWorkFlowTaskExecution restAPIWorkFlowTaskExecution(@Qualifier("restAPIWorkFlowTaskDefinition") WorkFlowTaskDefinition restAPIWorkFlowTaskDefinition) {
		return new RestAPIWorkFlowTaskExecution(restAPIWorkFlowTaskDefinition);
	}

	//Task 2
	@Bean(name = "loggingWorkFlowTaskDefinition")
	InfrastructureTaskDefinition loggingWorkFlowTaskDefinition(@Qualifier("restAPIWorkFlowTaskDefinition") InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition, @Qualifier("simpleWorkFlowCheckerDefinition") WorkFlowCheckerDefinition simpleWorkFlowCheckerDefinition) {
		InfrastructureTaskDefinition loggingWorkFlowTaskDefinition = InfrastructureTaskDefinition.builder()
				.name("loggingWorkFlowTaskDefinition")
				.description("A logging workflow task test")
				.parameters(List.of(WorkFlowTaskParameter.builder()
						.key("api-server")
						.type(WorkFlowTaskParameterType.URL)
						.optional(false)
						.description("The api server to push logs")
						.build()))
				.outputs(null)
				.previousTask(restAPIWorkFlowTaskDefinition)
				.nextTask(null)
				.workFlowCheckerDefinition(simpleWorkFlowCheckerDefinition)
				.build();
		restAPIWorkFlowTaskDefinition.setNextTask(loggingWorkFlowTaskDefinition);
		return loggingWorkFlowTaskDefinition;
	}

	@Bean(name = "loggingWorkFlowTaskExecution")
	LoggingWorkFlowTaskExecution loggingWorkFlowTaskExecution(@Qualifier("loggingWorkFlowTaskDefinition") WorkFlowTaskDefinition loggingWorkFlowTaskDefinition) {
		return new LoggingWorkFlowTaskExecution(loggingWorkFlowTaskDefinition);
	}

	//Start simpleWorkFlowChecker Task
	@Bean(name = "simpleWorkFlowCheckerTaskDefinition")
	WorkFlowCheckerTaskDefinition simpleWorkFlowCheckerTaskDefinition() {
		return WorkFlowCheckerTaskDefinition.builder()
				.name("simpleWorkFlowCheckerTaskDefinition")
				.description("A  workflow Checker task test")
				.build();
	}

    @Bean(name = "simpleWorkFlowCheckerTaskExecution")
	SimpleWorkFlowCheckerTaskExecution simpleWorkFlowCheckerTaskExecution(@Qualifier("simpleWorkFlowCheckerTaskDefinition") WorkFlowCheckerTaskDefinition simpleWorkFlowCheckerTaskDefinition) {
    	return new SimpleWorkFlowCheckerTaskExecution(simpleWorkFlowCheckerTaskDefinition);
    }
	//End simpleWorkFlowChecker Task

	//Start simpleWorkFlowChecker Workflow
	@Bean(name = "simpleWorkFlowCheckerDefinition")
	WorkFlowCheckerDefinition simpleWorkFlowCheckerDefinition(@Qualifier("simpleWorkFlowCheckerTaskDefinition") WorkFlowCheckerTaskDefinition simpleWorkFlowCheckerTaskDefinition, @Qualifier("simpleNextSequentialWorkFlowDefinition") WorkFlowDefinition simpleNextSequentialWorkFlowDefinition){
		return WorkFlowCheckerDefinition.builder()
				.name("simpleWorkFlowCheckerDefinition")
				.description("A simple workflow checker test")
				.type(WorkFlowType.CHECKER)
				.author("Peter")
				.tasks(List.of(simpleWorkFlowCheckerTaskDefinition))
				.createdDate(new Date())
				.modifiedDate(new Date())
				.cronExpression("0 0/1 * * * ?")
				.nextWorkFlowDefinition(simpleNextSequentialWorkFlowDefinition)
				.build();
	}

    @Bean(name = "simpleWorkFlowCheckerExecution")
    WorkFlow simpleWorkFlowCheckerExecution(@Qualifier("simpleWorkFlowCheckerDefinition") WorkFlowCheckerDefinition simpleWorkFlowCheckerDefinition, @Qualifier("simpleWorkFlowCheckerTaskExecution") SimpleWorkFlowCheckerTaskExecution simpleWorkFlowCheckerTaskExecution) {
        return SequentialFlow.Builder
        		.aNewSequentialFlow()
        		.named(simpleWorkFlowCheckerDefinition.getName())
        		.execute(simpleWorkFlowCheckerTaskExecution)
        		.build();
    }
    //End simpleWorkFlowChecker

	//Simple workflow
	@Bean(name = "simpleSequentialWorkFlowDefinition")
	WorkFlowDefinition simpleSequentialWorkFlowDefinition(@Qualifier("loggingWorkFlowTaskDefinition") InfrastructureTaskDefinition loggingWorkFlowTaskDefinition,
														  @Qualifier("restAPIWorkFlowTaskDefinition") InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition){
		return WorkFlowDefinition.builder()
				.name("simpleSequentialWorkFlowDefinition")
				.description("A simple sequential workflow test")
				.type(WorkFlowType.INFRASTRUCTURE)
				.author("Peter")
				.tasks(List.of(restAPIWorkFlowTaskDefinition, loggingWorkFlowTaskDefinition))
				.createdDate(new Date())
				.modifiedDate(new Date())
				.build();
	}

	@Bean(name = "simpleSequentialWorkFlowExecution")
	WorkFlow simpleSequentialWorkFlowExecution(@Qualifier("simpleSequentialWorkFlowDefinition") WorkFlowDefinition simpleSequentialWorkFlowDefinition,
														@Qualifier("restAPIWorkFlowTaskExecution") RestAPIWorkFlowTaskExecution restAPIWorkFlowTaskExecution,
														@Qualifier("loggingWorkFlowTaskExecution") LoggingWorkFlowTaskExecution loggingWorkFlowTaskExecution){
		return SequentialFlow.Builder.aNewSequentialFlow()
                .named(simpleSequentialWorkFlowDefinition.getName())
                .execute(restAPIWorkFlowTaskExecution)
                .then(loggingWorkFlowTaskExecution)
                .build();
	}
	//End simple sequential workflow

	//Simple next workflow
	@Bean(name = "simpleNextSequentialWorkFlowDefinition")
	WorkFlowDefinition simpleNextSequentialWorkFlowDefinition(
			@Qualifier("restAPIWorkFlowTaskDefinition") InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition){
		return WorkFlowDefinition.builder()
				.name("simpleNextSequentialWorkFlowDefinition")
				.description("A simple sequential workflow test")
				.type(WorkFlowType.INFRASTRUCTURE)
				.author("Peter")
				.tasks(List.of(restAPIWorkFlowTaskDefinition))
				.createdDate(new Date())
				.modifiedDate(new Date())
				.build();
	}

	@Bean(name = "simpleNextSequentialWorkFlowExecution")
	WorkFlow simpleNextSequentialWorkFlowExecution(@Qualifier("simpleNextSequentialWorkFlowDefinition") WorkFlowDefinition simpleNextSequentialWorkFlowDefinition,
											   @Qualifier("restAPIWorkFlowTaskExecution") RestAPIWorkFlowTaskExecution restAPIWorkFlowTaskExecution){
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named(simpleNextSequentialWorkFlowDefinition.getName())
				.execute(restAPIWorkFlowTaskExecution)
				.build();
	}
}