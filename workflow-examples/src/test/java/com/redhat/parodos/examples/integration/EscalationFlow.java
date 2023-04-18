package com.redhat.parodos.examples.integration;

import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

import static com.redhat.parodos.examples.integration.utils.ExamplesUtils.getProjectAsync;
import static com.redhat.parodos.examples.integration.utils.ExamplesUtils.waitWorkflowStatusAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
		ProjectResponseDTO testProject = getProjectAsync(apiClient, projectName, projectDescription);
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

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.COMPLETED.toString(), workFlowStatusResponseDTO.getStatus());
		log.info("******** Simple Escalation Flow {} ********", workFlowStatusResponseDTO.getStatus());

	}

}
