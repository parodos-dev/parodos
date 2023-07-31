package com.redhat.parodos.flows.common;

import java.util.List;
import java.util.function.Consumer;

import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

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
		assertThat(projectName, is(not(blankOrNullString())));
		assertThat(projectDescription, is(not(blankOrNullString())));
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		return this;
	}

	private void setupProject() throws ApiException {
		this.testProject = WorkFlowServiceUtils.getProjectAsync(apiClient, projectName, projectDescription);
		assertThat(testProject, is(notNullValue()));
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
		WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi(this.apiClient);

		// Get workflow definition by name
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitions = workflowDefinitionApi
				.getWorkFlowDefinitions(this.workFlowName);
		assertThat(workFlowDefinitions, hasSize(1));
		assertThat(workFlowDefinitions.get(0), is(notNullValue()));
		assertThat(workFlowDefinitions.get(0).getName(), equalTo(this.workFlowName));
		assertThat(workFlowDefinitions.get(0).getId(), is(notNullValue()));

		// Get workflow definition by ID
		WorkFlowDefinitionResponseDTO workFlowDefinition = workflowDefinitionApi
				.getWorkFlowDefinitionById(workFlowDefinitions.get(0).getId());

		this.workFlowDefinitionConsumer.accept(workFlowDefinition);
	}

	private void setupClient() throws InterruptedException, ApiException {
		this.apiClient = WorkFlowServiceUtils.getParodosAPiClient();
		assertThat(apiClient, is(notNullValue()));
	}

	public WorkFlowTestBuilder withWorkFlowDefinition(String workflowName,
			Consumer<WorkFlowDefinitionResponseDTO> consumer) {
		assertThat(workflowName, is(not(blankOrNullString())));
		this.workFlowName = workflowName;
		this.workFlowDefinitionConsumer = consumer;
		return this;
	}

	public WorkFlowTestBuilder withWorkFlowDefinition(String workFlowName) {
		assertThat(workFlowName, is(not(blankOrNullString())));
		this.workFlowName = workFlowName;
		this.workFlowDefinitionConsumer = (workFlowDefinition) -> {
			assertThat(workFlowDefinition.getId(), is(notNullValue()));
			assertThat(workFlowDefinition.getName(), equalTo(this.workFlowName));
		};
		return this;
	}

	public record TestComponents(ApiClient apiClient, ProjectResponseDTO project) {
	}

}
