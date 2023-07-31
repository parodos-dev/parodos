package com.redhat.parodos.flows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.util.CollectionUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

		assertThat(workFlowResponseDTO.getWorkFlowExecutionId(), is(notNullValue()));
		assertThat(workFlowResponseDTO.getWorkStatus(), is(notNullValue()));
		assertThat(workFlowResponseDTO.getWorkStatus(), equalTo(WorkStatusEnum.IN_PROGRESS));

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertThat(workFlowStatusResponseDTO, is(notNullValue()));
		assertThat(workFlowStatusResponseDTO.getStatus(), equalTo(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED));
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** PreBuilt Sequence Flow Completed ********");
	}

	private Consumer<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionResponseConsumer() {
		return workFlowDefinition -> {
			assertThat(workFlowDefinition.getId(), is(notNullValue()));
			assertThat(workFlowDefinition.getName(), equalTo(WORKFLOW_NAME));
			assertThat(workFlowDefinition.getProcessingType(),
					equalTo(WorkFlowDefinitionResponseDTO.ProcessingTypeEnum.SEQUENTIAL));
			assertThat(workFlowDefinition.getType(), equalTo(WorkFlowDefinitionResponseDTO.TypeEnum.INFRASTRUCTURE));

			assertNotNull(workFlowDefinition.getWorks());
			assertEquals(1, workFlowDefinition.getWorks().size());
			Optional<WorkDefinitionResponseDTO> firstWork = workFlowDefinition.getWorks().stream().findFirst();
			assertTrue(firstWork.isPresent());
			assertEquals("notificationTask", firstWork.get().getName());
			assertEquals(WorkDefinitionResponseDTO.WorkTypeEnum.TASK, firstWork.get().getWorkType());
			assertTrue(CollectionUtils.isEmpty(firstWork.get().getWorks()));
			assertNull(firstWork.get().getProcessingType());
			assertNotNull(firstWork.get().getParameters());
		};
	}

}
