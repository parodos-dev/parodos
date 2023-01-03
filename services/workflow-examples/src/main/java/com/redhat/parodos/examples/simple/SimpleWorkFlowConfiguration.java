package com.redhat.parodos.examples.simple;

import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
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
	
	@Bean(name = "simpleSequentialWorkFlow_" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	WorkFlow simpleSequentialWorkFlowTask() {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("simpleSequentialWorkFlow_" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(new RestAPIWorkFlowTask("restAPIWorkFlowTask"))
				.then(new LoggingWorkFlowTask("LoggingWorkFlowTask"))
				.build();
	}
	
	
	@Bean(name = "simpleParallelWorkFlow_" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	WorkFlow simpleParallelWorkFlowTask() {
		return ParallelFlow.Builder
				.aNewParallelFlow()
				.named("simpleParallelWorkFlow_" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(new LoggingWorkFlowTask("LoggingWorkFlowTask1"), new LoggingWorkFlowTask("LoggingWorkFlowTask2"), new LoggingWorkFlowTask("LoggingWorkFlowTask3"))
				.with(Executors.newFixedThreadPool(3))
				.build();
	}

}
