package com.redhat.parodos.flows;

import java.util.List;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.flows.common.WorkFlowTestBuilder.TestComponents;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowContextResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdkutils.SdkUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jordi Gil (Github: jordigilh)
 */
@Slf4j
public class VMMigrationWorkFlow {

	private static final String WORKFLOW_NAME = "vmMigration" + WorkFlowConstants.ASSESSMENT_WORKFLOW;

	@Test
	public void runMigrationWorkFlow() throws ApiException, InterruptedException {
		log.info("Running vm migration flow");

		TestComponents components = new WorkFlowTestBuilder().withDefaultProject().withWorkFlowDefinition(WORKFLOW_NAME)
				.build();

		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName("vmMigration" + WorkFlowConstants.ASSESSMENT_WORKFLOW);
		ArgumentRequestDTO vmName = new ArgumentRequestDTO().key("VM_NAME").value("mtv-rhel8-sanity");
		ArgumentRequestDTO apiUrl = new ArgumentRequestDTO().key("API_SERVER").value("localhost:6443");
		ArgumentRequestDTO token = new ArgumentRequestDTO().key("TOKEN").value("<token>");

		workFlowRequestDTO.addArgumentsItem(vmName).addArgumentsItem(apiUrl).addArgumentsItem(token);

		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		log.info("******** Running The Sequence Flow ********");

		log.info("******** Start VM Migration assessment Flow ********");
		WorkFlowExecutionResponseDTO workFlowResponseDTO = execute(workflowApi, workFlowRequestDTO);

		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);

		log.info("******** End VM Migration assessment Flow ********");

		WorkFlowContextResponseDTO workflowOptions = workflowApi
				.getWorkflowParameters(workFlowResponseDTO.getWorkFlowExecutionId(), List.of("WORKFLOW_OPTIONS"));
		assertNotNull(workflowOptions);
		assertNotNull(workflowOptions.getWorkFlowOptions());
		assertNotNull(workflowOptions.getWorkFlowOptions().getNewOptions());
		String infrastructureOption = workflowOptions.getWorkFlowOptions().getNewOptions().get(0).getWorkFlowName();
		log.info("The Following Option Is Available: {}", infrastructureOption);

		workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(infrastructureOption);

		workFlowRequestDTO.addArgumentsItem(vmName).addArgumentsItem(apiUrl).addArgumentsItem(token);

		log.info("******** Start VM Migration task  ********");
		workFlowResponseDTO = execute(workflowApi, workFlowRequestDTO);
		log.info("******** End VM Migration task  ********");
	}

	private WorkFlowExecutionResponseDTO execute(WorkflowApi workflowApi, WorkFlowRequestDTO request)
			throws ApiException, InterruptedException {
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(request);
		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkStatusEnum.IN_PROGRESS.toString(), workFlowResponseDTO.getWorkStatus().toString());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = SdkUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO.getStatus());
		assertEquals(WorkStatusEnum.COMPLETED.toString(), workFlowStatusResponseDTO.getStatus().toString());
		return workFlowResponseDTO;

	}

}
