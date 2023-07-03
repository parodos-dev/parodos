package com.redhat.parodos.flows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.redhat.parodos.sdk.model.WorkStatusResponseDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class SimpleRestartWorkFlowTest {

	private static final String WORKFLOW_NAME = "onboardingComplexAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW;

	@Test
	public void runRestartNotExistingWorkFlow() {
		TestComponents components = new WorkFlowTestBuilder().withDefaultProject().withWorkFlowDefinition(WORKFLOW_NAME)
				.build();
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());
		log.info("Restarting not existing WorkFlow");
		UUID execId = UUID.randomUUID();
		assertThat(assertThrows(ApiException.class, () -> workflowApi.restartWorkFlow(execId)))
				.hasMessageContaining(String.format("Workflow execution with ID: %s not found", execId))
				.hasMessageContaining("HTTP response code: 404");

	}

	@Test
	public void runRestartWorkFlow() throws ApiException {
		log.info("******** Running the assessment workFlow ********");
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

		WorkFlowContextResponseDTO workflowOptions = assertAssessmentWorkflowExecutionSuccess(workflowApi,
				workFlowResponseDTO, WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);
		String infrastructureOption = workflowOptions.getWorkFlowOptions().getNewOptions().get(0).getWorkFlowName();
		log.info("The Following Option Is Available: {}", infrastructureOption);

		log.info("Restarting the assessment WorkFlow");
		workFlowResponseDTO = workflowApi.restartWorkFlow(workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());
		log.info("restarted workflow submitted successfully with response: {}", workFlowResponseDTO);
		workflowOptions = assertAssessmentWorkflowExecutionSuccess(workflowApi, workFlowResponseDTO,
				WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);
		String infrastructureOptionRestarted = workflowOptions.getWorkFlowOptions().getNewOptions().get(0)
				.getWorkFlowName();
		log.info("The Following Option Is Available after restart: {}", infrastructureOptionRestarted);

		assertEquals(infrastructureOption, infrastructureOptionRestarted);

	}

	@Test
	public void runRestartWorkFlowComplex() throws ApiException {
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
		WorkFlowContextResponseDTO workflowOptions = assertAssessmentWorkflowExecutionSuccess(workflowApi,
				workFlowResponseDTO, WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO;
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

		assertNotNull("There is no valid WorkFlowExecutionId", workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());
		log.info("Onboarding workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("Onboarding workflow execution completed with status {}", workFlowStatusResponseDTO.getStatus());

		log.info("Restarting the onboarding WorkFlow");
		WorkFlowExecutionResponseDTO restartedWorkFlowResponseDTO = workflowApi
				.restartWorkFlow(workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.IN_PROGRESS, restartedWorkFlowResponseDTO.getWorkStatus());
		assertNotNull(restartedWorkFlowResponseDTO.getWorkFlowExecutionId());
		log.info("restarted workflow submitted successfully with response: {}", restartedWorkFlowResponseDTO);
		assertNotEquals(workFlowResponseDTO.getWorkFlowExecutionId(),
				restartedWorkFlowResponseDTO.getWorkFlowExecutionId());
		WorkFlowStatusResponseDTO restartedWorkFlowStatusResponseDTO = WorkFlowServiceUtils
				.waitWorkflowStatusAsync(workflowApi, restartedWorkFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(restartedWorkFlowStatusResponseDTO);
		List<String> works = workFlowStatusResponseDTO.getWorks().stream().map(WorkStatusResponseDTO::getName)
				.collect(Collectors.toList());
		List<String> restartedWorks = restartedWorkFlowStatusResponseDTO.getWorks().stream()
				.map(WorkStatusResponseDTO::getName).collect(Collectors.toList());
		assertEquals(works, restartedWorks);
		assertNotNull(restartedWorkFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, restartedWorkFlowStatusResponseDTO.getStatus());
		log.info("Restarted onboarding workflow execution completed with status {}",
				restartedWorkFlowStatusResponseDTO.getStatus());

		WorkFlowStatusResponseDTO originalWorkflowStatus = workflowApi
				.getStatus(workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(1, originalWorkflowStatus.getRestartedCount().intValue());
		WorkFlowStatusResponseDTO restartedWorkflowStatus = workflowApi
				.getStatus(restartedWorkFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowResponseDTO.getWorkFlowExecutionId(), restartedWorkflowStatus.getOriginalExecutionId());
	}

	@Test
	public void runRestartWorkFlowComplexMultipleTimes() throws ApiException, InterruptedException {
		log.info("******** Running The Complex workFlow multiple times ********");
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
		WorkFlowContextResponseDTO workflowOptions = assertAssessmentWorkflowExecutionSuccess(workflowApi,
				workFlowResponseDTO, WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO;
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

		assertNotNull("There is no valid WorkFlowExecutionId", workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());
		log.info("Onboarding workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		log.info("Onboarding workflow execution completed with status {}", workFlowStatusResponseDTO.getStatus());
		List<UUID> restartedIds = new ArrayList<>(5);
		for (int i = 1; i <= 5; i++) {
			log.info("********  Restarting the onboarding WorkFlow - {} ******** ", i);
			WorkFlowExecutionResponseDTO restartedWorkFlowResponseDTO = workflowApi
					.restartWorkFlow(workFlowResponseDTO.getWorkFlowExecutionId());
			assertEquals(WorkStatusEnum.IN_PROGRESS, restartedWorkFlowResponseDTO.getWorkStatus());
			assertNotNull(restartedWorkFlowResponseDTO.getWorkFlowExecutionId());
			log.info("restarted workflow submitted successfully with response: {}", restartedWorkFlowResponseDTO);
			assertNotEquals(workFlowResponseDTO.getWorkFlowExecutionId(),
					restartedWorkFlowResponseDTO.getWorkFlowExecutionId());
			assertFalse("Newly restarted execution ID shall not be in the list of already restarted execution ID",
					restartedIds.contains(restartedWorkFlowResponseDTO.getWorkFlowExecutionId()));
			restartedIds.add(restartedWorkFlowResponseDTO.getWorkFlowExecutionId());
			WorkFlowStatusResponseDTO restartedWorkFlowStatusResponseDTO = WorkFlowServiceUtils
					.waitWorkflowStatusAsync(workflowApi, restartedWorkFlowResponseDTO.getWorkFlowExecutionId());

			assertNotNull(restartedWorkFlowStatusResponseDTO);
			List<String> works = workFlowStatusResponseDTO.getWorks().stream().map(WorkStatusResponseDTO::getName)
					.collect(Collectors.toList());
			List<String> restartedWorks = restartedWorkFlowStatusResponseDTO.getWorks().stream()
					.map(WorkStatusResponseDTO::getName).collect(Collectors.toList());
			assertEquals(works, restartedWorks);
			assertNotNull(restartedWorkFlowStatusResponseDTO.getWorkFlowExecutionId());
			assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED,
					restartedWorkFlowStatusResponseDTO.getStatus());
			log.info("Restarted onboarding workflow execution completed with status {}",
					restartedWorkFlowStatusResponseDTO.getStatus());

			WorkFlowStatusResponseDTO originalWorkflowStatus = workflowApi
					.getStatus(workFlowResponseDTO.getWorkFlowExecutionId());
			assertEquals(i, originalWorkflowStatus.getRestartedCount().intValue());
			WorkFlowStatusResponseDTO restartedWorkflowStatus = workflowApi
					.getStatus(restartedWorkFlowStatusResponseDTO.getWorkFlowExecutionId());
			assertEquals(workFlowResponseDTO.getWorkFlowExecutionId(),
					restartedWorkflowStatus.getOriginalExecutionId());
		}
	}

	@Test
	public void runRestartWorkFlowComplexFailedStateBecauseMissingParameter()
			throws ApiException, InterruptedException {
		log.info("******** Running The Complex workFlow - should end in FAILED state********");
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
		WorkFlowContextResponseDTO workflowOptions = assertAssessmentWorkflowExecutionSuccess(workflowApi,
				workFlowResponseDTO, WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO;
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

		WorkRequestDTO adGroupsTask = new WorkRequestDTO();
		adGroupsTask.setWorkName("adGroupsWorkFlowTask");
		// userId is missing, workflow will end in FAILED state
		adGroupsTask.setArguments(
				Collections.singletonList(new ArgumentRequestDTO().key("adGroups").value("adGroupsVALUE")));
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

		assertNotNull("There is no valid WorkFlowExecutionId", workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());
		log.info("Onboarding workflow execution id: {}", workFlowResponseDTO.getWorkFlowExecutionId());

		workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId(), WorkFlowStatusResponseDTO.StatusEnum.FAILED);

		assertNotNull(workFlowStatusResponseDTO);
		assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.FAILED, workFlowStatusResponseDTO.getStatus());
		log.info("Onboarding workflow execution completed with status {}", workFlowStatusResponseDTO.getStatus());

		log.info("Restarting the onboarding WorkFlow");
		Thread.sleep(2000);
		WorkFlowExecutionResponseDTO restartedWorkFlowResponseDTO = workflowApi
				.restartWorkFlow(workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkStatusEnum.IN_PROGRESS, restartedWorkFlowResponseDTO.getWorkStatus());
		assertNotNull(restartedWorkFlowResponseDTO.getWorkFlowExecutionId());
		log.info("restarted workflow submitted successfully with response: {}", restartedWorkFlowResponseDTO);
		assertNotEquals(workFlowResponseDTO.getWorkFlowExecutionId(),
				restartedWorkFlowResponseDTO.getWorkFlowExecutionId());
		WorkFlowStatusResponseDTO restartedWorkFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(
				workflowApi, restartedWorkFlowResponseDTO.getWorkFlowExecutionId(),
				WorkFlowStatusResponseDTO.StatusEnum.FAILED);

		assertNotNull(restartedWorkFlowStatusResponseDTO);
		List<String> works = workFlowStatusResponseDTO.getWorks().stream().map(WorkStatusResponseDTO::getName)
				.collect(Collectors.toList());
		List<String> restartedWorks = restartedWorkFlowStatusResponseDTO.getWorks().stream()
				.map(WorkStatusResponseDTO::getName).collect(Collectors.toList());
		assertEquals(works, restartedWorks);
		assertNotNull(restartedWorkFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(WorkFlowStatusResponseDTO.StatusEnum.FAILED, restartedWorkFlowStatusResponseDTO.getStatus());
		log.info("Restarted onboarding workflow execution completed with status {}",
				restartedWorkFlowStatusResponseDTO.getStatus());

		WorkFlowStatusResponseDTO originalWorkflowStatus = workflowApi
				.getStatus(workFlowResponseDTO.getWorkFlowExecutionId());
		assertEquals(1, originalWorkflowStatus.getRestartedCount().intValue());
		WorkFlowStatusResponseDTO restartedWorkflowStatus = workflowApi
				.getStatus(restartedWorkFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowResponseDTO.getWorkFlowExecutionId(), restartedWorkflowStatus.getOriginalExecutionId());
		Thread.sleep(2000);
	}

	private static WorkFlowContextResponseDTO assertAssessmentWorkflowExecutionSuccess(WorkflowApi workflowApi,
			WorkFlowExecutionResponseDTO workFlowResponseDTO, WorkFlowStatusResponseDTO.StatusEnum expectedStatus)
			throws ApiException {
		// wait till assessment workflow is completed
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils.waitWorkflowStatusAsync(workflowApi,
				workFlowResponseDTO.getWorkFlowExecutionId());
		assertNotNull(workFlowStatusResponseDTO);
		assertThat(workFlowStatusResponseDTO.getStatus()).isEqualTo(expectedStatus);

		WorkFlowContextResponseDTO workflowOptions = workflowApi
				.getWorkflowParameters(workFlowResponseDTO.getWorkFlowExecutionId(), List.of("WORKFLOW_OPTIONS"));
		assertNotNull(workflowOptions);
		assertNotNull(workflowOptions.getWorkFlowOptions());
		assertNotNull(workflowOptions.getWorkFlowOptions().getNewOptions());

		return workflowOptions;
	}

}
