package com.redhat.parodos.tasks.migrationtoolkit;

import java.net.URI;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.log.WorkFlowTaskLogger;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
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

	// For testing purposes
	protected WorkFlowTaskLogger taskLogger;

	public SubmitAnalysisTask() {
		super();
	}

	public SubmitAnalysisTask(URI serverURL, String bearerToken) {
		this.mtaClient = new MTAClient(serverURL, bearerToken);
	}

	/**
	 * @param workContext optional context values: serverURL, and bearerToken for the
	 * mtaClient.
	 */
	@Override
	public WorkReport execute(WorkContext workContext) {
		String applicationID;
		try {
			applicationID = getRequiredParameterValue("applicationID");
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
			taskLogger.logErrorWithSlf4j("MTA client returned empty result with no error");
			// unexpected
			return new DefaultWorkReport(WorkStatus.REJECTED, new WorkContext(),
					new IllegalStateException("MTA client returned result with no error"));
		}
		else if (result instanceof Result.Failure<TaskGroup> failure) {
			taskLogger.logErrorWithSlf4j("MTA client returned failed result");
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, failure.t());
		}
		else if (result instanceof Result.Success<TaskGroup> success) {
			addParameter("taskGroupID", String.valueOf(success.value().id()));
			taskLogger.logInfoWithSlf4j("MTA client returned success result for analysis task with id: {}",
					String.valueOf(success.value().id()));
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		throw new IllegalArgumentException();
	}

}
