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

	public GetApplicationTask() {
	}

	public GetApplicationTask(URI serverURL, String bearerToken) {
		mtaClient = new MTAClient(serverURL, bearerToken);
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key("serverURL").type(WorkParameterType.TEXT).optional(true).description(
						"Base URL of the MTA instance - e.g https://mta-openshift-mta.app.clustername.clusterdomain")
						.build(),
				WorkParameter.builder().key("bearerToken").type(WorkParameterType.TEXT).optional(true)
						.description("Bearer token to authenticate server requests").build(),
				WorkParameter.builder().key("applicationName").type(WorkParameterType.TEXT).optional(false).description(
						"The application name as presented in the application hub. Can be generated from the repository name")
						.build(),
				WorkParameter.builder().key("sourceRepository").type(WorkParameterType.TEXT).optional(true).description(
						"The application name as presented in the application hub. Can be generated from the repository name")
						.build());
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
				var serverUrl = getOptionalParameterValue(workContext, "serverURL", null);
				var bearerToken = getOptionalParameterValue(workContext, "bearerToken", null);
				mtaClient = new MTAClient(URI.create(serverUrl), bearerToken);
			}
			applicationName = getRequiredParameterValue(workContext, "applicationName");
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		Result<App> result = mtaClient.get(applicationName);

		if (result == null) {
			// unexpected
			return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext(),
					new IllegalStateException("MTA client returned empty result with no error"));
		}
		else if (result instanceof Result.Failure<App> failure) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, failure.t());
		}
		else if (result instanceof Result.Success<App> success) {
			addParameter(workContext, "applicationID", String.valueOf(success.value().id()));
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		throw new IllegalArgumentException();
	}

}
