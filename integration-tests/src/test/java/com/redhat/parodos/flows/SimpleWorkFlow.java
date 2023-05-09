package com.redhat.parodos.flows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.redhat.parodos.flows.base.BaseIntegrationTest;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.sdkutils.SdkUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.enums.WorkType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import org.springframework.util.CollectionUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class SimpleWorkFlow extends BaseIntegrationTest {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

	@Test
	public void runSimpleWorkFlow() throws ApiException, InterruptedException {
		log.info("Running simple flow");

		ProjectResponseDTO testProject = SdkUtils.getProjectAsync(apiClient, projectName, projectDescription);

		// GET simpleSequentialWorkFlow DEFINITIONS
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi();
		List<WorkFlowDefinitionResponseDTO> simpleSequentialWorkFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions("simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW);
		assertEquals(1, simpleSequentialWorkFlowDefinitions.size());

		// GET WORKFLOW DEFINITION BY Id
		WorkFlowDefinitionResponseDTO simpleSequentialWorkFlowDefinition = workflowDefinitionApi
				.getWorkFlowDefinitionById(simpleSequentialWorkFlowDefinitions.get(0).getId());

		assertNotNull(simpleSequentialWorkFlowDefinition.getId());
		assertEquals("simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW,
				simpleSequentialWorkFlowDefinition.getName());
		assertEquals(WorkFlowDefinitionResponseDTO.ProcessingTypeEnum.SEQUENTIAL,
				simpleSequentialWorkFlowDefinition.getProcessingType());
		assertEquals(WorkFlowDefinitionResponseDTO.TypeEnum.INFRASTRUCTURE,
				simpleSequentialWorkFlowDefinition.getType());

		assertNotNull(simpleSequentialWorkFlowDefinition.getWorks());
		assertEquals(2, simpleSequentialWorkFlowDefinition.getWorks().size());
		assertEquals("restCallTask", simpleSequentialWorkFlowDefinition.getWorks().get(0).getName());
		assertEquals(WorkType.TASK.toString(), simpleSequentialWorkFlowDefinition.getWorks().get(0).getWorkType());
		assertTrue(CollectionUtils.isEmpty(simpleSequentialWorkFlowDefinition.getWorks().get(0).getWorks()));
		assertNull(simpleSequentialWorkFlowDefinition.getWorks().get(0).getProcessingType());
		assertNotNull(simpleSequentialWorkFlowDefinition.getWorks().get(0).getParameters());

		assertEquals("loggingTask", simpleSequentialWorkFlowDefinition.getWorks().get(1).getName());
		assertEquals(WorkType.TASK.toString(), simpleSequentialWorkFlowDefinition.getWorks().get(1).getWorkType());
		assertTrue(CollectionUtils.isEmpty(simpleSequentialWorkFlowDefinition.getWorks().get(1).getWorks()));
		assertNull(simpleSequentialWorkFlowDefinition.getWorks().get(1).getProcessingType());
		assertNotNull(simpleSequentialWorkFlowDefinition.getWorks().get(1).getParameters());

		// Define WorkRequests
		WorkRequestDTO work1 = new WorkRequestDTO();
		work1.setWorkName("restCallTask");
		work1.setArguments(Collections
				.singletonList(new ArgumentRequestDTO().key("url").value("http://localhost:8080/actuator/health")));

		WorkRequestDTO work2 = new WorkRequestDTO();
		work2.setWorkName("loggingTask");
		work2.setArguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("test-api-server")));

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(testProject.getId());
		workFlowRequestDTO.setWorkFlowName("simpleSequentialWorkFlow_INFRASTRUCTURE_WORKFLOW");
		workFlowRequestDTO.setWorks(Arrays.asList(work1, work2));

		WorkflowApi workflowApi = new WorkflowApi();
		log.info("******** Running The Simple Sequence Flow ********");

		WorkFlowResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = SdkUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO.getStatus());
		assertEquals(WorkStatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** Simple Sequence Flow Completed ********");
	}

}
