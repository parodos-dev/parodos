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
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(workFlowResponseDTO.getWorkFlowExecutionId()).isNotNull();
		assertThat(workFlowResponseDTO.getWorkStatus()).isEqualTo(WorkStatusEnum.IN_PROGRESS);

		log.info("PendingWithAlertMessageWorkFlow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId(), StatusEnum.PENDING);

		assertThat(workFlowStatusResponseDTO.getWorkFlowExecutionId()).isNotNull();
		assertThat(workFlowStatusResponseDTO.getStatus()).isEqualTo(StatusEnum.PENDING);
		assertThat(workFlowStatusResponseDTO.getMessage()).isNull();

		// verify the task status and message
		assertThat(workFlowStatusResponseDTO.getWorks()).isNotNull();
		assertThat(workFlowStatusResponseDTO.getWorks()).hasSize(2);

		// first task - doNothingWorkFlowTask (fetched in reversed order)
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getName()).isEqualTo("doNothingWorkFlowTask");
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getStatus())
				.isEqualTo(WorkStatusResponseDTO.StatusEnum.COMPLETED);
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getMessage()).isNull();
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getAlertMessage()).isNull();

		// second task - pendingWithAlertMessageWorkFlowTask
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getName())
				.isEqualTo("pendingWithAlertMessageWorkFlowTask");
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getStatus())
				.isEqualTo(WorkStatusResponseDTO.StatusEnum.PENDING);
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getMessage()).isNull();
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getAlertMessage())
				.isEqualTo("[link](http://localhost:8080)");

		log.info("******** PendingWithAlertMessageWorkFlow successfully ended: {} ********",
				workFlowStatusResponseDTO.getStatus());
	}

	private static Consumer<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionResponseConsumer() {
		return workFlowDefinition -> {
			assertThat(workFlowDefinition.getId()).isNotNull();
			assertThat(WORKFLOW_NAME).isEqualTo(workFlowDefinition.getName());
			assertThat(workFlowDefinition.getProcessingType()).isEqualTo(ProcessingTypeEnum.SEQUENTIAL);
			assertThat(workFlowDefinition.getType()).isEqualTo(TypeEnum.INFRASTRUCTURE);

			assertThat(workFlowDefinition.getWorks()).isNotNull();
			assertThat(workFlowDefinition.getWorks()).hasSize(2);
			List<WorkDefinitionResponseDTO> works = workFlowDefinition.getWorks().stream().toList();
			assertThat(works.get(1).getName()).isEqualTo("doNothingWorkFlowTask");
			assertThat(works.get(1).getWorkType()).isEqualTo(WorkDefinitionResponseDTO.WorkTypeEnum.TASK);
			assertThat(works.get(1).getWorks()).isNullOrEmpty();
			assertThat(works.get(1).getProcessingType()).isNull();
			assertThat(works.get(1).getParameters()).isNotNull();
			assertThat(works.get(0).getName()).isEqualTo("pendingWithAlertMessageWorkFlowTask");
		};
	}

}
