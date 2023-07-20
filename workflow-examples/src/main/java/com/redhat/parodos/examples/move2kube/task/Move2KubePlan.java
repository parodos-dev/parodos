package com.redhat.parodos.examples.move2kube.task;

import java.io.File;

import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.ApiClient;
import dev.parodos.move2kube.ApiException;
import dev.parodos.move2kube.api.PlanApi;
import dev.parodos.move2kube.api.ProjectInputsApi;
import dev.parodos.move2kube.client.model.GetPlan200Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@Slf4j
public class Move2KubePlan extends Move2KubeBase {

	private PlanApi planApi;

	@Setter
	private long sleepTime = 1000;

	private ProjectInputsApi projectInputsApi;

	public Move2KubePlan(String server) {
		super();
		this.setClient(server);
		planApi = new PlanApi(client);

		ApiClient clientFormData = client;
		clientFormData.addDefaultHeader("Content-Type", "multipart/form-data");
		projectInputsApi = new ProjectInputsApi(clientFormData);
	}

	// only used for testing
	public Move2KubePlan(String server, PlanApi plan, ProjectInputsApi projectInputs) {
		new Move2KubePlan(server);
		this.setClient(server);
		planApi = plan;
		projectInputsApi = projectInputs;
	}

	public WorkReport execute(WorkContext workContext) {
		String workspaceID = (String) workContext.get(getWorkspaceContextKey());
		String projectID = (String) workContext.get(getProjectContextKey());
		addSourceCode(workspaceID, projectID, (String) workContext.get("gitArchivePath"));
		try {
			planApi.startPlanning(workspaceID, projectID);

			for (int i = 1; i <= 10; ++i) {
				GetPlan200Response plan = null;
				try {
					plan = planApi.getPlan(workspaceID, projectID);
				}
				catch (IllegalArgumentException e) {
					log.error(e.getMessage());
				}
				if (plan == null) {
					try {
						sleep(i * this.sleepTime);
					}
					catch (Exception e) {
						continue;
					}
					continue;
				}
				log.error("Plan is here in the {} attempt", i);
				break;
			}
		}
		catch (ApiException e) {
			log.error("Cannot execute plan, error: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private void addSourceCode(String workspaceID, String projectID, String ZIPPath) {
		File file = new File(ZIPPath);
		try {
			projectInputsApi.createProjectInput(workspaceID, projectID, "sources", "Id", "source code", file);
		}
		catch (Exception e) {
			log.error("cannot append source code! {}", e.getMessage());
		}
	}

}
