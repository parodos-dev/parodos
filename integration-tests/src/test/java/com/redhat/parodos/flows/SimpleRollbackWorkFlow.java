package com.redhat.parodos.flows;

import java.util.List;

import com.redhat.parodos.flows.base.BaseIntegrationTest;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdkutils.SdkUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Richard Wang (Github: richrdW98)
 */
@Slf4j
public class SimpleRollbackWorkFlow extends BaseIntegrationTest {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

	@Test
	public void runRollbackWorkFlow() throws ApiException, InterruptedException {
		log.info("Running simple flow");

		ProjectResponseDTO testProject = SdkUtils.getProjectAsync(apiClient, projectName, projectDescription);

		// GET simpleSequentialWorkFlow DEFINITIONS
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi();
		List<WorkFlowDefinitionResponseDTO> simpleFailedWorkFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions("simpleFailedWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW);
		assertEquals(1, simpleFailedWorkFlowDefinitions.size());

		// GET WORKFLOW DEFINITION BY Id
		WorkFlowDefinitionResponseDTO simpleSequentialWorkFlowDefinition = workflowDefinitionApi
				.getWorkFlowDefinitionById(simpleFailedWorkFlowDefinitions.get(0).getId());

		assertNotNull(simpleSequentialWorkFlowDefinition.getId());
		assertEquals("simpleFailedWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW,
				simpleSequentialWorkFlowDefinition.getName());
		assertEquals(WorkFlowDefinitionResponseDTO.ProcessingTypeEnum.SEQUENTIAL,
				simpleSequentialWorkFlowDefinition.getProcessingType());
		assertEquals(WorkFlowType.INFRASTRUCTURE.toString(), simpleSequentialWorkFlowDefinition.getType().name());

		// Define WorkRequests

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(testProject.getId());
		workFlowRequestDTO.setWorkFlowName("simpleFailedWorkFlow_INFRASTRUCTURE_WORKFLOW");
		workFlowRequestDTO.setWorks(List.of());

		WorkflowApi workflowApi = new WorkflowApi();
		log.info("******** Running The Simple Failed Flow ********");

		WorkFlowResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = SdkUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO.getStatus());
		assertEquals(WorkStatusEnum.FAILED.name(), workFlowStatusResponseDTO.getStatus().name());
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** Simple Failed Flow Completed ********");
	}

}
