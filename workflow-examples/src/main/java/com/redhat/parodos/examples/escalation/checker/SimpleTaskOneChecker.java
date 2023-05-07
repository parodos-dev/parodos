package com.redhat.parodos.examples.escalation.checker;

import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple WorkflowChecker that will fail 3 times and then succeed every time after
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class SimpleTaskOneChecker extends BaseWorkFlowCheckerTask {

	private int failCount = 0;

	public SimpleTaskOneChecker(WorkFlow simpleTaskOneEscalatorWorkflow, long sla) {
		super(simpleTaskOneEscalatorWorkflow, sla);
	}

	@Override
	protected WorkReport checkWorkFlowStatus(WorkContext context) {
		if (failCount == 3) {
			log.info("The check of the status SimpleTaskOne has initiated has succeeded");
			return new DefaultWorkReport(WorkStatus.COMPLETED, context);
		}
		failCount++;
		log.info("The check of the status SimpleTaskOne has initiated has failed");
		return new DefaultWorkReport(WorkStatus.FAILED, context);
	}

}
