package com.redhat.parodos.examples.integration;

import com.redhat.parodos.sdk.api.ApiClient;
import com.redhat.parodos.sdk.api.ApiException;
import com.redhat.parodos.sdk.api.Configuration;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.model.ProjectRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.workflow.utils.CredUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * UseSDKExample is a dummy class to demonstrate very basic usage of @see
 * workflow-service-sdk.
 *
 * Future PRs will update or delete this class.
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public class UseSDKExample {

	private final String testPrjName = "Test Project Name";

	private final String testPrjDescription = "Test Project Description";

	@Test
	public void simpleSDKUsage() {
		ApiClient defaultClient = Configuration.getDefaultApiClient();

		defaultClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));

		ProjectApi apiInstance = new ProjectApi(defaultClient);
		ProjectRequestDTO projectRequestDTO = new ProjectRequestDTO();
		projectRequestDTO.setName(testPrjName);
		projectRequestDTO.setDescription(testPrjDescription);
		try {
			apiInstance.createProject(projectRequestDTO);
		}
		catch (ApiException e) {
			fail(String.format(
					"Exception when calling ProjectApi#createProject.\nStatus code: %d\n Reason: %s\n Response headers: %s",
					e.getCode(), e.getResponseBody(), e.getResponseHeaders().toString()));
		}

		List<ProjectResponseDTO> projects = null;
		try {
			projects = apiInstance.getProjects();
		}
		catch (ApiException ex) {
			fail("Exception when calling ProjectApi#getProjects");
		}
		assertTrue(projects.size() > 0);
		assertNotNull(projects.stream()
				.filter(prj -> testPrjName.equals(prj.getName()) && testPrjDescription.equals(prj.getDescription()))
				.findAny().orElse(null));
	}

}
