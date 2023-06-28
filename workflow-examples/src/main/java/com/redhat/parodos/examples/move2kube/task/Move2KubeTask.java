package com.redhat.parodos.examples.move2kube.task;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.base.Strings;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.ApiClient;
import dev.parodos.move2kube.ApiException;
import dev.parodos.move2kube.api.ProjectInputsApi;
import dev.parodos.move2kube.api.ProjectsApi;
import dev.parodos.move2kube.api.WorkspacesApi;
import dev.parodos.move2kube.client.model.CreateProject201Response;
import dev.parodos.move2kube.client.model.Project;
import dev.parodos.move2kube.client.model.ProjectInputsValue;
import dev.parodos.move2kube.client.model.Workspace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Move2KubeTask extends Move2KubeBase {

	private WorkspacesApi workspacesApi;

	private ProjectsApi projectsApi;

	private ProjectInputsApi projectInputsApi;

	public Move2KubeTask(String server) {
		super();
		this.setClient(server);
		workspacesApi = new WorkspacesApi(client);
		projectsApi = new ProjectsApi(client);

		ApiClient clientFormData = client;
		clientFormData.addDefaultHeader("Content-Type", "multipart/form-data");
		projectInputsApi = new ProjectInputsApi(clientFormData);
	}

	// constructor only used for testing.
	Move2KubeTask(String server, WorkspacesApi wrk, ProjectsApi projects, ProjectInputsApi projectInputs) {
		new Move2KubeTask(server);
		workspacesApi = wrk;
		projectsApi = projects;
		projectInputsApi = projectInputs;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	public WorkReport execute(WorkContext workContext) {
		log.debug("Init Move2Kube Project initialization!");
		String workspaceID = null;
		Map<String, ProjectInputsValue> workspaceInputs = null;
		try {
			Optional<Workspace> workspace = setWorkspace();
			if (workspace.isEmpty()) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext,
						new RuntimeException("No move2kube workspace found"));
			}
			workspaceID = workspace.get().getId();
			workspaceInputs = workspace.get().getInputs();
			workContext.put("move2KubeWorkspaceID", workspaceID);

			String projectId = setProject(workspaceID, workspaceInputs,
					WorkContextUtils.getMainExecutionId(workContext));
			if (Strings.isNullOrEmpty(projectId)) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext,
						new RuntimeException("Cannot create project on move2kube server"));
			}
			workContext.put("move2KubeProjectID", projectId);
		}
		catch (ApiException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Cannot setup the project on move2kube: %s".formatted(e.getMessage())));
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private Optional<Workspace> setWorkspace() throws ApiException {
		return workspacesApi.getWorkspaces().stream().findFirst();
	}

	private String setProject(String workspaceId, Map<String, ProjectInputsValue> inputs, UUID workflowId)
			throws ApiException {
		Project project = new Project();
		project.setName("WF- " + workflowId.toString().substring(0, 8));
		project.description("Project for workflow execution id: " + workflowId.toString());

		CreateProject201Response res = projectsApi.createProject(workspaceId, project);
		project.setId(res.getId());

		if (inputs == null) {
			return project.getId();
		}

		for (Map.Entry<String, ProjectInputsValue> v : inputs.entrySet()) {
			projectInputsApi.createProjectInput(workspaceId, project.getId(), "reference", v.getValue().getId(),
					v.getValue().getDescription(), null);
		}

		return project.getId();
	}

}
