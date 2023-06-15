package com.redhat.parodos.examples.negative.simple.task;

import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PendingWithAlertMessageWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("FailedWithAlertMessageWorkFlowTask execution should return PENDING");
		return new DefaultWorkReport(WorkStatus.PENDING, workContext, "[link](http://localhost:8080)");
	}

}
