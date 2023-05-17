package com.redhat.parodos.tasks.migrationtoolkit;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import com.redhat.parodos.email.Message;
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

	private final Consumer<Message> messageConsumer;

	public GetAnalysisTask(URI serverURL, String bearerToken, Consumer<Message> messageConsumer) {
		this.serverUrl = serverURL;
		this.mtaClient = new MTAClient(serverURL, bearerToken);
		this.messageConsumer = messageConsumer;
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
			// unexpected
			return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext(),
					new IllegalStateException("MTA client returned empty result with no error."));
		}
		else if (result instanceof Result.Failure<TaskGroup> failure) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, failure.t());
		}
		else if (result instanceof Result.Success<TaskGroup> success) {
			if ("Ready".equals(success.value().state()) && success.value().tasks() != null
					&& success.value().tasks()[0].state().equals("Succeeded")) {
				String reportURL = String.format("%s/hub/applications/%d/bucket/%s", serverUrl,
						success.value().tasks()[0].application().id(), success.value().data().output());
				sendEmail(reportURL, getOptionalParameterValue("email", null));
				addParameter("reportURL", reportURL);
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			else if ("Failed".equals(success.value().state())) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext,
						new Throwable("The underlying task failed, the report will not be ready"));
			}
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new Throwable("The report is not ready yet."));
		}
		throw new IllegalArgumentException();
	}

	private void sendEmail(String reportURL, String recipient) {
		if (recipient == null) {
			return;
		}
		messageConsumer.accept(new Message(recipient, "parodos-task-notificaion+mailtrap@redhat.com",
				"Parodos: Analysis report is done",
				String.format("The analysis report is done. Find it here %s", reportURL)));
	}

}
