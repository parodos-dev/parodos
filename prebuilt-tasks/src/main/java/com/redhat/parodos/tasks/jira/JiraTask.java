package com.redhat.parodos.tasks.jira;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JiraTask can create/retrieve/update/delete a jira task depending on the fields passed
 * in the context of the execution. #excute returns an extended work report containing a
 * jira issue, and/or the api respond Any 2xx return code is COMPLETED, and anything else
 * is FAILED
 *
 *
 */
@Slf4j
public class JiraTask extends BaseWorkFlowTask {

	// The jira client to work with. If nothing is passed then a default client
	// implementation is used, using the context params serverURL and bearerToken
	// during execution time.
	protected JiraIssueClient jiraClient;

	public static final int DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS = 600;

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key("serverURL").type(WorkParameterType.TEXT).optional(false)
						.description("Base URL of the Jira instance - e.g https://jira.example.org").build(),
				WorkParameter.builder().key("bearerToken").type(WorkParameterType.TEXT).optional(false)
						.description("Bearer token to authenticate Jira server requests").build());
	}

	/**
	 * @param workContext optional context values: serverURL, and bearerToken for the
	 * Jira's client auth. Other optional context values and how they affect execution:
	 * Create: id: do not exist in the context map title: value of title comments: [ list
	 * of comments to add ] Retrieve: id: valid ticket id all title, status, comments do
	 * not exist in the map Update: id: valid ticket id status: new ticket status (allows
	 * closing) title: new title comments: new comment to add If none of the context
	 * params passed then this execution is invalid and returns a failure.
	 * @return
	 */
	@Override
	public WorkReport execute(WorkContext workContext) {
		if (jiraClient == null) {
			// allow initiating the jira client without a context param.
			// Usefull for testing and initiating the client details directly
			/// by flow authors (for security reasons, it prevents the invoker
			// changing the server URL and the token)
			try {
				var serverUrl = WorkContextDelegate.getRequiredValueFromRequestParams(workContext, "serverURL");
				var bearerToken = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, "bearerToken",
						null);
				this.jiraClient = new JiraClient(URI.create(serverUrl), bearerToken);
			}
			catch (MissingParameterException e) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
			}
		}
		var id = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, "id", "");
		var summary = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, "summary", "");
		var project = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, "project", "");
		var status = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, "status", "");
		var description = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, "description", "");
		var comment = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, "comment", "");

		if (id.isBlank()) {
			try {
				JiraIssue issue = createIssue(new JiraIssue("", project, summary, description, "", List.of(comment)));
				WorkReport report = new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext());
				report.getWorkContext().put("issue", issue);
				return report;
			}
			catch (Exception e) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
			}

		}
		else {
			try {
				JiraIssue issue = getIssueByID(id);

				if (summary.isBlank() && description.isBlank() && comment.isBlank()) {
					// this is just a fetch, return the issue

					var context = new WorkContext();
					context.put("issue", issue);
					return new DefaultWorkReport(WorkStatus.COMPLETED, context);
				}
				if (!summary.isBlank() && !issue.summary.equals(summary)) {
					issue.summary = summary;
				}
				if (!description.isBlank() && !issue.description.equals(description)) {
					issue.description = description;
				}
				if (!status.isBlank() && !issue.status.equals(status)) {
					issue.status = status;
				}

				updateIssue(issue);

				// adding comments?
				if (!comment.isBlank()) {
					jiraClient.addComment(id, comment);
				}
				var context = new WorkContext();
				context.put("issue", issue);
				return new DefaultWorkReport(WorkStatus.COMPLETED, context);
			}
			catch (Exception e) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
			}
		}
	}

	private JiraIssue createIssue(JiraIssue issue) throws Exception {
		try {
			JSONObject jsonObject = jiraClient.create(issue.project, issue.summary, issue.description);
			return new JiraIssue(jsonObject.getString("id"), null, null, null, null, null);
		}
		catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void updateIssue(JiraIssue issue) throws Exception {
		try {
			jiraClient.update(issue.id, issue.summary, issue.description, issue.status);
		}
		catch (JSONException e) {
			throw new Exception(e);
		}
	}

	private JiraIssue getIssueByID(String id) throws Exception {
		try {
			JSONObject o = jiraClient.get(id);
			return new JiraIssue(o.getString("id"), o.getString("project"), o.getString("summary"),
					o.getString("description"), o.getString("status"), Collections.emptyList());
		}
		catch (JSONException e) {
			throw new Exception(e);
		}
	}

	interface JiraIssueClient {

		JSONObject get(String id) throws Exception;

		void update(String id, String summary, String description, String status) throws Exception;

		void addComment(String id, String comment) throws Exception;

		JSONObject create(String project, String summary, String description) throws Exception;

	}

	static class JiraClient implements JiraIssueClient {

		private IssueRestClient restClient;

		private JiraClient(URI serverURI, String bearerToken) {
			restClient = new AsynchronousJiraRestClientFactory()
					.create(serverURI, r -> r.setHeader("Authorization", bearerToken)).getIssueClient();
		}

		@Override
		public JSONObject get(String id) throws Exception {
			Issue issue;
			try {
				issue = restClient.getIssue(id).get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new Exception(e);
			}
			return new JSONObject().put("id", issue.getId()).put("summary", issue.getSummary())
					.put("description", issue.getDescription()).put("status", issue.getStatus())
					.put("comments", issue.getComments());
		}

		@Override
		public void update(String id, String summary, String description, String status) throws Exception {
			var issueInputBuilder = new IssueInputBuilder();
			if (description != null && !description.isBlank()) {
				issueInputBuilder.setDescription(description);
			}
			if (summary != null && !summary.isBlank()) {
				issueInputBuilder.setSummary(summary);
			}

			try {
				restClient.updateIssue(id, issueInputBuilder.build()).get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS,
						TimeUnit.SECONDS);
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new Exception(e);
			}

			Issue issue;
			try {
				issue = restClient.getIssue(id).get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
				Iterable<Transition> transitions = restClient.getTransitions(issue)
						.get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

				Optional<Transition> transition = StreamSupport.stream(transitions.spliterator(), false)
						.filter(t -> t.getName().equals(status)).findFirst();
				if (transition.isPresent()) {
					restClient.transition(issue, new TransitionInput(transition.get().getId()))
							.get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
				}
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void addComment(String id, String comment) throws Exception {
			Issue issue;
			try {
				issue = restClient.getIssue(id).get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new Exception(e);
			}

			try {
				restClient.addComment(issue.getCommentsUri(), Comment.valueOf(comment))
						.get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public JSONObject create(String project, String summary, String description) throws Exception {
			IssueInput input = new IssueInputBuilder().setSummary(summary).setProjectKey(project)
					.setDescription(description).build();
			try {
				BasicIssue issue = restClient.createIssue(input).get(DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS,
						TimeUnit.SECONDS);
				return new JSONObject().put("id", issue.getId());
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new Exception(e);
			}
		}

	}

}
