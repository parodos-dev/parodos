package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that create a serviceNow ticket
 *
 * @author Annel Ketcha (Github: anlukde)
 */

@Slf4j
public class ServiceNowTicketCreationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start ServiceNowTicketCreationWorkFlowTask...");
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

}
