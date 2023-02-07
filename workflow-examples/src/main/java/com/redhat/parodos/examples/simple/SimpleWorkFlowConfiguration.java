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

import com.redhat.parodos.workflow.WorkFlowType;
//import com.redhat.parodos.workflow.annotation.Author;
import com.redhat.parodos.workflow.annotation.Infrastructure;
//import com.redhat.parodos.workflow.annotation.WorkFlowDefinition;
//import com.redhat.parodos.workflow.annotation.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.task.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Very simple workflow configurations
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Configuration
public class SimpleWorkFlowConfiguration {

    //Task 1
//    @Bean(name = "restAPIWorkFlowTaskDefinition")
//	@WorkFlowTaskDefinition(name = "restAPIWorkFlowTaskDefinition",
//			description = "A rest api workflow task definition",
//			parameters = { @com.redhat.parodos.workflow.annotation.WorkFlowTaskParameter(key = "username",
//					description = "The username",
//					optional = false,
//					type = WorkFlowTaskParameterType.TEXT) },
//			outputs = { WorkFlowTaskOutput.HTTP2XX, WorkFlowTaskOutput.EXCEPTION}
//	)
//    InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition() {
//        return InfrastructureTaskDefinition.builder()
//                .name("restAPIWorkFlowTaskDefinition")
//                .description("A rest api workflow task test")
//                .parameters(List.of(WorkFlowTaskParameter.builder()
//                        .key("username")
//                        .type(WorkFlowTaskParameterType.TEXT)
//                        .optional(false)
//                        .description("The username of the requester")
//                        .build()))
//                .outputs(null)
//                .previousTask(null)
//                .build();
//    }
//
    @Bean
    RestAPIWorkFlowTask restAPIWorkFlowTask() {
        return new RestAPIWorkFlowTask();
    }
//
//    //Task 2
//    @Bean(name = "loggingWorkFlowTaskDefinition")
//    InfrastructureTaskDefinition loggingWorkFlowTaskDefinition(@Qualifier("restAPIWorkFlowTaskDefinition") InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition, @Qualifier("simpleWorkFlowCheckerDefinition") WorkFlowCheckerDefinition simpleWorkFlowCheckerDefinition) {
//        InfrastructureTaskDefinition loggingWorkFlowTaskDefinition = InfrastructureTaskDefinition.builder()
//                .name("loggingWorkFlowTaskDefinition")
//                .description("A logging workflow task test")
//                .parameters(List.of(WorkFlowTaskParameter.builder()
//                        .key("api-server")
//                        .type(WorkFlowTaskParameterType.URL)
//                        .optional(false)
//                        .description("The api server to push logs")
//                        .build()))
//                .outputs(null)
//                .previousTask(restAPIWorkFlowTaskDefinition)
//                .nextTask(null)
//                .workFlowCheckerDefinition(simpleWorkFlowCheckerDefinition)
//                .build();
//        restAPIWorkFlowTaskDefinition.setNextTask(loggingWorkFlowTaskDefinition);
//        return loggingWorkFlowTaskDefinition;
//    }

    @Bean
    LoggingWorkFlowTask loggingWorkFlowTask() {
        return new LoggingWorkFlowTask();
    }

    @Bean
    @Infrastructure
    WorkFlow simpleSequentialWorkFlow(RestAPIWorkFlowTask restAPIWorkFlowTask, LoggingWorkFlowTask loggingWorkFlowTask) {
        return SequentialFlow.Builder.aNewSequentialFlow()
                .named("simpleSequentialWorkFlow")
                .execute(restAPIWorkFlowTask)
                .then(loggingWorkFlowTask)
                .build();
    }

    //Start simpleWorkFlowChecker Task
//    @Bean(name = "simpleWorkFlowCheckerTaskDefinition")
//    WorkFlowCheckerTaskDefinition simpleWorkFlowCheckerTaskDefinition() {
//        return WorkFlowCheckerTaskDefinition.builder()
//                .name("simpleWorkFlowCheckerTaskDefinition")
//                .description("A  workflow Checker task test")
//                .build();
//    }

//    @Bean(name = "simpleWorkFlowCheckerTaskExecution")
//    SimpleWorkFlowCheckerTaskExecution simpleWorkFlowCheckerTaskExecution(@Qualifier("simpleWorkFlowCheckerTaskDefinition") WorkFlowCheckerTaskDefinition simpleWorkFlowCheckerTaskDefinition) {
//        return new SimpleWorkFlowCheckerTaskExecution(simpleWorkFlowCheckerTaskDefinition);
//    }
    //End simpleWorkFlowChecker Task

    //Start simpleWorkFlowChecker Workflow
//    @Bean(name = "simpleWorkFlowCheckerDefinition")
//    WorkFlowCheckerDefinition simpleWorkFlowCheckerDefinition(@Qualifier("simpleWorkFlowCheckerTaskDefinition") WorkFlowCheckerTaskDefinition simpleWorkFlowCheckerTaskDefinition, @Qualifier("simpleNextSequentialWorkFlowDefinition") WorkFlowDefinition simpleNextSequentialWorkFlowDefinition) {
//        return WorkFlowCheckerDefinition.builder()
//                .name("simpleWorkFlowCheckerDefinition")
//                .description("A simple workflow checker test")
//                .type(WorkFlowType.CHECKER)
//                .author("Peter")
//                .tasks(List.of(simpleWorkFlowCheckerTaskDefinition))
//                .createdDate(new Date())
//                .modifiedDate(new Date())
//                .cronExpression("0 0/1 * * * ?")
//                .nextWorkFlowDefinition(simpleNextSequentialWorkFlowDefinition)
//                .build();
//    }

//    @Bean(name = "simpleWorkFlowCheckerExecution")
//    @com.redhat.parodos.workflow.annotation.WorkFlowDefinition(name = "simpleWorkFlowCheckerExecution",
//            description = "A simple WorkFlow Checker test",
//            type = WorkFlowType.INFRASTRUCTURE,
//            author = "Peter Paul"
//    )
//    WorkFlow simpleWorkFlowCheckerExecution(@Qualifier("simpleWorkFlowCheckerDefinition") WorkFlowCheckerDefinition simpleWorkFlowCheckerDefinition, @Qualifier("simpleWorkFlowCheckerTaskExecution") SimpleWorkFlowCheckerTaskExecution simpleWorkFlowCheckerTaskExecution) {
//        return SequentialFlow.Builder
//                .aNewSequentialFlow()
//                .named(simpleWorkFlowCheckerDefinition.getName())
//                .execute(simpleWorkFlowCheckerTaskExecution)
//                .build();
//    }
    //End simpleWorkFlowChecker

    //Simple workflow
//    @Bean(name = "simpleSequentialWorkFlowDefinition")
//    WorkFlowDefinition simpleSequentialWorkFlowDefinition(@Qualifier("loggingWorkFlowTaskDefinition") InfrastructureTaskDefinition loggingWorkFlowTaskDefinition,
//                                                          @Qualifier("restAPIWorkFlowTaskDefinition") InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition) {
//        return WorkFlowDefinition.builder()
//                .name("simpleSequentialWorkFlowDefinition")
//                .description("A simple sequential workflow test")
//                .type(WorkFlowType.INFRASTRUCTURE)
//                .author("Peter")
//                .tasks(List.of(restAPIWorkFlowTaskDefinition, loggingWorkFlowTaskDefinition))
//                .createdDate(new Date())
//                .modifiedDate(new Date())
//                .build();
//    }
//
    //End simple sequential workflow

    //Simple next workflow
//    @Bean(name = "simpleNextSequentialWorkFlowDefinition")
//    WorkFlowDefinition simpleNextSequentialWorkFlowDefinition(
//            @Qualifier("restAPIWorkFlowTaskDefinition") InfrastructureTaskDefinition restAPIWorkFlowTaskDefinition) {
//        return WorkFlowDefinition.builder()
//                .name("simpleNextSequentialWorkFlowDefinition")
//                .description("A simple sequential workflow test")
//                .type(WorkFlowType.INFRASTRUCTURE)
//                .author("Peter")
//                .tasks(List.of(restAPIWorkFlowTaskDefinition))
//                .createdDate(new Date())
//                .modifiedDate(new Date())
//                .build();
//    }

//    @Bean(name = "simpleNextSequentialWorkFlowExecution")
//    @com.redhat.parodos.workflow.annotation.WorkFlowDefinition(name = "simpleNextSequentialWorkFlowExecution",
//            description = "A simple next sequential workflow test",
//            type = WorkFlowType.INFRASTRUCTURE,
//            author = "John Doe",
//            tasks = {
//                    @com.redhat.parodos.workflow.annotation.WorkFlowTaskDefinition(name = "task1",
//							description = "task 1 desc",
//							parameters = { @com.redhat.parodos.workflow.annotation.WorkFlowTaskParameter(key = "username",
//									description = "user name",
//									optional = false,
//									type = WorkFlowTaskParameterType.TEXT) },
//							outputs = { WorkFlowTaskOutput.HTTP2XX, WorkFlowTaskOutput.EXCEPTION}
//					),
//                    @com.redhat.parodos.workflow.annotation.WorkFlowTaskDefinition(name = "task2",
//							description = "task 2 desc",
//							parameters = {@com.redhat.parodos.workflow.annotation.WorkFlowTaskParameter(key = "apiserver",
//									description = "api server url",
//									optional = false,
//									type = WorkFlowTaskParameterType.URL)},
//							outputs = {WorkFlowTaskOutput.HTTP2XX, WorkFlowTaskOutput.EXCEPTION})
//            }
//    )
//    WorkFlow simpleNextSequentialWorkFlowExecution(@Qualifier("simpleNextSequentialWorkFlowDefinition") WorkFlowDefinition simpleNextSequentialWorkFlowDefinition,
//                                                   @Qualifier("restAPIWorkFlowTaskExecution") RestAPIWorkFlowTaskExecution restAPIWorkFlowTaskExecution) {
//        return SequentialFlow.Builder.aNewSequentialFlow()
//                .named(simpleNextSequentialWorkFlowDefinition.getName())
//                .execute(restAPIWorkFlowTaskExecution)
//                .build();
//    }
}