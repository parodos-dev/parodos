package com.redhat.parodos.flows.negative;

import java.util.Optional;
import java.util.function.Consumer;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.flows.common.WorkFlowTestBuilder.TestComponents;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.WorkDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO.ProcessingTypeEnum;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO.TypeEnum;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO.StatusEnum;
import com.redhat.parodos.sdk.model.WorkStatusResponseDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class SequentialFailedWorkFlowTest {

	private static final String WORKFLOW_NAME = "sequentialFailedWorkFlow";

	@Test
	public void runSequentialFailedWorkFlow() throws ApiException {
		log.info("******** Running The Sequential Failed WorkFlow ********");
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject()
				.withWorkFlowDefinition(WORKFLOW_NAME, getWorkFlowDefinitionResponseConsumer()).build();

		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(WORKFLOW_NAME);

		log.info("executes 1 task that should fail");
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertThat(workFlowResponseDTO.getWorkFlowExecutionId(), is(notNullValue()));
		assertThat(workFlowResponseDTO.getWorkStatus(), equalTo(WorkStatusEnum.IN_PROGRESS));

		log.info("Sequential Failed WorkFlow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId(), StatusEnum.FAILED);

		assertThat(workFlowStatusResponseDTO.getWorkFlowExecutionId(), is(notNullValue()));
		assertThat(workFlowStatusResponseDTO.getStatus(), equalTo(StatusEnum.FAILED));
		assertThat(workFlowStatusResponseDTO.getMessage(), equalTo("FailedWorkFlowTask failure"));

		// verify the task status and message
		assertThat(workFlowStatusResponseDTO.getWorks(), is(notNullValue()));
		assertThat(workFlowStatusResponseDTO.getWorks(), hasSize(1));
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getName(), equalTo("failedWorkFlowTask"));
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getStatus(),
				equalTo(WorkStatusResponseDTO.StatusEnum.FAILED));
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getMessage(), equalTo("FailedWorkFlowTask failure"));

		log.info("******** Sequential Failed WorkFlow successfully failed: {} ********",
				workFlowStatusResponseDTO.getStatus());
	}

	private static Consumer<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionResponseConsumer() {
		return workFlowDefinition -> {
			assertThat(workFlowDefinition.getId(), is(notNullValue()));
			assertThat(WORKFLOW_NAME, equalTo(workFlowDefinition.getName()));
			assertThat(workFlowDefinition.getProcessingType(), equalTo(ProcessingTypeEnum.SEQUENTIAL));
			assertThat(workFlowDefinition.getType(), equalTo(TypeEnum.INFRASTRUCTURE));

			assertThat(workFlowDefinition.getWorks(), is(notNullValue()));
			assertThat(workFlowDefinition.getWorks(), hasSize(1));
			Optional<WorkDefinitionResponseDTO> firstWork = workFlowDefinition.getWorks().stream().findFirst();
			assertTrue(firstWork.isPresent());
			assertThat(firstWork.get().getName(), equalTo("failedWorkFlowTask"));
			assertThat(firstWork.get().getWorkType(), equalTo(WorkDefinitionResponseDTO.WorkTypeEnum.TASK));
			assertThat(firstWork.get().getWorks(), anyOf(nullValue(), empty()));
			assertThat(firstWork.get().getProcessingType(), is(nullValue()));
			assertThat(firstWork.get().getParameters(), is(notNullValue()));
		};
	}

}
