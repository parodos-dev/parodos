package com.redhat.parodos.examples.move2kube.task;

import java.io.IOException;
import java.net.URISyntaxException;

import com.redhat.parodos.examples.move2kube.utils.Move2KubeUtils;
import com.redhat.parodos.infrastructure.Notifier;
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

import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class Move2KubeTransform extends Move2KubeBase {

	private PlanApi planApi;

	private ProjectOutputsApi projectOutputsApi;

	private Notifier notifierBus;

	private String plan;

	private String server;

	@Value("${workflows.m2k.public_url}")
	private String publicUrl;

	public Move2KubeTransform(String serverUrl, Notifier notifier) {
		super();
		this.setClient(serverUrl);
		planApi = new PlanApi(client);
		projectOutputsApi = new ProjectOutputsApi(client);
		notifierBus = notifier;
		server = serverUrl;
	}

	public Move2KubeTransform(String serverUrl, Notifier notifier, PlanApi plan, ProjectOutputsApi projectOutputs) {
		new Move2KubeTransform(serverUrl, notifier);
		planApi = plan;
		projectOutputsApi = projectOutputs;
		notifierBus = notifier;
		server = serverUrl;
	}

	public WorkReport execute(WorkContext workContext) {
		String workspaceId = (String) workContext.get(getWorkspaceContextKey());
		String projectId = (String) workContext.get(getProjectContextKey());
		String transformId = null;

		try {
			isPlanCreated(workspaceId, projectId);
		}
		catch (ApiException | RuntimeException e) {
			String errorMessage = "Plan for workspace '%s'' and project '%s' is not created".formatted(workspaceId,
					projectId);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new IllegalArgumentException(errorMessage));
		}

		try {
			transformId = transform(workspaceId, projectId);
			workContext.put(getTransformContextKey(), transformId);
		}
		catch (IllegalArgumentException | IOException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (ApiException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		String message = sendNotification(workspaceId, projectId, transformId);
		if (message == null) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Cannot notify user about the transformation status"));
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, message);
	}

	private String sendNotification(String workspaceID, String projectID, String outputID) {
		String message;

		String serverURL = server;
		if (this.publicUrl != null && !this.publicUrl.isEmpty()) {
			serverURL = this.publicUrl;
		}

		try {
			String url = Move2KubeUtils.getPath(serverURL, workspaceID, projectID, outputID);
			message = String.format(
					"You need to complete some information for your transformation in the following [url](%s)", url);

			notifierBus.send("Move2kube workflow approval needed", message);
		}
		catch (URISyntaxException e) {
			log.error("Cannot parse move2kube url {}", server);
			return null;
		}
		return message;
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
