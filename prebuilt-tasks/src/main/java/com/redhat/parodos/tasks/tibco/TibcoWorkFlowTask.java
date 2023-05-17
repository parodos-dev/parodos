package com.redhat.parodos.tasks.tibco;

import java.util.LinkedList;
import java.util.List;

import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TibcoWorkFlowTask extends BaseWorkFlowTask {

	private final Tibjms service;

	private final String url;

	private final String caFile;

	private final String username;

	private final String password;

	public TibcoWorkFlowTask(String url, String caFile, String username, String password) {
		this(new TibjmsImpl(), url, username, password, caFile);
	}

	TibcoWorkFlowTask(Tibjms service, String url, String caFile, String username, String password) {
		this.service = service;
		this.url = url;
		this.caFile = caFile;
		this.username = username;
		this.password = password;
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkParameter> params = new LinkedList<WorkParameter>();
		params.add(WorkParameter.builder().key("topic").type(WorkParameterType.TEXT).optional(false)
				.description("Topic to send to").build());
		params.add(WorkParameter.builder().key("message").type(WorkParameterType.TEXT).optional(false)
				.description("Message to send to topic").build());
		return params;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		try {
			String topic = getRequiredParameterValue("topic");
			String message = getRequiredParameterValue("message");

			service.sendMessage(url, caFile, username, password, topic, message);

		}
		catch (Exception e) {
			log.error("TIBCO task failed", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
