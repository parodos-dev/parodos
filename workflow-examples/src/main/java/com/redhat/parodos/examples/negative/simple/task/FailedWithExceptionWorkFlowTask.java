package com.redhat.parodos.examples.negative.simple.task;

import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FailedWithExceptionWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("FailedWithExceptionWorkFlowTask execution should throw an exception");
		throw new RuntimeException("FailedWithExceptionWorkFlowTask failure");
	}

}
