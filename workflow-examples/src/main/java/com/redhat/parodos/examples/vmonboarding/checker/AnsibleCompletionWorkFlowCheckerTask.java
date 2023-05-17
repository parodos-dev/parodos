package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that check for ansible completion
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class AnsibleCompletionWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	public AnsibleCompletionWorkFlowCheckerTask() {
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start AnsibleCompletionWorkFlowCheckerTask...");
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

}
