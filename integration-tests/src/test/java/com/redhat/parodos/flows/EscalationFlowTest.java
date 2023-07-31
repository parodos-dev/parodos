package com.redhat.parodos.flows;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.flows.common.WorkFlowTestBuilder.TestComponents;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO.WorkStatusEnum;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.describedAs;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class EscalationFlowTest {

	private static final String WORKFLOW_NAME = "workflowStartingCheckingAndEscalation";

	@Test
	public void runEscalationFlow() throws ApiException {
		log.info("******** Running The Escalation workFlow ********");
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject().withWorkFlowDefinition(WORKFLOW_NAME)
				.build();

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName("workflowStartingCheckingAndEscalation");

		log.info("executes 1 task with a WorkFlowChecker");
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertThat(workFlowResponseDTO.getWorkFlowExecutionId(),
				describedAs("There is no valid WorkFlowExecutionId", is(notNullValue())));
		assertEquals(workFlowResponseDTO.getWorkStatus(), WorkStatusEnum.IN_PROGRESS);
		log.info("Escalation workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());
		log.info("Escalation workFlow {}", workFlowResponseDTO.getWorkStatus());
		log.info("Waiting for checkers to complete...");

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("******** Escalation workFlow {} ********", workFlowStatusResponseDTO.getStatus());
	}

}
