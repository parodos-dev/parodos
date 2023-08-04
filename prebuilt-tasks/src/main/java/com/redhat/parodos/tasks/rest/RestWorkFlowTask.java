package com.redhat.parodos.tasks.rest;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

@Slf4j
public class RestWorkFlowTask extends BaseWorkFlowTask {

	private RestService restService;

	public RestWorkFlowTask() {
		restService = new RestServiceImpl();
	}

	RestWorkFlowTask(String beanName, RestService restService) {
		this.restService = restService;
		setBeanName(beanName);
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkParameter> params = new LinkedList<>();
		params.add(WorkParameter.builder().key("url").type(WorkParameterType.TEXT).optional(false)
				.description("URL to send request to").build());
		params.add(WorkParameter.builder().key("method").type(WorkParameterType.TEXT).optional(false)
				.description("The HTTP method").build());
		params.add(WorkParameter.builder().key("content").type(WorkParameterType.TEXT).optional(true)
				.description("The content of the HTTP request").build());
		params.add(WorkParameter.builder().key("username").type(WorkParameterType.TEXT).optional(true)
				.description("Username for basic HTTP authentication").build());
		params.add(WorkParameter.builder().key("password").type(WorkParameterType.TEXT).optional(true)
				.description("Password for basic HTTP authentication").build());
		params.add(WorkParameter.builder().key("response-key").type(WorkParameterType.TEXT).optional(true)
				.description("The content of the response will be stored in this key").build());
		return params;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String url = "";
		try {
			url = getRequiredParameterValue("url");
			String method = getRequiredParameterValue("method");

			HttpMethod httpMethod = Arrays.stream(HttpMethod.values())
					.filter(m -> m.name().equals(method.toUpperCase())).findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Invalid HTTP method: " + method));

			ResponseEntity<String> responseEntity = restService.exchange(url, httpMethod,
					buildRequestEntity(workContext));

			if (!responseEntity.getStatusCode().is2xxSuccessful()) {
				throw new RestClientException(
						"Request failed with HTTP status code " + responseEntity.getStatusCodeValue());
			}

			processResponseEntity(workContext, responseEntity);

		}
		catch (MissingParameterException | IllegalArgumentException e) {
			log.error("Rest task failed for url " + url, e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	protected void processResponseEntity(WorkContext workContext, ResponseEntity<String> responseEntity)
			throws RestClientException {
		String responseKey = getOptionalParameterValue("response-key", "");

		if (responseKey.isEmpty()) {
			return;
		}

		workContext.put(responseKey, responseEntity.getBody());
	}

	protected HttpEntity<String> buildRequestEntity(WorkContext workContext) throws RestClientException {
		String content = getOptionalParameterValue("content", "");
		return new HttpEntity<>(content, buildHttpHeaders(workContext));
	}

	protected HttpHeaders buildHttpHeaders(WorkContext workContext) throws RestClientException {
		HttpHeaders httpHeaders = new HttpHeaders();

		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

		String username = getOptionalParameterValue("username", "");
		String password;

		if (!username.isEmpty()) {
			try {
				password = getRequiredParameterValue("password");
			}
			catch (MissingParameterException e) {
				throw new RestClientException("Missing password", e);
			}
			httpHeaders.setBasicAuth(username, password);
		}
		return httpHeaders;
	}

}
