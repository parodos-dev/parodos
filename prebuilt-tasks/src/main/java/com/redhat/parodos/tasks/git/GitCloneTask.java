package com.redhat.parodos.tasks.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.base.Strings;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

@AllArgsConstructor
@Slf4j
public class GitCloneTask extends BaseWorkFlowTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(GitConstants.URI).type(WorkParameterType.TEXT).optional(false)
						.description("Url to clone from").build(),
				WorkParameter.builder().key(GitConstants.BRANCH).type(WorkParameterType.TEXT).optional(true)
						.description("Branch to clone from, default main").build(),
				WorkParameter.builder().key("credentials").type(WorkParameterType.TEXT).optional(false)
						.description("Git credential").build());
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String gitUri = null;
		String destination = null;
		String gitBranch = null;
		String gitCredentials = null;

		try {
			gitUri = this.getRequiredParameterValue(GitConstants.URI);
			gitBranch = this.getOptionalParameterValue(GitConstants.BRANCH, GitConstants.DEFAULT_BRANCH);
			gitCredentials = this.getOptionalParameterValue("credentials", "");
			destination = cloneRepo(gitUri, gitBranch, gitCredentials);
		}
		catch (MissingParameterException e) {
			log.error("Failed to resolve required parameter: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (TransportException e) {
			log.error("Cannot connect to repository server '{}' error: {}", gitUri, e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("cannot connect to the repository server"));
		}
		catch (InvalidRemoteException e) {
			log.error("remote repository server '{}' is not available, error: {}", gitUri, e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("Remote repository " + gitUri + " is not available"));
		}
		catch (IOException | GitAPIException e) {
			log.error("Cannot clone repository: {} {}", gitUri, e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("cannot clone repository, error: " + e.getMessage()));
		}
		workContext.put(GitConstants.CONTEXT_URI, gitUri);
		workContext.put(GitConstants.CONTEXT_DESTINATION, destination);
		workContext.put(GitConstants.CONTEXT_BRANCH, gitBranch);
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, null);
	}

	private String cloneRepo(String gitUri, String gitBranch, String credentials)
			throws InvalidRemoteException, TransportException, IOException, GitAPIException {

		String tmpDir = Files.createTempDirectory("GitTaskClone").toAbsolutePath().toString();

		CloneCommand cloneCommand = Git.cloneRepository().setURI(gitUri).setBranch("refs/heads/" + gitBranch)
				.setDirectory(new File(tmpDir));
		if (!Strings.isNullOrEmpty(credentials)) {
			cloneCommand.setTransportConfigCallback(GitUtils.getTransport(Path.of(credentials)));
		}
		cloneCommand.call();
		return tmpDir;
	}

}
