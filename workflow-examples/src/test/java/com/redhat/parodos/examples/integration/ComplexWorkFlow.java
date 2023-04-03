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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class ComplexWorkFlow {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

	private ApiClient apiClient;

	@Before
	public void setUp() throws IOException {
		apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));
	}

	@Test
	public void runComplexWorkFlow() throws ApiException, InterruptedException {
		log.info("Running complex flow");
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
		log.info("******** Running The Complex WorkFlow ********");

		log.info("Running the Assessment to see what WorkFlows are eligible for this situation:");

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(testProject.getId());
		workFlowRequestDTO.setWorkFlowName("onboardingMasterAssessment_ASSESSMENT_WORKFLOW");
		workFlowRequestDTO.setWorks(List.of(WorkRequestDTO.builder()
				.arguments(List.of(ArgumentRequestDTO.builder().key("GIT_REPO_URL").value("git_repo_url").build()))
				.build()));

		WorkFlowResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);
		assertEquals(WorkStatusEnum.COMPLETED, workFlowResponseDTO.getWorkStatus());
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		if (workFlowResponseDTO.getWorkFlowOptions() == null
				|| workFlowResponseDTO.getWorkStatus() != WorkStatusEnum.COMPLETED) {
			fail("There is no valid INFRASTRUCTURE_OPTION");
		}

		// log.info("The Following newOption : {}",
		// workFlowResponseDTO.getWorkFlowOptions().getNewOptions());

		String infrastructureOption = workFlowResponseDTO.getWorkFlowOptions().getNewOptions().get(0).getWorkFlowName();
		log.info("The Following Option Is Available: {}", infrastructureOption);

		log.info("Running the onboarding WorkFlow");
		log.info("executes 3 tasks in Parallel with a WorkFlowChecker");
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi();
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions(infrastructureOption);

		assertNotNull(workFlowDefinitions);
		assertTrue(workFlowDefinitions.size() > 0);
		assertNotNull("There is no valid Onboarding workflow id", workFlowDefinitions.get(0).getId());
		assertEquals("There is no valid Onboarding workflow name", workFlowDefinitions.get(0).getName(),
				infrastructureOption);
		log.info("Onboarding workflow id {}", workFlowDefinitions.get(0).getId());
		log.info("Onboarding workflow name {}", workFlowDefinitions.get(0).getName());

		WorkRequestDTO work1 = new WorkRequestDTO();
		work1.setWorkName("certWorkFlowTask");
		work1.setArguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("api.com")));

		WorkRequestDTO work2 = new WorkRequestDTO();
		work2.setWorkName("adGroupWorkFlowTask");
		work2.setArguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("api.com")));

		WorkRequestDTO work3 = new WorkRequestDTO();
		work3.setWorkName("dynatraceWorkFlowTask");
		work3.setArguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("api.com")));

		workFlowRequestDTO.setProjectId(testProject.getId());
		workFlowRequestDTO.setWorkFlowName(workFlowDefinitions.get(0).getName());
		workFlowRequestDTO.setWorks(Arrays.asList(work1, work2, work3));
		workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull("There is no valid WorkFlowExecutionId", workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowResponseDTO.getWorkStatus(), WorkStatusEnum.IN_PROGRESS);
		log.info("Onboarding workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = waitAsyncStatusResponse(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.COMPLETED.toString(), workFlowStatusResponseDTO.getStatus());
		log.info("Onboarding workflow execution completed with status {}", workFlowStatusResponseDTO.getStatus());
	}

}
