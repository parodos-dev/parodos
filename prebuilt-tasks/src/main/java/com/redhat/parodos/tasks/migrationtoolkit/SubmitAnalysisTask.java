package com.redhat.parodos.tasks.migrationtoolkit;

import java.net.URI;
import java.util.List;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SubmitAnalysisTask} submits a analysis task to MTA on a specific application.
 * Prerequisite is that the application is already in the inventory of the MTA
 * ("Application Inventory"). This task would return back the id of the submitted request
 * for analysis, and with the combination of AnalysisGetTask is would fetch the result of
 * the task. Analysis target type is currently "cloud-readiness" only and might be
 * expanded later.
 */
@Slf4j
public class SubmitAnalysisTask extends BaseInfrastructureWorkFlowTask {

	// The MTA application client to work with. If kept null then a client would be
	// created using the context params serverURL and bearerToken during execution time.
	// Not depending on the context params for the client details
	// means flow authors can build the client object with their
	// params and pass it is. This also prevents an invoker to pass the client
	// details by ignoring them if a client already exists.
	// This method is useful for testing as well.
	protected MTATaskGroupClient mtaClient;

	public SubmitAnalysisTask() {
	}

	public SubmitAnalysisTask(URI serverURL, String bearerToken) {
		this.mtaClient = new MTAClient(serverURL, bearerToken);
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key("applicationName").type(WorkParameterType.TEXT).optional(false).description(
						"The application name as presented in the application hub. Can be generated from the repository name")
						.build(),
				WorkParameter.builder().key("serverURL").type(WorkParameterType.TEXT).optional(true).description(
						"Base URL of the MTA instance - e.g https://mta-openshift-mta.app.clustername.clusterdomain")
						.build(),
				WorkParameter.builder().key("bearerToken").type(WorkParameterType.TEXT).optional(true)
						.description("Bearer token to authenticate server requests").build());
	}

	/**
	 * @param workContext optional context values: serverURL, and bearerToken for the
	 * mtaClient.
	 */
	@Override
	public WorkReport execute(WorkContext workContext) {
		int applicationID;
		try {
			applicationID = Integer.parseInt(getRequiredParameterValue("applicationID"));
			if (mtaClient == null) {
				var serverUrl = getOptionalParameterValue("serverURL", null);
				var bearerToken = getOptionalParameterValue("bearerToken", null);
				this.mtaClient = new MTAClient(URI.create(serverUrl), bearerToken);
			}
		}
		catch (NumberFormatException | MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		Result<TaskGroup> result = mtaClient.create(applicationID);

		if (result == null) {
			// unexpected
			return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext(),
					new IllegalStateException("MTA client returned result with no error"));
		}
		else if (result instanceof Result.Failure<TaskGroup> failure) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, failure.t());
		}
		else if (result instanceof Result.Success<TaskGroup> success) {
			addParameter("taskGroupID", String.valueOf(success.value().id()));
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		throw new IllegalArgumentException();
	}

}
