package com.redhat.parodos.flows;

import java.util.List;
import java.util.function.Consumer;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.flows.common.WorkFlowTestBuilder.TestComponents;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdkutils.SdkUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Richard Wang (Github: richrdW98)
 */
@Slf4j
public class SimpleRollbackWorkFlowTest {

	private static final String WORKFLOW_NAME = "simpleFailedWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW;

	@Test
	public void runRollbackWorkFlow() throws ApiException, InterruptedException {
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

		WorkFlowResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = SdkUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId(), WorkFlowStatusResponseDTO.StatusEnum.FAILED);

		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO.getStatus());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.FAILED, workFlowStatusResponseDTO.getStatus());
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** Simple Failed Flow Completed ********");
	}

	@NotNull
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
