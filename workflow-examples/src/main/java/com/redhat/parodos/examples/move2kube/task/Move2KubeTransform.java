package com.redhat.parodos.examples.move2kube.task;

import java.io.IOException;
import java.util.List;

import com.redhat.parodos.examples.ocponboarding.task.dto.notification.NotificationRequest;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.ApiException;
import dev.parodos.move2kube.api.PlanApi;
import dev.parodos.move2kube.api.ProjectOutputsApi;
import dev.parodos.move2kube.client.model.GetPlan200Response;
import dev.parodos.move2kube.client.model.StartTransformation202Response;
import dev.parodos.move2kube.client.model.StartTransformationRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@Slf4j
public class Move2KubeTransform extends Move2KubeBase {

	private PlanApi planApi;

	private ProjectOutputsApi projectOutputsApi;

	private String plan;

	public Move2KubeTransform(String server) {
		super();
		this.setClient(server);
		planApi = new PlanApi(client);
		projectOutputsApi = new ProjectOutputsApi(client);
	}

	public Move2KubeTransform(String server, PlanApi plan, ProjectOutputsApi projectOutputs) {
		new Move2KubeTransform(server);
		planApi = plan;
		projectOutputsApi = projectOutputs;
	}

	public WorkReport execute(WorkContext workContext) {
		String workspaceId = (String) workContext.get(getWorkspaceContextKey());
		String projectId = (String) workContext.get(getProjectContextKey());
		try {
			isPlanCreated(workspaceId, projectId);
		}
		catch (ApiException | RuntimeException e) {
			String errorMessage = "Plan for workspace '%s'' and project '%s' is not created".formatted(workspaceId,
					projectId);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new IllegalArgumentException(errorMessage));
		}

		try {
			String transformId = transform(workspaceId, projectId);
			workContext.put(getTransformContextKey(), transformId);
		}
		catch (IllegalArgumentException | IOException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (ApiException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		String userId = String.valueOf(WorkContextUtils.getUserId(workContext));
		if (!sendNotification(userId, workspaceId, projectId)) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Cannot notify user about the transformation status"));
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private boolean sendNotification(String userID, String workspaceID, String projectID) {

		String url = String.format("http://localhost:8081/workspaces/%s/projects/%s", workspaceID, projectID);
		String message = String.format(
				"You need to complete some information for your transformation in the following url <a href=\"%s\"> %s</a>",
				url, url);

		// @TODO userID is the ID, but we need the username, so hardcode it here for now.
		NotificationRequest request = NotificationRequest.builder().usernames(List.of("test"))
				.subject("Complete the Move2Kube transformation steps").body(message).build();

		HttpEntity<NotificationRequest> notificationRequestHttpEntity = RestUtils.getRequestWithHeaders(request, "test",
				"test");
		ResponseEntity<String> response = RestUtils.executePost("http://localhost:8082/api/v1/messages",
				notificationRequestHttpEntity);

		return response.getStatusCode().is2xxSuccessful();
	}

	private String transform(String workspaceID, String projectID)
			throws IllegalArgumentException, ApiException, IOException {
		StartTransformation202Response response = projectOutputsApi.startTransformation(workspaceID, projectID,
				StartTransformationRequest.fromJson(plan));
		if (response == null) {
			throw new IllegalArgumentException("Cannot start transformation");
		}
		return response.getId();
	}

	private void isPlanCreated(String workspaceID, String projectID) throws ApiException {
		GetPlan200Response response = planApi.getPlan(workspaceID, projectID);
		if (response == null) {
			throw new RuntimeException("Plan cannot be retrieved");
		}
		this.plan = response.toJson();
	}

}