package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.tasks.ansible.AapGetJobResponseDTO;
import com.redhat.parodos.workflow.exception.MissingParameterException;
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
public class AnsibleJobCompletionWorkFlowCheckerTask extends AnsibleVMCreationWorkFlowCheckerTask {

	public AnsibleJobCompletionWorkFlowCheckerTask(String aapUrl, String username, String password) {
		super(aapUrl, username, password);
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start AnsibleJobCompletionWorkFlowCheckerTask ...");
		String skipChecker = null;
		try {
			skipChecker = getRequiredParameterValue("SKIP");
		}
		catch (MissingParameterException e) {
			log.error("parameter SKIP was not found");
		}

		if ("true".equalsIgnoreCase(skipChecker)) {
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}

		try {
			jobId = getRequiredParameterValue("TOOLS_JOB_ID");
			log.info("job id: {}", jobId);
		}
		catch (MissingParameterException e) {
			log.error("parameter TOOLS_JOB_ID is not found!");
			return new DefaultWorkReport(WorkStatus.REJECTED, workContext);
		}
		return super.checkWorkFlowStatus(workContext);
	}

	@Override
	protected void responseAction(AapGetJobResponseDTO responseDTO) {
		String message = "java and tomcat has been installed in your VM!";
		addParameter("NOTIFICATION_MESSAGE", message);
		taskLogger.logInfoWithSlf4j(message);
	}

}