package com.redhat.parodos.examples.integration;

import com.redhat.parodos.openapi.api.ApiClient;
import com.redhat.parodos.openapi.api.ApiException;
import com.redhat.parodos.openapi.api.Configuration;
import com.redhat.parodos.openapi.api.ProjectApi;
import com.redhat.parodos.openapi.model.ProjectRequestDTOGenerated;
import com.redhat.parodos.openapi.model.ProjectResponseDTOGenerated;
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
		ProjectRequestDTOGenerated projectRequestDTOGenerated = new ProjectRequestDTOGenerated();
		projectRequestDTOGenerated.setName(testPrjName);
		projectRequestDTOGenerated.setDescription(testPrjDescription);
		try {
			apiInstance.createProject(projectRequestDTOGenerated);
		}
		catch (ApiException e) {
			fail(String.format(
					"Exception when calling ProjectApi#createProject.\nStatus code: %d\n Reason: %s\n Response headers: %s",
					e.getCode(), e.getResponseBody(), e.getResponseHeaders().toString()));
		}

		List<ProjectResponseDTOGenerated> projects = null;
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
