package com.redhat.parodos.flows.negative;

import java.util.List;
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

@Slf4j
public class PendingWithAlertMessageWorkFlowTest {

	private static final String WORKFLOW_NAME = "pendingWithAlertMessageWorkFlow";

	@Test
	public void runSequentialFailedWorkFlow() throws ApiException {
		log.info("******** Running The PendingWithAlertMessageWorkFlow ********");
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject()
				.withWorkFlowDefinition(WORKFLOW_NAME, getWorkFlowDefinitionResponseConsumer()).build();

		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(WORKFLOW_NAME);

		log.info("executes 2 tasks that should end with pending status and alert message");
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertThat(workFlowResponseDTO.getWorkFlowExecutionId(), is(notNullValue()));
		assertThat(workFlowResponseDTO.getWorkStatus(), equalTo(WorkStatusEnum.IN_PROGRESS));

		log.info("PendingWithAlertMessageWorkFlow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId(), StatusEnum.PENDING);

		assertThat(workFlowStatusResponseDTO.getWorkFlowExecutionId(), is(notNullValue()));
		assertThat(workFlowStatusResponseDTO.getStatus(), equalTo(StatusEnum.PENDING));
		assertThat(workFlowStatusResponseDTO.getMessage(), is(nullValue()));

		// verify the task status and message
		assertThat(workFlowStatusResponseDTO.getWorks(), is(notNullValue()));
		assertThat(workFlowStatusResponseDTO.getWorks(), hasSize(2));

		// first task - doNothingWorkFlowTask (fetched in reversed order)
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getName(), equalTo("doNothingWorkFlowTask"));
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getStatus(),
				equalTo(WorkStatusResponseDTO.StatusEnum.COMPLETED));
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getMessage(), is(nullValue()));
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getAlertMessage(), is(nullValue()));

		// second task - pendingWithAlertMessageWorkFlowTask
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getName(),
				equalTo("pendingWithAlertMessageWorkFlowTask"));
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getStatus(),
				equalTo(WorkStatusResponseDTO.StatusEnum.PENDING));
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getMessage(), is(nullValue()));
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getAlertMessage(),
				equalTo("[link](http://localhost:8080)"));

		log.info("******** PendingWithAlertMessageWorkFlow successfully ended: {} ********",
				workFlowStatusResponseDTO.getStatus());
	}

	private static Consumer<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionResponseConsumer() {
		return workFlowDefinition -> {
			assertThat(workFlowDefinition.getId(), is(notNullValue()));
			assertThat(WORKFLOW_NAME, equalTo(workFlowDefinition.getName()));
			assertThat(workFlowDefinition.getProcessingType(), equalTo(ProcessingTypeEnum.SEQUENTIAL));
			assertThat(workFlowDefinition.getType(), equalTo(TypeEnum.INFRASTRUCTURE));

			assertThat(workFlowDefinition.getWorks(), is(notNullValue()));
			assertThat(workFlowDefinition.getWorks(), hasSize(2));
			List<WorkDefinitionResponseDTO> works = workFlowDefinition.getWorks().stream().toList();
			assertThat(works.get(1).getName(), equalTo("doNothingWorkFlowTask"));
			assertThat(works.get(1).getWorkType(), equalTo(WorkDefinitionResponseDTO.WorkTypeEnum.TASK));
			assertThat(works.get(1).getWorks(), anyOf(nullValue(), empty()));
			assertThat(works.get(1).getProcessingType(), is(nullValue()));
			assertThat(works.get(1).getParameters(), is(notNullValue()));
			assertThat(works.get(0).getName(), equalTo("pendingWithAlertMessageWorkFlowTask"));
		};
	}

}
