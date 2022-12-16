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
				.execute(new RestAPIWorkFlowTask())
				.then(new LoggingWorkFlowTask())
				.build();
	}
	
	
	@Bean(name = "simpleParallelWorkFlow_" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	WorkFlow simpleParallelWorkFlowTask() {
		return ParallelFlow.Builder
				.aNewParallelFlow()
				.execute(new LoggingWorkFlowTask(), new LoggingWorkFlowTask(), new LoggingWorkFlowTask())
				.with(Executors.newFixedThreadPool(3))
				.build();
	}

}
