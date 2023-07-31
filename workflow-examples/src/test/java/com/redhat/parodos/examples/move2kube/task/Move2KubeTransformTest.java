package com.redhat.parodos.examples.move2kube.task;

import java.util.UUID;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.api.PlanApi;
import dev.parodos.move2kube.api.ProjectOutputsApi;
import dev.parodos.move2kube.client.model.GetPlan200Response;
import dev.parodos.move2kube.client.model.StartTransformation202Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Move2KubeTransformTest {

	Move2KubeTransform task;

	private static final String MOVE2KUBE_WORKSPACE_ID_CONTEXT_KEY = "move2KubeWorkspaceID";

	private static final String MOVE2KUBE_PROJECT_ID_CONTEXT_KEY = "move2KubeProjectID";

	private PlanApi planApi;

	private Notifier notifierBus;

	private ProjectOutputsApi projectOutputsApi;

	@BeforeEach
	public void setup() throws Exception {
		planApi = mock(PlanApi.class);
		projectOutputsApi = mock(ProjectOutputsApi.class);
		notifierBus = mock(Notifier.class);
		task = new Move2KubeTransform("http://localhost:8080", notifierBus, planApi, projectOutputsApi);
	}

	@Test
	public void testParameters() {
		assertThat(this.task.getWorkFlowTaskParameters(), hasSize(0));
	}

	@Test
	public void testValidExecution() {

		// given
		WorkContext context = getSampleWorkContext();
		StartTransformation202Response transformResponse = new StartTransformation202Response();
		transformResponse.setId("foo");

		assertDoesNotThrow(() -> {
			when(planApi.getPlan(any(), any())).thenReturn(new GetPlan200Response());
			when(projectOutputsApi.startTransformation(any(), any(), any())).thenReturn(transformResponse);
		});

		try (MockedStatic<RestUtils> mockedStatic = mockStatic(RestUtils.class)) {
			ResponseEntity<String> response = ResponseEntity.ok("ok");
			response.getStatusCode();

			mockedStatic.when((MockedStatic.Verification) RestUtils.executePost(any(), (HttpEntity<?>) any()))
					.thenReturn(ResponseEntity.ok("ok"));

			// when
			WorkReport report = this.task.execute(context);

			// then
			assertThat(report.getError(), is(nullValue()));
			assertThat(report.getStatus(), equalTo(WorkStatus.COMPLETED));
		}

		assertDoesNotThrow(() -> {
			verify(projectOutputsApi, times(1)).startTransformation(eq(MOVE2KUBE_WORKSPACE_ID_CONTEXT_KEY),
					eq(MOVE2KUBE_PROJECT_ID_CONTEXT_KEY), any());
		});

	}

	public WorkContext getSampleWorkContext() {
		WorkContext workContext = new WorkContext();
		workContext.put(MOVE2KUBE_PROJECT_ID_CONTEXT_KEY, MOVE2KUBE_PROJECT_ID_CONTEXT_KEY);
		workContext.put(MOVE2KUBE_WORKSPACE_ID_CONTEXT_KEY, MOVE2KUBE_WORKSPACE_ID_CONTEXT_KEY);
		WorkContextUtils.setUserId(workContext, UUID.randomUUID());
		return workContext;
	}

}
