package com.redhat.parodos.flows;

import java.util.Arrays;
import java.util.List;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.flows.common.WorkFlowTestBuilder.TestComponents;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowContextResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO.WorkStatusEnum;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.sdkutils.SdkUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public class ComplexWorkFlowTest {

	private static final String WORKFLOW_NAME = "onboardingComplexAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW;

	@Test
	public void runComplexWorkFlow() throws ApiException, InterruptedException {
		log.info("******** Running The Complex workFlow ********");
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject().withWorkFlowDefinition(WORKFLOW_NAME)
				.build();

		// Define WorkFlowRequest
		WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO();
		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(WORKFLOW_NAME);
		workFlowRequestDTO.setWorks(List.of(new WorkRequestDTO()
				.arguments(List.of(new ArgumentRequestDTO().key("GIT_REPO_URL").value("git_repo_url")))));

		log.info("Running the Assessment to see what WorkFlows are eligible for this situation:");
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());
		log.info("workflow submitted successfully with response: {}", workFlowResponseDTO);

		// wait till assessment workflow is completed
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = SdkUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO);
		assertThat(workFlowStatusResponseDTO.getStatus()).as("Assessment workflow should be completed")
				.isEqualTo(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);

		WorkFlowContextResponseDTO workflowOptions = workflowApi
				.getWorkflowParameters(workFlowResponseDTO.getWorkFlowExecutionId(), List.of("WORKFLOW_OPTIONS"));
		assertNotNull(workflowOptions);
		assertNotNull(workflowOptions.getWorkFlowOptions());
		assertNotNull(workflowOptions.getWorkFlowOptions().getNewOptions());
		String infrastructureOption = workflowOptions.getWorkFlowOptions().getNewOptions().get(0).getWorkFlowName();
		log.info("The Following Option Is Available: {}", infrastructureOption);

		log.info("Running the onboarding WorkFlow");
		log.info("executes 3 tasks in Parallel with a WorkFlowChecker");
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi();
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions(infrastructureOption);

		assertNotNull(workFlowDefinitions);
		assertTrue(workFlowDefinitions.size() > 0);
		assertNotNull("There is no valid Onboarding workflow id", workFlowDefinitions.get(0).getId());
		assertEquals("There is no valid Onboarding workflow name", workFlowDefinitions.get(0).getName(),
				infrastructureOption);
		log.info("Onboarding workflow id {}", workFlowDefinitions.get(0).getId());
		log.info("Onboarding workflow name {}", workFlowDefinitions.get(0).getName());

		WorkRequestDTO work1 = new WorkRequestDTO();
		work1.setWorkName("certWorkFlowTask");
		work1.setArguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("api.com")));

		WorkRequestDTO work2 = new WorkRequestDTO();
		work2.setWorkName("adGroupWorkFlowTask");
		work2.setArguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("api.com")));

		WorkRequestDTO work3 = new WorkRequestDTO();
		work3.setWorkName("dynatraceWorkFlowTask");
		work3.setArguments(Arrays.asList(new ArgumentRequestDTO().key("user-id").value("test-user-id"),
				new ArgumentRequestDTO().key("api-server").value("api.com")));

		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(workFlowDefinitions.get(0).getName());
		workFlowRequestDTO.setWorks(Arrays.asList(work1, work2, work3));
		workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertNotNull("There is no valid WorkFlowExecutionId", workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());
		log.info("Onboarding workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		workFlowStatusResponseDTO = SdkUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("Onboarding workflow execution completed with status {}", workFlowStatusResponseDTO.getStatus());
	}

}
