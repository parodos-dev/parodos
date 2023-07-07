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
public class AapCreateVMWorkFlowTask extends AapLaunchJobWorkFlowTask {

	private final String windowsJobTemplateId;

	private final String rhelJobTemplateId;

	public AapCreateVMWorkFlowTask(String aapUrl, String windowsJobTemplateId, String rhelJobTemplateId,
			String username, String password) {
		super(aapUrl, username, password);
		this.windowsJobTemplateId = windowsJobTemplateId;
		this.rhelJobTemplateId = rhelJobTemplateId;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start AapCreateVMWorkFlowTask...");
		String vmType;
		try {
			vmType = getRequiredParameterValue("VM_TYPE");
		}
		catch (MissingParameterException e) {
			log.error("parameter VM_TYPE was not found");
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		jobTemplateId = "WINDOWS".equalsIgnoreCase(vmType) ? windowsJobTemplateId : rhelJobTemplateId;
		addParameter("VM_TYPE", vmType);

		return super.execute(workContext);
	}

	@Override
	protected void responseAction(AapGetJobResponseDTO responseDTO) {
		String jobId = responseDTO.getJobId();
		log.info("Rest call completed, job id: {}", jobId);
		addParameter("JOB_ID", jobId);
	}

}