package com.redhat.parodos.examples.escalation;

import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Basic Tasks to show how something executes in a Workflow after the checker has
 * succeeded
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Gloria Ciavarrini (Github: gciavarrini)
 *
 */
@Slf4j
public class SimpleTaskTwo extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Finishing the final task in the Workflow. Thanks to Escalation, we got it done faster");
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
