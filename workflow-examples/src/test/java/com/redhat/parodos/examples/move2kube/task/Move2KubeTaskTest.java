package com.redhat.parodos.examples.move2kube.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.ApiException;
import dev.parodos.move2kube.api.ProjectInputsApi;
import dev.parodos.move2kube.api.ProjectsApi;
import dev.parodos.move2kube.api.WorkspacesApi;
import dev.parodos.move2kube.client.model.CreateProject201Response;
import dev.parodos.move2kube.client.model.ProjectInputsValue;
import dev.parodos.move2kube.client.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class Move2KubeTaskTest {

	Move2KubeTask task;

	private WorkspacesApi workspacesApi;

	private ProjectsApi projectsApi;

	private ProjectInputsApi projectInputsApi;

	private static String move2KubeWorkspaceIDCtxKey = "move2KubeWorkspaceID";

	private static String move2KubeProjectIDCtxKey = "move2KubeProjectID";

	@Before
	public void BeforeEach() {
		workspacesApi = Mockito.mock(WorkspacesApi.class);
		projectsApi = Mockito.mock(ProjectsApi.class);
		projectInputsApi = Mockito.mock(ProjectInputsApi.class);

		task = new Move2KubeTask("http://localhost", workspacesApi, projectsApi, projectInputsApi);
		log.error("Move2KubeTask BeforeEach");
	}

	@After
	public void AfterEach() {
		log.error("Move2KubeTask AfterEach");
	}

	@Test
	public void testValidExecution() {
		// given
		WorkContext workContext = getSampleWorkContext();
		Workspace workspace = getSampleWorkspace("test");

		Map<String, ProjectInputsValue> inputs = new HashMap<String, ProjectInputsValue>();
		inputs.put("test1", getSampleProjectInput("test1"));
		inputs.put("test2", getSampleProjectInput("test2"));
		workspace.setInputs(inputs);

		assertDoesNotThrow(() -> {
			Mockito.when(workspacesApi.getWorkspaces()).thenReturn(List.of(workspace));
			Mockito.when(projectsApi.createProject(Mockito.any(), Mockito.any())).thenReturn(getSampleProject("test"));
		});

		// when
		WorkReport report = task.execute(workContext);

		// then
		assertNull(report.getError());
		assertEquals(report.getStatus(), WorkStatus.COMPLETED);
		assertNotNull(report.getWorkContext().get(move2KubeWorkspaceIDCtxKey));
		assertNotNull(report.getWorkContext().get(move2KubeProjectIDCtxKey));

		assertDoesNotThrow(() -> {
			Mockito.verify(workspacesApi, Mockito.times(1)).getWorkspaces();
			Mockito.verify(projectsApi, Mockito.times(1)).createProject(Mockito.any(), Mockito.any());
			Mockito.verify(projectInputsApi, Mockito.times(2)).createProjectInput(Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		});
	}

	@Test
	public void testWithoutValidWorkspace() {
		// given
		WorkContext workContext = getSampleWorkContext();
		assertDoesNotThrow(() -> {
			Mockito.when(workspacesApi.getWorkspaces()).thenReturn(Collections.emptyList());
			Mockito.when(projectsApi.createProject(Mockito.any(), Mockito.any())).thenReturn(getSampleProject("test"));
		});

		// when
		WorkReport report = task.execute(workContext);

		// then
		assertNotNull(report.getError());
		assertEquals(report.getStatus(), WorkStatus.FAILED);
		assertNull(report.getWorkContext().get(move2KubeWorkspaceIDCtxKey));
		assertNull(report.getWorkContext().get(move2KubeProjectIDCtxKey));

		assertDoesNotThrow(() -> {
			Mockito.verify(workspacesApi, Mockito.times(1)).getWorkspaces();
			Mockito.verify(projectsApi, Mockito.times(0)).createProject(Mockito.any(), Mockito.any());
		});
	}

	@Test
	public void testWithIssuesCreatingProject() {
		// given
		WorkContext workContext = getSampleWorkContext();
		assertDoesNotThrow(() -> {
			Mockito.when(workspacesApi.getWorkspaces()).thenReturn(List.of(getSampleWorkspace("test")));
			Mockito.when(projectsApi.createProject(Mockito.any(), Mockito.any())).thenThrow(ApiException.class);
		});

		// when
		WorkReport report = task.execute(workContext);

		// then
		assertNotNull(report.getError());
		assertEquals(report.getStatus(), WorkStatus.FAILED);
		assertNotNull(report.getWorkContext().get(move2KubeWorkspaceIDCtxKey));
		assertNull(report.getWorkContext().get(move2KubeProjectIDCtxKey));

		assertDoesNotThrow(() -> {
			Mockito.verify(workspacesApi, Mockito.times(1)).getWorkspaces();
			Mockito.verify(projectsApi, Mockito.times(1)).createProject(Mockito.any(), Mockito.any());
		});
	}

	public Workspace getSampleWorkspace(String workspace) {
		var wrk = new Workspace();
		wrk.setId(UUID.randomUUID().toString());
		wrk.setName(workspace);
		return wrk;
	}

	public CreateProject201Response getSampleProject(String name) {
		CreateProject201Response project = new CreateProject201Response();
		project.setId(UUID.randomUUID().toString());
		return project;
	}

	public WorkContext getSampleWorkContext() {
		WorkContext workContext = new WorkContext();
		WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());
		return workContext;
	}

	public ProjectInputsValue getSampleProjectInput(String name) {
		ProjectInputsValue projectInput = new ProjectInputsValue();
		projectInput.setName(name);
		projectInput.setDescription(name);
		projectInput.setId(UUID.randomUUID().toString());
		return projectInput;
	}

}
