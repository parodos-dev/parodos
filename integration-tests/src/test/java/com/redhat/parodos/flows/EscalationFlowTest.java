package com.redhat.parodos.flows;

import com.redhat.parodos.flows.base.BaseIntegrationTest;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.redhat.parodos.sdkutils.SdkUtils.getProjectAsync;
import static com.redhat.parodos.sdkutils.SdkUtils.waitWorkflowStatusAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class EscalationFlowTest extends BaseIntegrationTest {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

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
		assertEquals(workFlowResponseDTO.getWorkStatus(), WorkFlowResponseDTO.WorkStatusEnum.IN_PROGRESS);
		log.info("Simple escalation workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());
		log.info("Simple Escalation Flow {}", workFlowResponseDTO.getWorkStatus());
		log.info("Waiting for checkers to complete...");

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("******** Simple Escalation Flow {} ********", workFlowStatusResponseDTO.getStatus());
	}

}
