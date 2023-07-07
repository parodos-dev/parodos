package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.tasks.ansible.AapGetJobResponseDTO;
import com.redhat.parodos.tasks.ansible.AnsibleCompletionWorkFlowCheckerTask;
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
public class AnsibleVMCreationWorkFlowCheckerTask extends AnsibleCompletionWorkFlowCheckerTask {

	public AnsibleVMCreationWorkFlowCheckerTask(String aapUrl, String username, String password) {
		super(aapUrl, username, password);
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start AnsibleVMCreationWorkFlowCheckerTask ...");
		try {
			jobId = getRequiredParameterValue("JOB_ID");
			log.info("job id: {}", jobId);
		}
		catch (MissingParameterException e) {
			log.error("parameter JOB_ID is not found!");
			return new DefaultWorkReport(WorkStatus.REJECTED, workContext);
		}
		return super.checkWorkFlowStatus(workContext);
	}

	@Override
	protected void responseAction(AapGetJobResponseDTO responseDTO) {
		String ip = responseDTO.getArtifacts().getAzureVmPublicIp();

		addParameter("VM_IP", ip);
		String vmType = null;
		try {
			vmType = getRequiredParameterValue("VM_TYPE");
		}
		catch (MissingParameterException e) {
			log.error("parameter VM_TYPE is not found");
		}

		String message;
		String envVars = responseDTO.getExtraVars().replace("\\", "");
		if ("WINDOWS".equalsIgnoreCase(vmType)) {
			String vmUsername = envVars.split("\"win_admin_user\": \"")[1].split("\"")[0];
			String vmPassword = envVars.split("\"win_admin_password\": \"")[1].split("\"")[0];
			message = String.format(
					"please use the information below to connect to your windows VM: ip: %s, username: %s, password: %s",
					ip, vmUsername, vmPassword);
		}
		else {
			String vmUsername = envVars.split("\"rhel_admin_user\": \"")[1].split("\"")[0];
			message = String.format("please run this cmd to connect to your vm: ssh -i id_rsa %s@%s", vmUsername, ip);
		}
		addParameter("NOTIFICATION_MESSAGE", message);
	}

}