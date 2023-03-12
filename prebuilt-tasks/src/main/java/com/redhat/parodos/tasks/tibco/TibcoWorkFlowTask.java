package com.redhat.parodos.tasks.tibco;

import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

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
	public @NonNull List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkFlowTaskParameter> params = new LinkedList<WorkFlowTaskParameter>();
		params.add(WorkFlowTaskParameter.builder().key("topic").type(WorkFlowTaskParameterType.TEXT).optional(false)
				.description("Topic to send to").build());
		params.add(WorkFlowTaskParameter.builder().key("message").type(WorkFlowTaskParameterType.TEXT).optional(false)
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
			String topic = getParameterValue(workContext, "topic");
			String message = getParameterValue(workContext, "message");

			service.sendMessage(url, caFile, username, password, topic, message);

		}
		catch (Exception e) {
			log.error("TIBCO task failed", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
