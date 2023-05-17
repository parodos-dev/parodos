package com.redhat.parodos.tasks.notification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.NotificationMessageApi;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.workflow.exception.MissingParameterException;
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

import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

@Slf4j
public class NotificationWorkFlowTask extends BaseWorkFlowTask {

	private final NotificationMessageApi apiInstance;

	public NotificationWorkFlowTask(String basePath, String auth) {
		this(basePath, null, auth);
	}

	protected NotificationWorkFlowTask(String basePath, NotificationMessageApi apiInstance) {
		this(basePath, apiInstance, null);
	}

	private NotificationWorkFlowTask(String basePath, NotificationMessageApi apiInstance, String auth) {
		if (apiInstance == null) {
			ApiClient apiClient = Configuration.getDefaultApiClient();
			apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, auth);
			apiClient.setBasePath(basePath);
			apiInstance = new NotificationMessageApi(apiClient);
		}
		this.apiInstance = apiInstance;
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkParameter> params = new LinkedList<>();
		params.add(WorkParameter.builder().key("type").type(WorkParameterType.TEXT).optional(false)
				.description("Message Type").build());
		params.add(WorkParameter.builder().key("body").type(WorkParameterType.TEXT).optional(false)
				.description("Message Body").build());
		params.add(WorkParameter.builder().key("subject").type(WorkParameterType.TEXT).optional(false)
				.description("Message Subject").build());
		// TODO Add an option for List parameter
		params.add(WorkParameter.builder().key("userNames").type(WorkParameterType.TEXT).optional(true)
				.description("Comma separated list of user names").build());
		params.add(WorkParameter.builder().key("groupNames").type(WorkParameterType.TEXT).optional(true)
				.description("Comma separated list of group names").build());
		return params;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return super.getWorkFlowTaskOutputs();
	}

	@Override
	public HashMap<String, Map<String, Object>> getAsJsonSchema() {
		return super.getAsJsonSchema();
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = new NotificationMessageCreateRequestDTO();

		try {
			notificationMessageCreateRequestDTO.messageType(getRequiredParameterValue("type"));
			notificationMessageCreateRequestDTO.body(getRequiredParameterValue("body"));
			notificationMessageCreateRequestDTO.subject(getRequiredParameterValue("subject"));
			List<String> userNames = toList(getOptionalParameterValue("userNames", null));
			List<String> groupNames = toList(getOptionalParameterValue("groupNames", null));
			if (CollectionUtils.isEmpty(userNames) && CollectionUtils.isEmpty(groupNames)) {
				throw new MissingParameterException("User Names or Group Names must be provided");
			}
			notificationMessageCreateRequestDTO.usernames(userNames);
			notificationMessageCreateRequestDTO.groupNames(groupNames);
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter:", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		try {
			this.apiInstance.create(notificationMessageCreateRequestDTO);
		}
		catch (ApiException e) {
			log.error("Exception when calling NotificationMessageApi#create:", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private List<String> toList(String str) {
		if (str == null) {
			return null;
		}
		return Arrays.asList(str.split("\\s*;\\s*"));
	}

}
