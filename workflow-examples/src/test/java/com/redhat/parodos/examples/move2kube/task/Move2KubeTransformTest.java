package com.redhat.parodos.examples.move2kube.task;

import java.util.UUID;

import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.api.PlanApi;
import dev.parodos.move2kube.api.ProjectOutputsApi;
import dev.parodos.move2kube.client.model.GetPlan200Response;
import dev.parodos.move2kube.client.model.StartTransformation202Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

public class Move2KubeTransformTest {

	Move2KubeTransform task;

	private static String move2KubeWorkspaceIDCtxKey = "move2KubeWorkspaceID";

	private static String move2KubeProjectIDCtxKey = "move2KubeProjectID";

	PlanApi planApi;

	ProjectOutputsApi projectOutputsApi;

	@Before
	public void setup() throws Exception {
		planApi = Mockito.mock(PlanApi.class);
		projectOutputsApi = Mockito.mock(ProjectOutputsApi.class);
		task = new Move2KubeTransform("http://localhost:8080", planApi, projectOutputsApi);
	}

	@Test
	public void testParameters() {
		assertEquals(this.task.getWorkFlowTaskParameters().size(), 0);
	}

	@Test
	public void testValidExecution() {

		// given
		WorkContext context = getSampleWorkContext();
		GetPlan200Response response = new GetPlan200Response();
		StartTransformation202Response transformResponse = new StartTransformation202Response();
		transformResponse.setId("foo");

		assertDoesNotThrow(() -> {
			Mockito.when(planApi.getPlan(Mockito.any(), Mockito.any())).thenReturn(new GetPlan200Response());
			Mockito.when(projectOutputsApi.startTransformation(Mockito.any(), Mockito.any(), Mockito.any()))
					.thenReturn(transformResponse);
		});

		try (MockedStatic<RestUtils> mockedStatic = mockStatic(RestUtils.class)) {
			ResponseEntity<String> responseo = ResponseEntity.ok("ok");
			responseo.getStatusCode();

			mockedStatic.when(
					(MockedStatic.Verification) RestUtils.executePost(Mockito.any(), (HttpEntity<?>) Mockito.any()))
					.thenReturn(ResponseEntity.ok("ok"));

			// when
			WorkReport report = this.task.execute(context);

			// then
			assertNull(report.getError());
			assertEquals(report.getStatus(), WorkStatus.COMPLETED);
		}

		assertDoesNotThrow(() -> {
			Mockito.verify(projectOutputsApi, Mockito.times(1)).startTransformation(
					Mockito.eq(move2KubeWorkspaceIDCtxKey), Mockito.eq(move2KubeProjectIDCtxKey), Mockito.any());
		});

	}

	public WorkContext getSampleWorkContext() {
		WorkContext workContext = new WorkContext();
		workContext.put(move2KubeProjectIDCtxKey, move2KubeProjectIDCtxKey);
		workContext.put(move2KubeWorkspaceIDCtxKey, move2KubeWorkspaceIDCtxKey);
		WorkContextUtils.setUserId(workContext, UUID.randomUUID());
		return workContext;
	}

}
