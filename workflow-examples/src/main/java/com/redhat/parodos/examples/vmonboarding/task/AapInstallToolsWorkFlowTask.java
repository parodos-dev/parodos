package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.tasks.ansible.AapGetJobResponseDTO;
import com.redhat.parodos.tasks.ansible.AapLaunchJobWorkFlowTask;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AapInstallToolsWorkFlowTask extends AapLaunchJobWorkFlowTask {

	public AapInstallToolsWorkFlowTask(String aapUrl, String installToolsJobTemplateId, String username,
			String password) {
		super(aapUrl, username, password);
		this.jobTemplateId = installToolsJobTemplateId;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start AapInstallToolsWorkFlowTask...");
		String vmType;
		try {
			vmType = getRequiredParameterValue("VM_TYPE");
		}
		catch (MissingParameterException e) {
			log.error("parameter VM_TYPE was not found");
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		boolean skipChecker = "WINDOWS".equalsIgnoreCase(vmType);
		addParameter("SKIP", String.valueOf(skipChecker));
		return skipChecker ? new DefaultWorkReport(WorkStatus.COMPLETED, workContext) : super.execute(workContext);
	}

	@Override
	protected void responseAction(AapGetJobResponseDTO responseDTO) {
		String jobId = responseDTO.getJobId();
		log.info("Rest call completed, job id: {}", jobId);
		addParameter("TOOLS_JOB_ID", jobId);
	}

}