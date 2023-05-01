package com.redhat.parodos.examples.integration;

import com.redhat.parodos.examples.integration.utils.ExamplesUtils;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class SimpleWorkFlow {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

	private ApiClient apiClient;

	@Before
	public void setUp() throws IOException {
		apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));

	}

	@Test
	public void runSimpleWorkFlow() throws ApiException, InterruptedException {
		log.info("Running simple flow");

		ProjectResponseDTO testProject = ExamplesUtils.getProjectAsync(apiClient, projectName, projectDescription);

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
		assertEquals(WorkFlowType.INFRASTRUCTURE.toString(), simpleSequentialWorkFlowDefinition.getType());

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
		assertNull(workFlowResponseDTO.getWorkFlowOptions());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkFlowResponseDTO.WorkStatusEnum.COMPLETED, workFlowResponseDTO.getWorkStatus());

		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** Simple Sequence Flow Completed ********");
	}

}