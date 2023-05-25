package com.redhat.parodos.examples.rest;

import java.util.Arrays;
import java.util.List;

import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceSdkUtils;
import com.redhat.parodos.workflow.utils.CredUtils;
import org.junit.Test;

import org.springframework.http.HttpHeaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestWorkFlow {

	private final String projectName = "project-1";

	private final String projectDescription = "Rest example project";

	private final String workflowName = "restWorkFlow";

	private final String taskName = "restTask";

	@Test
	public void runFlow() throws InterruptedException, ApiException {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		defaultClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));
		ProjectResponseDTO testProject = WorkFlowServiceSdkUtils.getProjectAsync(defaultClient, projectName,
				projectDescription);

		// GET workflow DEFINITIONS
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi(defaultClient);
		List<WorkFlowDefinitionResponseDTO> simpleSequentialWorkFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions(workflowName);
		assertEquals(1, simpleSequentialWorkFlowDefinitions.size());

		// GET WORKFLOW DEFINITION BY Id
		WorkFlowDefinitionResponseDTO simpleSequentialWorkFlowDefinition = workflowDefinitionApi
				.getWorkFlowDefinitionById(simpleSequentialWorkFlowDefinitions.get(0).getId());

		// EXECUTE WORKFLOW
		WorkflowApi workflowApi = new WorkflowApi();

		// Define WorkRequests
		WorkRequestDTO workGet = new WorkRequestDTO().workName(taskName)
				.arguments(Arrays.asList(
						new ArgumentRequestDTO().key("url").value("http://localhost:8080/api/v1/workflowdefinitions"),
						new ArgumentRequestDTO().key("method").value("get"),
						new ArgumentRequestDTO().key("username").value("test"),
						new ArgumentRequestDTO().key("password").value("test")));

		WorkFlowRequestDTO workFlowRequestGet = new WorkFlowRequestDTO().projectId(testProject.getId())
				.workFlowName(workflowName).works(Arrays.asList(workGet));
		WorkFlowRequestDTO workFlowRequests[] = new WorkFlowRequestDTO[] { workFlowRequestGet };

		for (WorkFlowRequestDTO workFlowRequest : workFlowRequests) {
			WorkFlowExecutionResponseDTO execute = workflowApi.execute(workFlowRequest);

			assertNotNull(execute.getWorkFlowExecutionId());
		}
	}

}
