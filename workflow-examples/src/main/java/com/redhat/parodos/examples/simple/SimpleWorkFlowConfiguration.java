package com.redhat.parodos.examples.simple;

import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;


/**
 * Very simple workflow configurations
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Configuration
public class SimpleWorkFlowConfiguration {

    @Bean(name = "simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
    @Infrastructure
    WorkFlow simpleSequentialWorkFlowTask() {
        return SequentialFlow.Builder.aNewSequentialFlow()
                .named("simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
                .execute(new RestAPIWorkFlowTask())
                .then(new LoggingWorkFlowTask())
                .build();
    }


    @Bean(name = "simpleParallelWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
    @Infrastructure
    WorkFlow simpleParallelWorkFlowTask() {
        return ParallelFlow.Builder
                .aNewParallelFlow()
                .named("simpleParallelWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
                .execute(new LoggingWorkFlowTask(), new LoggingWorkFlowTask(), new LoggingWorkFlowTask())
                .with(Executors.newFixedThreadPool(3))
                .build();
    }

}
