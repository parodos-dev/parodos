package com.redhat.parodos.flows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.describedAs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO);
		assertThat(workFlowStatusResponseDTO.getStatus(), describedAs("Assessment workflow should be completed",
				equalTo(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED)));

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
		assertThat(workFlowDefinitions.size(), greaterThan(0));
		assertThat(workFlowDefinitions.get(0).getId(),
				describedAs("There is no valid Onboarding workflow id", is(notNullValue())));
		assertThat(workFlowDefinitions.get(0).getName(),
				describedAs("There is no valid Onboarding workflow name", equalTo(infrastructureOption)));
		log.info("Onboarding workflow id {}", workFlowDefinitions.get(0).getId());
		log.info("Onboarding workflow name {}", workFlowDefinitions.get(0).getName());

		WorkRequestDTO adGroupsTask = new WorkRequestDTO();
		adGroupsTask.setWorkName("adGroupsWorkFlowTask");
		adGroupsTask.setArguments(Arrays.asList(new ArgumentRequestDTO().key("userId").value("test-user-id"),
				new ArgumentRequestDTO().key("adGroups").value("adGroupsVALUE")));
		adGroupsTask.setType(WorkRequestDTO.TypeEnum.TASK);

		WorkRequestDTO namespaceWorkFlowTask = new WorkRequestDTO();
		namespaceWorkFlowTask.setWorkName("namespaceWorkFlowTask");
		namespaceWorkFlowTask.setArguments(Collections
				.singletonList(new ArgumentRequestDTO().key("projectId").value(UUID.randomUUID().toString())));
		namespaceWorkFlowTask.setType(WorkRequestDTO.TypeEnum.TASK);

		WorkRequestDTO sslCertificationWorkFlowTask = new WorkRequestDTO();
		sslCertificationWorkFlowTask.setWorkName("sslCertificationWorkFlowTask");
		sslCertificationWorkFlowTask
				.setArguments(Arrays.asList(new ArgumentRequestDTO().key("domainName").value("api.com"),
						new ArgumentRequestDTO().key("ipAddress").value("127.0.0.1")));
		sslCertificationWorkFlowTask.setType(WorkRequestDTO.TypeEnum.TASK);

		// splunkMonitoringWorkFlowTask and adGroupsWorkFlowTask are task of
		// subWorkFlowOne
		WorkRequestDTO subWorkFlowOne = new WorkRequestDTO();
		subWorkFlowOne.workName("subWorkFlowOne");
		subWorkFlowOne.setType(WorkRequestDTO.TypeEnum.WORKFLOW);
		subWorkFlowOne.addWorksItem(adGroupsTask);

		// namespaceWorkFlowTask is task of subWorkFlowTwo and subWorkFlowOne is a
		// sub-workflow
		WorkRequestDTO subWorkFlowTwo = new WorkRequestDTO();
		subWorkFlowTwo.workName("subWorkFlowTwo");
		subWorkFlowTwo.setType(WorkRequestDTO.TypeEnum.WORKFLOW);
		subWorkFlowTwo.addWorksItem(namespaceWorkFlowTask);
		subWorkFlowTwo.addWorksItem(subWorkFlowOne);

		// sslCertificationWorkFlowTask is task of subWorkFlowTwo and subWorkFlowTwo is a
		// sub-workflow
		WorkRequestDTO subWorkFlowThree = new WorkRequestDTO();
		subWorkFlowThree.workName("subWorkFlowThree");
		subWorkFlowThree.setType(WorkRequestDTO.TypeEnum.WORKFLOW);
		subWorkFlowThree.addWorksItem(sslCertificationWorkFlowTask);
		subWorkFlowThree.addWorksItem(subWorkFlowTwo);

		workFlowRequestDTO.setProjectId(components.project().getId());
		workFlowRequestDTO.setWorkFlowName(workFlowDefinitions.get(0).getName());
		workFlowRequestDTO.setWorks(Arrays.asList(subWorkFlowOne, subWorkFlowTwo, subWorkFlowThree, adGroupsTask,
				namespaceWorkFlowTask, sslCertificationWorkFlowTask));
		workFlowResponseDTO = workflowApi.execute(workFlowRequestDTO);

		assertThat(workFlowResponseDTO.getWorkFlowExecutionId(),
				describedAs("There is no valid WorkFlowExecutionId", is(notNullValue())));

		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());
		log.info("Onboarding workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("Onboarding workflow execution completed with status {}", workFlowStatusResponseDTO.getStatus());
	}

}
