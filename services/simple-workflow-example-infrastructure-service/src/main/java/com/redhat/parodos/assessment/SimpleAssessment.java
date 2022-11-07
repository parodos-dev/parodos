package com.redhat.parodos.assessment;

import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.infrastructure.option.InfrastructureOptions;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * This is an Assessment to determine if an application is suitable for a 
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class SimpleAssessment implements AssessmentTask, AssessmentWorkFlowAware {
	
	//We only have one InfrastructureOption to recommend - but this could also be a list or a Map of Options
	private final InfrastructureOption myOption;
	
	public SimpleAssessment(InfrastructureOption myOption) {
		this.myOption = myOption;
	}

	//This will return the same InfrastructureOptions
	public WorkReport execute(WorkContext workContext) {
		log.info("This is my assessment - it always recommends the InfrastructureOption 'Awesome Tool Stack With Correct Permissions And Config'");
		log.info("Here is the arguments for the assessment: {}", workContext.get(ASSESSMENT_REQUEST));
		log.info("Putting the recommended InfrastructureOption in the InfrastructureOptions wrapper and placing it in the WorkContext");
		workContext.put(RESULTING_INFRASTRUCTURE_OPTIONS, 
				new InfrastructureOptions.Builder()
				.addNewOption(myOption)
				.build());
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}
	
}