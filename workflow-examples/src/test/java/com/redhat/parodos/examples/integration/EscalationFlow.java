package com.redhat.parodos.examples.integration;

import com.redhat.parodos.examples.integration.utils.ExamplesUtils;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.ProjectRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.redhat.parodos.examples.integration.utils.ExamplesUtils.getProjectByNameAndDescription;
import static com.redhat.parodos.examples.integration.utils.ExamplesUtils.waitAsyncStatusResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class EscalationFlow {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

	private ApiClient apiClient;

	@Before
	public void setUp() throws IOException {
		apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));
	}

	@Test
	public void runEscalationFlow() throws ApiException, InterruptedException {
		log.info("Running escalation flow");
		ProjectApi projectApi = new ProjectApi(apiClient);

		ExamplesUtils.waitProjectStart(projectApi);
		log.info("Project is ✔️ on {}", apiClient.getBasePath());

		ProjectResponseDTO testProject;

		// RETRIEVE ALL PROJECTS AVAILABLE
		log.info("Get all available projects");
		List<ProjectResponseDTO> projects = projectApi.getProjects();

		// CHECK IF testProject ALREADY EXISTS
		testProject = getProjectByNameAndDescription(projects, projectName, projectDescription);

		// CREATE PROJECT "Test Project Name" IF NOT EXISTS
		if (testProject == null) {
			log.info("There are no projects. Creating project {}", projectName);
			// DEFINE A TEST PROJECT REQUEST
			ProjectRequestDTO projectRequestDTO = new ProjectRequestDTO();
			projectRequestDTO.setName(projectName);
			projectRequestDTO.setDescription(projectDescription);

			ProjectResponseDTO projectResponseDTO = projectApi.createProject(projectRequestDTO);
			assertNotNull(projectResponseDTO);
			assertEquals(projectName, projectResponseDTO.getName());
			assertEquals(projectDescription, projectResponseDTO.getDescription());
			log.info("Project {} successfully created", projectName);
		}

		// ASSERT PROJECT "testProject" IS PRESENT
		projects = projectApi.getProjects();
		log.debug("PROJECTS: {}", projects);
		assertTrue(projects.size() > 0);
		testProject = getProjectByNameAndDescription(projects, projectName, projectDescription);
		assertNotNull(testProject);

		WorkflowApi workflowApi = new WorkflowApi();

		log.info("******** Running The Escalation WorkFlow ********");
		log.info("executes 1 task with a WorkFlowChecker");

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(testProject.getId());
		workFlowRequestDTO.setWorkFlowName("workflowStartingCheckingAndEscalation");

		WorkFlowResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull("There is no valid WorkFlowExecutionId", workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowResponseDTO.getWorkStatus(), WorkStatusEnum.IN_PROGRESS);
		log.info("Simple escalation workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());
		log.info("Simple Escalation Flow {}", workFlowResponseDTO.getWorkStatus());
		log.info("Waiting for checkers to complete...");
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = waitAsyncStatusResponse(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.COMPLETED.toString(), workFlowStatusResponseDTO.getStatus());
		log.info("******** Simple Escalation Flow {} ********", workFlowStatusResponseDTO.getStatus());

	}

}
