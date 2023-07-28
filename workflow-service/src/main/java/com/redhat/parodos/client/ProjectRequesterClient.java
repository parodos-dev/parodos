package com.redhat.parodos.client;

import java.util.Base64;
import java.util.UUID;

import com.redhat.parodos.infrastructure.ProjectRequester;
import com.redhat.parodos.sdk.api.ProjectAccessApi;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.AccessRequestDTO;
import com.redhat.parodos.sdk.model.AccessResponseDTO;
import com.redhat.parodos.sdk.model.AccessStatusResponseDTO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProjectRequesterClient implements ProjectRequester {

	private final ProjectApi projectApi;

	private final ProjectAccessApi projectAccessApi;

	public ProjectRequesterClient(@Value("${workflow.url:test}") String url,
			@Value("${workflow.auth.basic.user:test}") String user,
			@Value("${workflow.auth.basic.password:test}") String password) {
		ApiClient apiClient = new ApiClient().setBasePath(url).addDefaultHeader(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes()));
		projectApi = new ProjectApi(apiClient);
		projectAccessApi = new ProjectAccessApi(apiClient);
	}

	@Override
	public String getBasePath() {
		return projectApi.getApiClient().getBasePath();
	}

	@Override
	public AccessResponseDTO createAccess(UUID id, AccessRequestDTO accessRequestDTO) throws ApiException {
		return projectApi.createAccessRequestToProject(id, accessRequestDTO);
	}

	@Override
	public AccessStatusResponseDTO getAccessStatus(UUID id) throws ApiException {
		return projectAccessApi.getProjectAccessStatus(id);
	}

}
