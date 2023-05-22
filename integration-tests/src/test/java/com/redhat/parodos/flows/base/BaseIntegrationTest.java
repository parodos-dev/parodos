package com.redhat.parodos.flows.base;

import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdkutils.SdkUtils;
import org.junit.Before;

import static com.redhat.parodos.sdkutils.SdkUtils.getParodosAPiClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public class BaseIntegrationTest {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

	protected ApiClient apiClient;

	protected ProjectResponseDTO testProject;

	@Before
	public void setUp() throws ApiException, InterruptedException {
		apiClient = getParodosAPiClient();
		initProject();
	}

	private void initProject() throws ApiException {
		this.testProject = SdkUtils.getProjectAsync(apiClient, projectName, projectDescription);
		assertNotNull(testProject);
	}

}
