package com.redhat.parodos.examples.integration;

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
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.utils.CredUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
	public void runSimpleFlow() throws ApiException {
		ApiClient defaultClient = Configuration.getDefaultApiClient();

		defaultClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));

		ProjectApi apiInstance = new ProjectApi(defaultClient);

		ProjectResponseDTO testProject = null;

		try {
			// RETRIEVE ALL PROJECTS AVAILABLE
			List<ProjectResponseDTO> projects = apiInstance.getProjects();

			// CHECK IF testProject ALREADY EXISTS
			testProject = projects.stream()
					.filter(prj -> testPrjName.equals(prj.getName()) && testPrjDescription.equals(prj.getDescription()))
					.findAny().orElse(null);

			// CREATE PROJECT "Test Project Name" IF NOT EXISTS
			if (testProject == null) {
				// DEFINE A TEST PROJECT REQUEST
				ProjectRequestDTO projectRequestDTO = ProjectRequestDTO.builder().name(testPrjName)
						.description(testPrjDescription).build();

				ProjectResponseDTO projectResponseDTO = apiInstance.createProject(projectRequestDTO);
				assertNotNull(projectResponseDTO);
				assertEquals(testPrjName, projectResponseDTO.getName());

			}

			// ASSERT PROJECT "testProject" IS PRESENT
			projects = apiInstance.getProjects();

			assertTrue(projects.size() > 0);
			assertNotNull(projects.stream()
					.filter(prj -> testPrjName.equals(prj.getName()) && testPrjDescription.equals(prj.getDescription()))
					.findAny().orElse(null));

			// GET simpleSequentialWorkFlow DEFINITIONS
			WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi();
			List<WorkFlowDefinitionResponseDTO> simpleSequentialWorkFlowDefinitions = workflowDefinitionApi
					.getWorkFlowDefinitions("simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW);
			assertEquals(1, simpleSequentialWorkFlowDefinitions.size());

			// GET WORKFLOW DEFINITION BY Id
			WorkFlowDefinitionResponseDTO simpleSequentialWorkFlowDefinition = workflowDefinitionApi
					.getWorkFlowDefinitionById(simpleSequentialWorkFlowDefinitions.get(0).getId().toString());

			assertEquals("simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW,
					simpleSequentialWorkFlowDefinition.getName());
			assertEquals(WorkFlowProcessingType.SEQUENTIAL.toString(),
					simpleSequentialWorkFlowDefinition.getProcessingType());
			assertEquals(WorkFlowType.INFRASTRUCTURE.toString(), simpleSequentialWorkFlowDefinition.getType());

			// EXECUTE SIMPLE WORKFLOW
			WorkflowApi workflowApi = new WorkflowApi();

			// 1 - Define WorkRequests
			WorkRequestDTO work1 = WorkRequestDTO.builder().workName("restCallTask")
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("url").value("https://httpbin.org/post"),
							new ArgumentRequestDTO().key("payload").value("'Hello!'")))
					.build();

			WorkRequestDTO work2 = WorkRequestDTO.builder().workName("loggingTask")
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
							new ArgumentRequestDTO().key("api-server").value("test-api-server")))
					.build();

			// 2 - Define WorkFlowRequest
			WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().projectId(testProject.getId())
					.workFlowName("simpleSequentialWorkFlow_INFRASTRUCTURE_WORKFLOW").works(Arrays.asList(work1, work2))
					.build();

			// 3 - Execute Workflow
			WorkFlowResponseDTO execute = workflowApi.execute(workFlowRequestDTO);

			// 4 - Check execution details
			assertNotNull(execute.getWorkFlowExecutionId());
		}
		catch (ApiException e) {
			fail(String.format("Exception when calling API.\nStatus code: %d\n Reason: %s\n Response headers: %s",
					e.getCode(), e.getResponseBody(), e.getResponseHeaders()));
		}
	}

}
