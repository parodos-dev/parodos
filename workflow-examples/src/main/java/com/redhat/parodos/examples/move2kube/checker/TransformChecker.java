package com.redhat.parodos.examples.move2kube.checker;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Strings;
import com.redhat.parodos.examples.move2kube.utils.Move2KubeUtils;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.ApiClient;
import dev.parodos.move2kube.ApiException;
import dev.parodos.move2kube.api.ProjectsApi;
import dev.parodos.move2kube.client.model.Project;
import dev.parodos.move2kube.client.model.ProjectOutputsValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.steppschuh.markdowngenerator.text.TextBuilder;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class TransformChecker extends BaseWorkFlowCheckerTask {

	private ApiClient client = null;

	private static String transformContextKey = "move2KubeTransformID";

	private static String workspaceContextKey = "move2KubeWorkspaceID";

	@Getter
	protected static String projectContextKey = "move2KubeProjectID";

	private final String server;

	public TransformChecker(String server) {
		client = new ApiClient();
		client.setBasePath(server);
		this.server = server;
	}

	@Value("${workflows.m2k.public_url}")
	private String publicUrl;

	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {

		String workspaceID = (String) workContext.get(workspaceContextKey);
		String projectID = (String) workContext.get(projectContextKey);
		String transformID = (String) workContext.get(transformContextKey);
		if (Strings.isNullOrEmpty(transformID)) {
			return new DefaultWorkReport(WorkStatus.PENDING, workContext,
					new IllegalArgumentException("There is no transform ID"));
		}
		ProjectsApi project = new ProjectsApi(client);
		try {
			Project res = project.getProject(workspaceID, projectID);
			ProjectOutputsValue output = Objects.requireNonNull(res.getOutputs()).get(transformID);
			if (output == null) {
				String errorMsg = "Cannot get the project transformation output from the list";
				taskLogger.logErrorWithSlf4j(errorMsg);
				return new DefaultWorkReport(WorkStatus.REJECTED, workContext, new IllegalArgumentException(errorMsg));
			}
			if (!Objects.equals(output.getStatus(), "done")) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext,
						getMessage(workspaceID, projectID, transformID));
			}
		}
		catch (ApiException e) {
			return new DefaultWorkReport(WorkStatus.REJECTED, workContext, new IllegalArgumentException(
					"Cannot get current project for the workflow, error:" + e.getMessage()));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.REJECTED, workContext,
					new IllegalArgumentException("Transform checker cannot be validated:" + e.getMessage()));
		}
		// reset alert message, if any
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, (String) null);
	}

	@Override
	public List<WorkParameter> getWorkFlowTaskParameters() {
		return Collections.emptyList();
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return Collections.emptyList();
	}

	private String getMessage(String workspaceID, String projectID, String transformId) {

		String serverURL = server;
		if (this.publicUrl != null && !this.publicUrl.isEmpty()) {
			serverURL = this.publicUrl;
		}
		String path;
		try {
			path = Move2KubeUtils.getPath(serverURL, workspaceID, projectID, transformId);
		}
		catch (URISyntaxException e) {
			path = "Cannot parse move2kube url " + server;
		}
		return new TextBuilder().heading("Alert", 1).newParagraph()
				.text("You need to complete some information for your transformation").newLine()
				.text("Please check this").link("link", path).end().toString();
	}

}
