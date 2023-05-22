package com.redhat.parodos.flows;

import com.redhat.parodos.flows.base.BaseIntegrationTest;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdkutils.SdkUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class EscalationFlowTest extends BaseIntegrationTest {

	@Test
	public void runEscalationFlow() throws ApiException, InterruptedException {
		log.info("Running escalation flow");
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

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = SdkUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("******** Simple Escalation Flow {} ********", workFlowStatusResponseDTO.getStatus());
	}

}
