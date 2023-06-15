package com.redhat.parodos.flows.negative;

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
public class FailedWithAlertMessageWorkFlowTest {

	private static final String WORKFLOW_NAME = "failedWithAlertMessageWorkFlow";

	@Test
	public void runSequentialFailedWorkFlow() throws ApiException {
		log.info("******** Running The FailedWithAlertMessageWorkFlow ********");
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject()
				.withWorkFlowDefinition(WORKFLOW_NAME, getWorkFlowDefinitionResponseConsumer()).build();

		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(WORKFLOW_NAME);

		log.info("executes 2 tasks that should end with failed status and alert message");
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertThat(workFlowResponseDTO.getWorkFlowExecutionId()).isNotNull();
		assertThat(workFlowResponseDTO.getWorkStatus()).isEqualTo(WorkStatusEnum.IN_PROGRESS);

		log.info("FailedWithAlertMessageWorkFlow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId(), StatusEnum.FAILED);

		assertThat(workFlowStatusResponseDTO.getWorkFlowExecutionId()).isNotNull();
		assertThat(workFlowStatusResponseDTO.getStatus()).isEqualTo(StatusEnum.FAILED);
		assertThat(workFlowStatusResponseDTO.getMessage()).isNull();

		// verify the task status and message
		assertThat(workFlowStatusResponseDTO.getWorks()).isNotNull();
		assertThat(workFlowStatusResponseDTO.getWorks()).hasSize(2);

		// first task - doNothingWorkFlowTask (fetched in reversed order)
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getName()).isEqualTo("doNothingAgainWorkFlowTask");
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getStatus())
				.isEqualTo(WorkStatusResponseDTO.StatusEnum.COMPLETED);
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getMessage()).isNull();
		assertThat(workFlowStatusResponseDTO.getWorks().get(1).getAlertMessage()).isNull();

		// second task - failedWithAlertMessageWorkFlowTask
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getName())
				.isEqualTo("failedWithAlertMessageWorkFlowTask");
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getStatus())
				.isEqualTo(WorkStatusResponseDTO.StatusEnum.FAILED);
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getMessage()).isNull();
		assertThat(workFlowStatusResponseDTO.getWorks().get(0).getAlertMessage())
				.isEqualTo("[link](http://localhost:8080)");

		log.info("******** FailedWithAlertMessageWorkFlow successfully ended: {} ********",
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
			assertThat(workFlowDefinition.getWorks().get(1).getName()).isEqualTo("doNothingAgainWorkFlowTask");
			assertThat(workFlowDefinition.getWorks().get(1).getWorkType())
					.isEqualTo(WorkDefinitionResponseDTO.WorkTypeEnum.TASK);
			assertThat(workFlowDefinition.getWorks().get(1).getWorks()).isNullOrEmpty();
			assertThat(workFlowDefinition.getWorks().get(1).getProcessingType()).isNull();
			assertThat(workFlowDefinition.getWorks().get(1).getParameters()).isNotNull();
			assertThat(workFlowDefinition.getWorks().get(0).getName()).isEqualTo("failedWithAlertMessageWorkFlowTask");
		};
	}

}
