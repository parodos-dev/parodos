package com.redhat.parodos.examples.escalation.task;

import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * A simple Escalator that does not fail and just logs that its doing something to
 * Escalate when required
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class SimpleTaskOneEscalator extends BaseWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Escalating on the process SimpleTaskOne created. This is taking too long!!");
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
