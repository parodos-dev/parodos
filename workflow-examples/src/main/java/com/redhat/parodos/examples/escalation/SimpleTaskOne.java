package com.redhat.parodos.examples.escalation;

import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Basic task that can be used to test out a Checker
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Gloria Ciavarrini (Github: gciavarrini)
 *
 */
@Slf4j
public class SimpleTaskOne extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("SimpleTaskOne: I trigger a long running process that needs to be checked on");
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
