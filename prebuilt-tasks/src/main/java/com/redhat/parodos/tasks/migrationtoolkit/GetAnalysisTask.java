package com.redhat.parodos.tasks.migrationtoolkit;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.inject.Inject;

import com.redhat.parodos.infrastructure.Notifier;
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
 * {@link GetAnalysisTask} gets analysis task from the MTA. This task returns the
 * taskgroup details, including state of the analysis task and the url of the report if
 * the task is in state succeeded.
 */
@Slf4j
public class GetAnalysisTask extends BaseInfrastructureWorkFlowTask {

	// The MTA application client to work with. If kept null then a client would be
	// created using the context params serverURL and bearerToken during execution time.
	// Not depending on the context params for the client details
	// means flow authors can build the client object with their
	// params and pass it is. This also prevents an invoker to pass the client
	// details by ignoring them if a client already exists.
	// This method is useful for testing as well.
	protected MTATaskGroupClient mtaClient;

	private URI serverUrl;

	@Inject
	private Notifier notificationSender;

	public GetAnalysisTask(URI serverURL, String bearerToken, Notifier notifier) {
		this.serverUrl = serverURL;
		this.mtaClient = new MTAClient(serverURL, bearerToken);
		this.notificationSender = notifier;
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key("taskGroupID").type(WorkParameterType.NUMBER).optional(false)
						.description("The application name as presented in the application hub").build(),
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
		if (mtaClient == null) {
			serverUrl = URI.create(getOptionalParameterValue("serverURL", null));
			var bearerToken = getOptionalParameterValue("bearerToken", null);
			if (serverUrl == null) {
				log.error(
						"serverURL is empty. Either pass it while creating the instance of the task or in the context");
				return new DefaultWorkReport(WorkStatus.FAILED, workContext);
			}
			mtaClient = new MTAClient(serverUrl, bearerToken);
		}

		int taskGroupID;
		try {
			taskGroupID = Integer.parseInt(getRequiredParameterValue("taskGroupID"));
		}
		catch (MissingParameterException | NumberFormatException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		Result<TaskGroup> result = mtaClient.get(taskGroupID);

		if (result == null) {
			taskLogger.logErrorWithSlf4j("MTA client returned empty result with no error.");
			// unexpected
			return new DefaultWorkReport(WorkStatus.REJECTED, new WorkContext(),
					new IllegalStateException("MTA client returned empty result with no error."));
		}
		else if (result instanceof Result.Failure<TaskGroup> failure) {
			taskLogger.logErrorWithSlf4j("MTA client returned failed result");
			return new DefaultWorkReport(WorkStatus.REJECTED, workContext, failure.t());
		}
		else if (result instanceof Result.Success<TaskGroup> success) {
			if ("Ready".equals(success.value().state()) && success.value().tasks() != null
					&& "Succeeded".equals(success.value().tasks()[0].state())) {
				String reportURL = "%s/hub/applications/%d/bucket%s".formatted(serverUrl,
						success.value().tasks()[0].application().id(), success.value().data().output());
				taskLogger.logInfoWithSlf4j("MTA client returned success result with report url: {}", reportURL);
				addParameter("reportURL", reportURL);
				addAdditionInfo("MTA assessment report", reportURL);
				notificationSender.send("Migration Analysis Report Completed",
						"[Migration analysis report](%s) completed.".formatted(reportURL));
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			else if ("Failed".equals(success.value().state())
					|| Arrays.stream(Objects.requireNonNull(success.value().tasks()))
							.anyMatch(task -> "Failed".equals(task.state()))) {
				taskLogger.logErrorWithSlf4j("The underlying task failed, the report will not be ready");
				return new DefaultWorkReport(WorkStatus.REJECTED, workContext,
						new Throwable("The underlying task failed, the report will not be ready"));
			}
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Throwable("The report is not ready yet."));
		}
		throw new IllegalArgumentException();
	}

}
