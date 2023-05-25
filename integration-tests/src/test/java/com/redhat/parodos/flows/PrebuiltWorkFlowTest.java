package com.redhat.parodos.flows;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.flows.common.WorkFlowTestBuilder.TestComponents;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.WorkDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceSdkUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import org.springframework.util.CollectionUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class PrebuiltWorkFlowTest {

	private static final String WORKFLOW_NAME = "prebuiltWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW;

	@Test
	public void runPreBuiltWorkFlow() throws ApiException, InterruptedException {
		log.info("******** Running The pre-built WorkFlow (name: {}) ********", WORKFLOW_NAME);
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject()
				.withWorkFlowDefinition(WORKFLOW_NAME, getWorkFlowDefinitionResponseConsumer()).build();

		// Define WorkRequests
		WorkRequestDTO work1 = new WorkRequestDTO();
		work1.setWorkName("notificationTask");
		work1.setArguments(Arrays.asList(new ArgumentRequestDTO().key("type").value("test-type"),
				new ArgumentRequestDTO().key("body").value("test body"),
				new ArgumentRequestDTO().key("subject").value("test subject"),
				new ArgumentRequestDTO().key("userNames").value("test-username")));

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(WORKFLOW_NAME);
		workFlowRequestDTO.setWorks(List.of(work1));

		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		log.info("******** Running The PreBuilt Flow ********");
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceSdkUtils
				.waitWorkflowStatusAsync(workflowApi, workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO);
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** PreBuilt Sequence Flow Completed ********");
	}

	private Consumer<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionResponseConsumer() {
		return workFlowDefinition -> {
			assertNotNull(workFlowDefinition.getId());
			assertEquals(WORKFLOW_NAME, workFlowDefinition.getName());
			assertEquals(WorkFlowDefinitionResponseDTO.ProcessingTypeEnum.SEQUENTIAL,
					workFlowDefinition.getProcessingType());
			assertEquals(WorkFlowDefinitionResponseDTO.TypeEnum.INFRASTRUCTURE, workFlowDefinition.getType());

			assertNotNull(workFlowDefinition.getWorks());
			assertEquals(1, workFlowDefinition.getWorks().size());
			assertEquals("notificationTask", workFlowDefinition.getWorks().get(0).getName());
			assertEquals(WorkDefinitionResponseDTO.WorkTypeEnum.TASK,
					workFlowDefinition.getWorks().get(0).getWorkType());
			assertTrue(CollectionUtils.isEmpty(workFlowDefinition.getWorks().get(0).getWorks()));
			assertNull(workFlowDefinition.getWorks().get(0).getProcessingType());
			assertNotNull(workFlowDefinition.getWorks().get(0).getParameters());
		};
	}

}
