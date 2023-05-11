package com.redhat.parodos.examples.move2kube.checker;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Strings;
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

@Slf4j
public class TransformChecker extends BaseWorkFlowCheckerTask {

	private ApiClient client = null;

	private static String transformContextKey = "move2KubeTransformID";

	private static String workspaceContextKey = "move2KubeWorkspaceID";

	@Getter
	protected static String projectContextKey = "move2KubeProjectID";

	public TransformChecker(String server) {
		super();
		client = new ApiClient();
		client.setBasePath(server);
	}

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
				return new DefaultWorkReport(WorkStatus.FAILED, workContext,
						new IllegalArgumentException("Cannot get the project transformation output from the list"));
			}
			if (!Objects.equals(output.getStatus(), "done")) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext);
			}
		}
		catch (ApiException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new IllegalArgumentException(
					"Cannot get current project for the workflow, error:" + e.getMessage()));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new IllegalArgumentException("Transform checker cannot be validated:" + e.getMessage()));
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	@Override
	public List<WorkParameter> getWorkFlowTaskParameters() {
		return Collections.emptyList();
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return Collections.emptyList();
	}

}
