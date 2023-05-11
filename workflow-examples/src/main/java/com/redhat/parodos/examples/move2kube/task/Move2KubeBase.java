package com.redhat.parodos.examples.move2kube.task;

import java.util.List;

import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import dev.parodos.move2kube.ApiClient;
import lombok.Getter;

public class Move2KubeBase extends BaseInfrastructureWorkFlowTask {

	@Getter
	protected static String workspaceContextKey = "move2KubeWorkspaceID";

	@Getter
	protected static String projectContextKey = "move2KubeProjectID";

	@Getter
	protected static String transformContextKey = "move2KubeTransformID";

	protected ApiClient client = null;

	protected void setClient(String server) {
		this.client = new ApiClient();
		this.client.setBasePath(server);
	}

	public void setAPIClient(ApiClient ApiClient) {
		client = ApiClient;
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of();
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		return null;
	}

}
