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
package com.redhat.parodos.examples.continued.complex;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.parodos.examples.simple.LoggingWorkFlowTaskExecution;
import com.redhat.parodos.workflow.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.WorkFlowDefinition;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
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
    //Start Assessment Logic
    //Infrastructure Option for Onboarding
    @Bean(name = "onboardingOption")
    WorkFlowOption onboardingOption() {
        return new WorkFlowOption.Builder("onboardingOption", "onboardingWorkFlowDefinition")
                .displayName("onboardingWorkFlowDefinition")
                .addToDetails("An example of a WorkFlow with Status checks")
                .build();
    }

    //start assessment task
    @Bean(name = "onboardingAssessmentTaskDefinition")
    WorkFlowTaskDefinition onboardingAssessmentTaskDefinition() {
        return WorkFlowTaskDefinition.builder()
                .name("onboardingAssessmentTaskDefinition")
                .description("onboarding Assessment Task")
                .parameters(List.of(
                        WorkFlowTaskParameter.builder()
                                .key("INPUT")
                                .description("Enter some information to use for the Assessment to determine if they can onboard")
                                .optional(false)
                                .type(WorkFlowTaskParameterType.TEXT)
                                .build()))
                .outputs(null)
                .previousTask(null)
                .build();
    }

    @Bean(name = "onboardingAssessmentTaskExecution")
    OnboardingAssessmentTaskExecution onboardingAssessmentTaskExecution(@Qualifier("onboardingAssessmentTaskDefinition") WorkFlowTaskDefinition onboardingAssessmentTaskDefinition, @Qualifier("onboardingOption") WorkFlowOption onboardingOptions) {
        return new OnboardingAssessmentTaskExecution(onboardingOptions, onboardingAssessmentTaskDefinition);
    }
    //end assessment task

    //start assessment workflow
    @Bean(name = "onboardingAssessmentDefinition")
    WorkFlowDefinition onboardingAssessmentDefinition(@Qualifier("onboardingAssessmentTaskDefinition") WorkFlowTaskDefinition onboardingAssessmentTaskDefinition) {
        return WorkFlowDefinition.builder()
                .name("onboardingAssessmentDefinition")
                .description("Onbaoarding assessment workflow")
                .type(WorkFlowType.ASSESSMENT)
                .author("Peter")
                .tasks(List.of(onboardingAssessmentTaskDefinition))
                .createdDate(new Date())
                .modifiedDate(new Date())
                .build();
    }

    @Bean(name = "onboardingAssessmentExecution")
    WorkFlow onboardingAssessmentExecution(@Qualifier("onboardingAssessmentDefinition") WorkFlowDefinition onboardingAssessmentDefinition,
                                           @Qualifier("onboardingAssessmentTaskExecution") OnboardingAssessmentTaskExecution onboardingAssessmentTaskExecution) {
        return SequentialFlow.Builder.aNewSequentialFlow()
                .named(onboardingAssessmentDefinition.getName())
                .execute(onboardingAssessmentTaskExecution)
                .build();
    }
    //end assessment workflow
    //End Assessment Logic

    //Start Onboarding Logic
    //start cert  task
    @Bean(name = "certWorkFlowTaskDefinition")
    WorkFlowTaskDefinition certWorkFlowTaskDefinition() {
        return WorkFlowTaskDefinition.builder()
                .name("certWorkFlowTaskDefinition")
                .description("A cert workflow task")
                .parameters(List.of(WorkFlowTaskParameter.builder()
                        .key("username")
                        .type(WorkFlowTaskParameterType.TEXT)
                        .optional(false)
                        .description("username for cert taask")
                        .build()))
                .build();
    }

    @Bean(name = "certWorkFlowTaskExecution")
    LoggingWorkFlowTaskExecution certWorkFlowTaskExecution(@Qualifier("certWorkFlowTaskDefinition") WorkFlowTaskDefinition certWorkFlowTaskDefinition) {
        return new LoggingWorkFlowTaskExecution(certWorkFlowTaskDefinition);
    }
    //end cert task

    //start adGroup  task
    @Bean(name = "adGroupWorkFlowTaskDefinition")
    WorkFlowTaskDefinition adGroupWorkFlowTaskDefinition(@Qualifier("onboardingWorkFlowCheckDefinition") WorkFlowCheckerDefinition onboardingWorkFlowCheckDefinition) {
        return WorkFlowTaskDefinition.builder()
                .name("adGroupWorkFlowTaskDefinition")
                .description("adGroup workflow task")
                .parameters(List.of(WorkFlowTaskParameter.builder()
                        .key("api-server")
                        .type(WorkFlowTaskParameterType.URL)
                        .optional(false)
                        .description("The adgroup api server to push logs")
                        .build()))
                .workFlowCheckerDefinition(onboardingWorkFlowCheckDefinition)
                .build();
    }

    @Bean(name = "adGroupWorkFlowTaskExecution")
    LoggingWorkFlowTaskExecution adGroupWorkFlowTaskExecution(@Qualifier("adGroupWorkFlowTaskDefinition") WorkFlowTaskDefinition adGroupWorkFlowTaskDefinition) {
        return new LoggingWorkFlowTaskExecution(adGroupWorkFlowTaskDefinition);
    }

    //end adGroup task
    //start dynatrace  task
    @Bean(name = "dynatraceWorkFlowTaskDefinition")
    WorkFlowTaskDefinition dynatraceWorkFlowTaskDefinition(@Qualifier("onboardingWorkFlowCheckDefinition") WorkFlowCheckerDefinition onboardingWorkFlowCheckDefinition) {
        return WorkFlowTaskDefinition.builder()
                .name("dynatraceWorkFlowTaskDefinition")
                .description("dynatrace workflow task")
                .workFlowCheckerDefinition(onboardingWorkFlowCheckDefinition)
                .build();
    }

    @Bean(name = "dynatraceWorkFlowTaskExecution")
    LoggingWorkFlowTaskExecution dynatraceWorkFlowTaskExecution(@Qualifier("dynatraceWorkFlowTaskDefinition") WorkFlowTaskDefinition dynatraceWorkFlowTaskDefinition) {
        return new LoggingWorkFlowTaskExecution(dynatraceWorkFlowTaskDefinition);
    }
    //end dynatrace task

    //start onboarding infrastructure workflow
    @Bean(name = "onboardingWorkFlowDefinition")
    WorkFlowDefinition onboardingWorkFlowDefinition(@Qualifier("certWorkFlowTaskDefinition") WorkFlowTaskDefinition certWorkFlowTaskDefinition, @Qualifier("adGroupWorkFlowTaskDefinition") WorkFlowTaskDefinition adGroupWorkFlowTaskDefinition, @Qualifier("dynatraceWorkFlowTaskDefinition") WorkFlowTaskDefinition dynatraceWorkFlowTaskDefinition) {
        return WorkFlowDefinition.builder()
                .name("onboardingWorkFlowDefinition")
                .description("onboarding parallel workflow test")
                .type(WorkFlowType.INFRASTRUCTURE)
                .author("Peter")
                .tasks(List.of(certWorkFlowTaskDefinition, adGroupWorkFlowTaskDefinition, dynatraceWorkFlowTaskDefinition))
                .createdDate(new Date())
                .modifiedDate(new Date())
                .build();
    }

    @Bean(name = "onboardingWorkFlowExecution")
    WorkFlow onboardingWorkFlowExecution(@Qualifier("onboardingWorkFlowDefinition") WorkFlowDefinition onboardingWorkFlowDefinition,
                                         @Qualifier("certWorkFlowTaskExecution") LoggingWorkFlowTaskExecution certWorkFlowTaskExecution,
                                         @Qualifier("adGroupWorkFlowTaskExecution") LoggingWorkFlowTaskExecution adGroupWorkFlowTaskExecution,
                                         @Qualifier("dynatraceWorkFlowTaskExecution") LoggingWorkFlowTaskExecution dynatraceWorkFlowTaskExecution) {
        return ParallelFlow.Builder.aNewParallelFlow()
                .named(onboardingWorkFlowDefinition.getName())
                .execute(certWorkFlowTaskExecution, adGroupWorkFlowTaskExecution, dynatraceWorkFlowTaskExecution)
                .with(Executors.newFixedThreadPool(3))
                .build();
    }
    //end onboarding infrastructure workflow
    // End Onboarding Logic

    //Start Name Space Logic
    //start nameSpace  task
    @Bean(name = "namespaceWorkFlowTaskDefinition")
    WorkFlowTaskDefinition namespaceWorkFlowTaskDefinition(@Qualifier("namespaceWorkFlowCheckDefinition") WorkFlowCheckerDefinition namespaceWorkFlowCheckDefinition) {
        return WorkFlowTaskDefinition.builder()
                .name("namespaceWorkFlowTaskDefinition")
                .description("namespace workflow task")
                .workFlowCheckerDefinition(namespaceWorkFlowCheckDefinition)
                .build();
    }

    @Bean(name = "namespaceWorkFlowTaskExecution")
    LoggingWorkFlowTaskExecution namespaceWorkFlowTaskExecution(@Qualifier("namespaceWorkFlowTaskDefinition") WorkFlowTaskDefinition namespaceWorkFlowTaskDefinition) {
        return new LoggingWorkFlowTaskExecution(namespaceWorkFlowTaskDefinition);
    }
    //end nameSpace task

    //Start nameSpace workflow
    @Bean(name = "nameSpaceWorkFlowDefinition")
    WorkFlowDefinition nameSpaceWorkFlowDefinition(@Qualifier("namespaceWorkFlowTaskDefinition") WorkFlowTaskDefinition namespaceWorkFlowTaskDefinition) {
        return WorkFlowDefinition.builder()
                .name("nameSpaceWorkFlowDefinition")
                .description("namespace workflow test")
                .type(WorkFlowType.INFRASTRUCTURE)
                .author("Peter")
                .tasks(List.of(namespaceWorkFlowTaskDefinition))
                .createdDate(new Date())
                .modifiedDate(new Date())
                .build();
    }

    @Bean(name = "nameSpaceWorkFlowExecution")
    WorkFlow nameSpaceWorkFlowExecution(@Qualifier("nameSpaceWorkFlowDefinition") WorkFlowDefinition nameSpaceWorkFlowDefinition, @Qualifier("namespaceWorkFlowTaskExecution") LoggingWorkFlowTaskExecution namespaceWorkFlowTaskExecution) {
        return SequentialFlow.Builder.aNewSequentialFlow()
                .named(nameSpaceWorkFlowDefinition.getName())
                .execute(namespaceWorkFlowTaskExecution)
                .build();
    }
    //End nameSpace workflow
    //End Name Space Logic

    //Start Load Balancer Logic
    //start loadBalancer  task
    @Bean(name = "loadBalancerWorkFlowTaskDefinition")
    WorkFlowTaskDefinition loadBalancerWorkFlowTaskDefinition() {
        return WorkFlowTaskDefinition.builder()
                .name("loadBalancerWorkFlowTaskDefinition")
                .description("A loadBalancer workflow task")
                .build();
    }

    @Bean(name = "loadBalancerWorkFlowTaskExecution")
    LoggingWorkFlowTaskExecution loadBalancerWorkFlowTaskExecution(@Qualifier("loadBalancerWorkFlowTaskDefinition") WorkFlowTaskDefinition loadBalancerWorkFlowTaskDefinition) {
        return new LoggingWorkFlowTaskExecution(loadBalancerWorkFlowTaskDefinition);
    }
    //end cert task

    //start cert  task
    @Bean(name = "failOverWorkFlowTaskDefinition")
    WorkFlowTaskDefinition failOverWorkFlowTaskDefinition(@Qualifier("loadBalancerWorkFlowTaskDefinition") WorkFlowTaskDefinition loadBalancerWorkFlowTaskDefinition) {
        WorkFlowTaskDefinition failOverWorkFlowTaskDefinition =  WorkFlowTaskDefinition.builder()
                .name("failOverWorkFlowTaskDefinition")
                .description("failOver workflow task")
                .previousTask(loadBalancerWorkFlowTaskDefinition)
                .build();
        loadBalancerWorkFlowTaskDefinition.setNextTask(failOverWorkFlowTaskDefinition);
        return failOverWorkFlowTaskDefinition;
    }

    @Bean(name = "failOverWorkFlowTaskExecution")
    LoggingWorkFlowTaskExecution failOverWorkFlowTaskExecution(@Qualifier("failOverWorkFlowTaskDefinition") WorkFlowTaskDefinition failOverWorkFlowTaskDefinition) {
        return new LoggingWorkFlowTaskExecution(failOverWorkFlowTaskDefinition);
    }
    //end cert task

    //Start networking Logic
    //Start networking workflow
    @Bean(name = "networkingWorkFlowDefinition")
    WorkFlowDefinition networkingWorkFlowDefinition(@Qualifier("loadBalancerWorkFlowTaskDefinition") WorkFlowTaskDefinition loadBalancerWorkFlowTaskDefinition, @Qualifier("failOverWorkFlowTaskDefinition") WorkFlowTaskDefinition failOverWorkFlowTaskDefinition) {
        return WorkFlowDefinition.builder()
                .name("networkingWorkFlowDefinition")
                .description("networking workflow test")
                .type(WorkFlowType.INFRASTRUCTURE)
                .author("Peter")
                .tasks(List.of(loadBalancerWorkFlowTaskDefinition, failOverWorkFlowTaskDefinition))
                .createdDate(new Date())
                .modifiedDate(new Date())
                .build();
    }

    @Bean(name = "networkingWorkFlowExecution")
    WorkFlow networkingWorkFlowExecution(@Qualifier("networkingWorkFlowDefinition") WorkFlowDefinition networkingWorkFlowDefinition, @Qualifier("loadBalancerWorkFlowTaskExecution") LoggingWorkFlowTaskExecution loadBalancerWorkFlowTaskExecution, @Qualifier("failOverWorkFlowTaskExecution") LoggingWorkFlowTaskExecution failOverWorkFlowTaskExecution) {
        return SequentialFlow.Builder.aNewSequentialFlow()
                .named(networkingWorkFlowDefinition.getName())
                .execute(loadBalancerWorkFlowTaskExecution)
                .then(failOverWorkFlowTaskExecution)
                .build();
    }
    //End networking workflow
    //End networking Logic


    //Start onboardingWorkFlowCheck Logic
    //Start onboardingWorkFlowCheck Task
    @Bean(name = "onboardingWorkFlowCheckTaskDefinition")
    WorkFlowTaskDefinition gateTwo() {
        return WorkFlowTaskDefinition.builder()
                .name("onboardingWorkFlowCheckTaskDefinition")
                .description("onboarding workflow Checker task test")
                .build();
    }

    @Bean(name = "onboardingWorkFlowCheckTaskExecution")
    MockApprovalWorkFlowCheckerTaskExecution onboardingWorkFlowCheckTaskExecution(@Qualifier("onboardingWorkFlowCheckTaskDefinition") WorkFlowTaskDefinition gateTwo) {
        return new MockApprovalWorkFlowCheckerTaskExecution(gateTwo);
    }
    //End onboardingWorkFlowCheck Task

    //Start onboardingWorkFlowCheck Workflow
    @Bean(name = "onboardingWorkFlowCheckDefinition")
    WorkFlowCheckerDefinition onboardingWorkFlowCheckDefinition(@Qualifier("onboardingWorkFlowCheckTaskDefinition") WorkFlowTaskDefinition onboardingWorkFlowCheckTaskDefinition, @Qualifier("nameSpaceWorkFlowDefinition") WorkFlowDefinition namespaceWorkFlowDefinition) {
        return WorkFlowCheckerDefinition.builder()
                .name("onboardingWorkFlowCheckDefinition")
                .description("onboarding workflow checker test")
                .type(WorkFlowType.CHECKER)
                .author("Peter")
                .tasks(List.of(onboardingWorkFlowCheckTaskDefinition))
                .createdDate(new Date())
                .modifiedDate(new Date())
                .cronExpression("0 0/1 * * * ?")
                .nextWorkFlowDefinition(namespaceWorkFlowDefinition)
                .build();
    }

    @Bean(name = "onboardingWorkFlowCheckExecution")
    WorkFlow onboardingWorkFlowCheckExecution(@Qualifier("onboardingWorkFlowCheckDefinition") WorkFlowCheckerDefinition onboardingWorkFlowCheckDefinition, @Qualifier("onboardingWorkFlowCheckTaskExecution") MockApprovalWorkFlowCheckerTaskExecution onboardingWorkFlowCheckTaskExecution) {
        return SequentialFlow.Builder
                .aNewSequentialFlow()
                .named(onboardingWorkFlowCheckDefinition.getName())
                .execute(onboardingWorkFlowCheckTaskExecution)
                .build();
    }
    //End onboardingWorkFlowCheck
    //End onboardingWorkFlowCheck Logic


    //Start namespaceWorkFlowCheck Logic
    //Start namespaceWorkFlowCheck Task
    @Bean(name = "namespaceWorkFlowCheckTaskDefinition")
    WorkFlowTaskDefinition gateThree() {
        return WorkFlowTaskDefinition.builder()
                .name("namespaceWorkFlowCheckTaskDefinition")
                .description("namespace workflow Checker task test")
                .build();
    }

    @Bean(name = "namespaceWorkFlowCheckTaskExecution")
    MockApprovalWorkFlowCheckerTaskExecution namespaceWorkFlowCheckTaskExecution(@Qualifier("namespaceWorkFlowCheckTaskDefinition") WorkFlowTaskDefinition gateThree) {
        return new MockApprovalWorkFlowCheckerTaskExecution(gateThree);
    }

    //End namespaceWorkFlowCheck Task
    //Start namespaceWorkFlowCheck Workflow
    @Bean(name = "namespaceWorkFlowCheckDefinition")
    WorkFlowCheckerDefinition namespaceWorkFlowCheckDefinition(@Qualifier("namespaceWorkFlowCheckTaskDefinition") WorkFlowTaskDefinition namespaceWorkFlowCheckTaskDefinition, @Qualifier("networkingWorkFlowDefinition") WorkFlowDefinition namespaceWorkFlowDefinition) {
        return WorkFlowCheckerDefinition.builder()
                .name("namespaceWorkFlowCheckDefinition")
                .description("namespace workflow checker test")
                .type(WorkFlowType.CHECKER)
                .author("Peter")
                .tasks(List.of(namespaceWorkFlowCheckTaskDefinition))
                .createdDate(new Date())
                .modifiedDate(new Date())
                .cronExpression("0 0/1 * * * ?")
                .nextWorkFlowDefinition(namespaceWorkFlowDefinition)
                .build();
    }

    @Bean(name = "namespaceWorkFlowCheckExecution")
    WorkFlow namespaceWorkFlowCheckExecution(@Qualifier("namespaceWorkFlowCheckDefinition") WorkFlowCheckerDefinition namespaceWorkFlowCheckDefinition, @Qualifier("namespaceWorkFlowCheckTaskExecution") MockApprovalWorkFlowCheckerTaskExecution namespaceWorkFlowCheckTaskExecution) {
        return SequentialFlow.Builder
                .aNewSequentialFlow()
                .named(namespaceWorkFlowCheckDefinition.getName())
                .execute(namespaceWorkFlowCheckTaskExecution)
                .build();
    }
    //End namespaceWorkFlowCheck
    //End namespaceWorkFlowCheck Logic
}
