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
 * {@link GetApplicationTask} gets the application from the application inventory.
 */
@Slf4j
public class GetApplicationTask extends BaseInfrastructureWorkFlowTask {

	// The MTA application client to work with. If kept null then a client would be
	// created using the context params serverURL and bearerToken during execution time.
	// Not depending on the context params for the client details
	// means flow authors can build the client object with their
	// params and pass it is. This also prevents an invoker to pass the client
	// details by ignoring them if a client already exists.
	// This method is useful for testing as well.
	protected MTAApplicationClient mtaClient;

	// For testing purposes
	protected WorkFlowTaskLogger taskLogger;

	public GetApplicationTask() {
		super();
	}

	public GetApplicationTask(URI serverURL, String bearerToken) {
		mtaClient = new MTAClient(serverURL, bearerToken);
	}

	/**
	 * @param workContext optional context values: serverURL, and bearerToken for the
	 * mtaClient.
	 */
	@Override
	public WorkReport execute(WorkContext workContext) {
		String applicationName = "";
		try {
			if (mtaClient == null) {
				var serverUrl = getOptionalParameterValue("serverURL", null);
				var bearerToken = getOptionalParameterValue("bearerToken", null);
				mtaClient = new MTAClient(URI.create(serverUrl), bearerToken);
			}
			applicationName = getRequiredParameterValue("applicationName");
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		Result<App> result = mtaClient.get(applicationName);

		if (result == null) {
			taskLogger.logErrorWithSlf4j("MTA client returned empty result with no error");
			// unexpected
			return new DefaultWorkReport(WorkStatus.REJECTED, new WorkContext(),
					new IllegalStateException("MTA client returned empty result with no error"));
		}
		else if (result instanceof Result.Failure<App> failure) {
			taskLogger.logErrorWithSlf4j("MTA client returned failed result");
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, failure.t());
		}
		else if (result instanceof Result.Success<App> success) {
			addParameter("applicationID", String.valueOf(success.value().id()));
			taskLogger.logInfoWithSlf4j("MTA client returned success result for getting application with id: {}",
					String.valueOf(success.value().id()));
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		throw new IllegalArgumentException();
	}

}
