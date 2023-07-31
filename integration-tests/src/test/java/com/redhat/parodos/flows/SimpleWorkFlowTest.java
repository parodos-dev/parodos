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
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO.ProcessingTypeEnum;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO.TypeEnum;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.util.CollectionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class SimpleWorkFlowTest {

	private static final String WORKFLOW_NAME = "simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW;

	@Test
	public void runSimpleWorkFlow() throws ApiException, InterruptedException {
		log.info("******** Running The Simple WorkFlow ********");
		TestComponents components = new WorkFlowTestBuilder().withProject("simpleWorkFlowProject", "test project")
				.withWorkFlowDefinition(WORKFLOW_NAME, getWorkFlowDefinitionResponseConsumer()).build();

		// Define WorkRequests
		WorkRequestDTO work1 = new WorkRequestDTO();
		work1.setWorkName("restCallTask");
		work1.setArguments(
				Arrays.asList(new ArgumentRequestDTO().key("url").value("http://localhost:8080/actuator/health"),
						new ArgumentRequestDTO().key("method").value("get")));

		WorkRequestDTO work2 = new WorkRequestDTO();
		work2.setWorkName("loggingTask");
		work2.setArguments(List.of(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("test-api-server")));

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(WORKFLOW_NAME);
		workFlowRequestDTO.setWorks(Arrays.asList(work1, work2));

		log.info("******** Running The Simple Sequence Flow ********");
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO.getStatus());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** Simple Sequence Flow Completed ********");
	}

	private static Consumer<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionResponseConsumer() {
		return workFlowDefinition -> {
			assertNotNull(workFlowDefinition.getId());
			assertEquals(WORKFLOW_NAME, workFlowDefinition.getName());
			assertEquals(ProcessingTypeEnum.SEQUENTIAL, workFlowDefinition.getProcessingType());
			assertEquals(TypeEnum.INFRASTRUCTURE, workFlowDefinition.getType());

			assertNotNull(workFlowDefinition.getWorks());
			assertEquals(2, workFlowDefinition.getWorks().size());
			List<WorkDefinitionResponseDTO> works = workFlowDefinition.getWorks().stream().toList();
			assertEquals("restCallTask", works.get(0).getName());
			assertEquals(WorkDefinitionResponseDTO.WorkTypeEnum.TASK, works.get(0).getWorkType());
			assertTrue(CollectionUtils.isEmpty(works.get(0).getWorks()));
			assertNull(works.get(0).getProcessingType());
			assertNotNull(works.get(0).getParameters());

			assertEquals("loggingTask", works.get(1).getName());
			assertEquals(WorkDefinitionResponseDTO.WorkTypeEnum.TASK, works.get(1).getWorkType());
			assertTrue(CollectionUtils.isEmpty(works.get(1).getWorks()));
			assertNull(works.get(1).getProcessingType());
			assertNotNull(works.get(1).getParameters());
		};
	}

}
