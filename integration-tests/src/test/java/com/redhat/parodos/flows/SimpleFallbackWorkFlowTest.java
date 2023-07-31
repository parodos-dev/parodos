package com.redhat.parodos.flows;

import java.util.List;
import java.util.function.Consumer;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.flows.common.WorkFlowTestBuilder.TestComponents;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Richard Wang (Github: richrdW98)
 */
@Slf4j
public class SimpleFallbackWorkFlowTest {

	private static final String WORKFLOW_NAME = "simpleFailedWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW;

	@Test
	public void runFallbackWorkFlow() throws ApiException {
		log.info("******** Running The Simple WorkFlow ********");
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject()
				.withWorkFlowDefinition(WORKFLOW_NAME, getWorkFlowDefinitionResponseConsumer()).build();

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(WORKFLOW_NAME);
		workFlowRequestDTO.setWorks(List.of());

		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		log.info("******** Running The Simple Failed Flow ********");

		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId(), WorkFlowStatusResponseDTO.StatusEnum.FAILED);

		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO.getStatus());
		assertNotNull(workFlowStatusResponseDTO.getFallbackExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.FAILED, workFlowStatusResponseDTO.getStatus());
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** Waiting for fallback workflow {} to be Completed ********",
				workFlowStatusResponseDTO.getFallbackExecutionId());
		WorkFlowStatusResponseDTO fallbackWorkFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(
				workflowApi, workFlowStatusResponseDTO.getFallbackExecutionId(),
				WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);
		log.info("fallback workflow finished successfully with response: {}", fallbackWorkFlowStatusResponseDTO);
		workFlowStatusResponseDTO = workflowApi.getStatus(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(fallbackWorkFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertNotNull(fallbackWorkFlowStatusResponseDTO.getStatus());
		assertEquals(fallbackWorkFlowStatusResponseDTO.getOriginalExecutionId(),
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertNull(fallbackWorkFlowStatusResponseDTO.getFallbackExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, fallbackWorkFlowStatusResponseDTO.getStatus());
		// Fallback execution are NOT restarts
		assertEquals(fallbackWorkFlowStatusResponseDTO.getRestartedCount().intValue(), 0);
		assertEquals(workFlowStatusResponseDTO.getRestartedCount().intValue(), 0);

		log.info("******** Simple Failed Flow Completed ********");
	}

	private static Consumer<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionResponseConsumer() {
		return workFlowDefinition -> {
			assertNotNull(workFlowDefinition.getId());
			assertEquals(WORKFLOW_NAME, workFlowDefinition.getName());
			assertEquals(WorkFlowDefinitionResponseDTO.ProcessingTypeEnum.SEQUENTIAL,
					workFlowDefinition.getProcessingType());
			assertEquals(WorkFlowDefinitionResponseDTO.TypeEnum.INFRASTRUCTURE, workFlowDefinition.getType());
		};
	}

}
