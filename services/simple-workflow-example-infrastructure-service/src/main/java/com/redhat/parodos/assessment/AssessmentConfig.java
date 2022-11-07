package com.redhat.parodos.assessment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Sample assessment workflow
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Configuration
public class AssessmentConfig implements AssessmentWorkFlowAware {
	
	//There might be many AssessmentTasks, using names and qualifiers ensure the right assessments under up in the correct workflow
	@Bean(name= "simpleAssessment")
	AssessmentTask simpleAssessment(@Qualifier("awesomeToolStack") InfrastructureOption awesomeToolsOption) {
		return new SimpleAssessment(awesomeToolsOption);
	}

	//There might be many AssessmentTasks, using names and qualifiers ensure the right assessments under up in the correct workflow
	@Bean(name="assessmentWorkFlow" + ASSESSMENT_WORKFLOW)
	WorkFlow assessmentWorkFlow(@Qualifier("simpleAssessment") AssessmentTask  simpleAssessment) {
		return SequentialFlow
				.Builder
				.aNewSequentialFlow()
				.named("MyAssessment_" + ASSESSMENT_WORKFLOW)
				.execute(simpleAssessment)
				.build();
	}
	
}
