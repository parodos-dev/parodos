package com.redhat.parodos.flows.common;

import java.util.List;
import java.util.function.Consumer;

import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdkutils.SdkUtils;

import static com.redhat.parodos.sdkutils.SdkUtils.getParodosAPiClient;
import static org.assertj.core.api.Assertions.assertThat;

public class WorkFlowTestBuilder {

	private static final String DEFAULT_PROJECT_NAME = "project-1";

	private static final String DEFAULT_PROJECT_DESCRIPTION = "an example project";

	private ProjectResponseDTO testProject;

	private String projectName;

	private String projectDescription;

	private String workFlowName;

	private ApiClient apiClient;

	private Consumer<WorkFlowDefinitionResponseDTO> workFlowDefinitionConsumer;

	public WorkFlowTestBuilder withDefaultProject() {
		this.projectName = DEFAULT_PROJECT_NAME;
		this.projectDescription = DEFAULT_PROJECT_DESCRIPTION;
		return this;
	}

	public WorkFlowTestBuilder withProject(String projectName, String projectDescription) {
		assertThat(projectName).isNotBlank();
		assertThat(projectDescription).isNotBlank();
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		return this;
	}

	private void setupProject() throws ApiException {
		this.testProject = SdkUtils.getProjectAsync(apiClient, projectName, projectDescription);
		assertThat(testProject).isNotNull();
	}

	public TestComponents build() {
		try {
			setupClient();
			setupProject();
			setupWorkFlowDefinition();
		}
		catch (InterruptedException | ApiException e) {
			throw new RuntimeException(e);
		}

		return new TestComponents(apiClient, testProject);
	}

	// Test two methods to retrieve the workflow definition: one by name and one by id
	// The consumer is used to test the workflow definition.
	private void setupWorkFlowDefinition() throws ApiException {
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi(apiClient);

		// Get workflow definition by name
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions(workFlowName);
		assertThat(workFlowDefinitions.size()).isEqualTo(1);
		assertThat(workFlowDefinitions.get(0)).isNotNull();
		assertThat(workFlowDefinitions.get(0).getName()).isEqualTo(workFlowName);
		assertThat(workFlowDefinitions.get(0).getId()).isNotNull();

		// Get workflow definition by ID
		WorkFlowDefinitionResponseDTO workFlowDefinition = workflowDefinitionApi
				.getWorkFlowDefinitionById(workFlowDefinitions.get(0).getId());

		workFlowDefinitionConsumer.accept(workFlowDefinition);
	}

	private void setupClient() throws InterruptedException, ApiException {
		apiClient = getParodosAPiClient();
		assertThat(apiClient).isNotNull();
	}

	public WorkFlowTestBuilder withWorkFlowDefinition(String workflowName,
			Consumer<WorkFlowDefinitionResponseDTO> consumer) {
		assertThat(workflowName).isNotBlank();
		this.workFlowName = workflowName;
		this.workFlowDefinitionConsumer = consumer;
		return this;
	}

	public WorkFlowTestBuilder withWorkFlowDefinition(String workflowName) {
		assertThat(workflowName).isNotBlank();
		this.workFlowName = workflowName;
		this.workFlowDefinitionConsumer = (workFlowDefinition) -> {
			assertThat(workFlowDefinition.getId()).isNotNull();
			assertThat(workFlowDefinition.getName()).isEqualTo(workflowName);
		};
		return this;
	}

	public record TestComponents(ApiClient apiClient, ProjectResponseDTO project) {
	}

}
