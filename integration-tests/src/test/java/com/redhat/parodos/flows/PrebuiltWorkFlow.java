package com.redhat.parodos.flows;

import java.util.Arrays;
import java.util.List;

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

import static com.redhat.parodos.sdkutils.SdkUtils.getProjectAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class PrebuiltWorkFlow {

	private static final String projectName = "project-1";

	private static final String projectDescription = "an example project";

	private ApiClient apiClient;

	@Before
	public void setUp() {
		apiClient = Configuration.getDefaultApiClient();
		apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));

	}

	@Test
	public void runPreBuiltWorkFlow() throws ApiException, InterruptedException {
		String workFlowName = "prebuiltWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW;

		log.info("Running pre-built flow (name: {})", workFlowName);

		ProjectResponseDTO testProject = getProjectAsync(apiClient, projectName, projectDescription);

		// GET preBuiltWorkFlow DEFINITIONS
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi(apiClient);
		List<WorkFlowDefinitionResponseDTO> prebuiltWorkFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions(workFlowName);
		assertEquals(1, prebuiltWorkFlowDefinitions.size());

		// GET WORKFLOW DEFINITION BY Id
		WorkFlowDefinitionResponseDTO prebuiltWorkFlowDefinition = workflowDefinitionApi
				.getWorkFlowDefinitionById(prebuiltWorkFlowDefinitions.get(0).getId());

		assertNotNull(prebuiltWorkFlowDefinition.getId());
		assertEquals(workFlowName, prebuiltWorkFlowDefinition.getName());
		assertEquals(WorkFlowDefinitionResponseDTO.ProcessingTypeEnum.SEQUENTIAL,
				prebuiltWorkFlowDefinition.getProcessingType());
		assertEquals(WorkFlowType.INFRASTRUCTURE.toString(), prebuiltWorkFlowDefinition.getType());

		assertNotNull(prebuiltWorkFlowDefinition.getWorks());
		assertEquals(1, prebuiltWorkFlowDefinition.getWorks().size());
		assertEquals("notificationTask", prebuiltWorkFlowDefinition.getWorks().get(0).getName());
		assertEquals(WorkType.TASK.toString(), prebuiltWorkFlowDefinition.getWorks().get(0).getWorkType());
		assertTrue(CollectionUtils.isEmpty(prebuiltWorkFlowDefinition.getWorks().get(0).getWorks()));
		assertNull(prebuiltWorkFlowDefinition.getWorks().get(0).getProcessingType());
		assertNotNull(prebuiltWorkFlowDefinition.getWorks().get(0).getParameters());

		// Define WorkRequests
		WorkRequestDTO work1 = new WorkRequestDTO();
		work1.setWorkName("notificationTask");
		work1.setArguments(Arrays.asList(new ArgumentRequestDTO().key("type").value("test-type"),
				new ArgumentRequestDTO().key("body").value("test body"),
				new ArgumentRequestDTO().key("subject").value("test subject"),
				new ArgumentRequestDTO().key("userNames").value("test-username")));

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(testProject.getId());
		workFlowRequestDTO.setWorkFlowName(workFlowName);
		workFlowRequestDTO.setWorks(List.of(work1));

		WorkflowApi workflowApi = new WorkflowApi(apiClient);
		log.info("******** Running The PreBuilt Flow ********");
		WorkFlowResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
		assertNull(workFlowResponseDTO.getWorkFlowOptions());
		assertNotNull(workFlowResponseDTO.getWorkStatus());
		assertEquals(WorkFlowResponseDTO.WorkStatusEnum.COMPLETED, workFlowResponseDTO.getWorkStatus());

		log.info("workflow finished successfully with response: {}", workFlowResponseDTO);
		log.info("******** PreBuilt Sequence Flow Completed ********");
	}

}
