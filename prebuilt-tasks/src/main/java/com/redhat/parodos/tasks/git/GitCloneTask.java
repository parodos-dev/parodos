package com.redhat.parodos.tasks.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
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
				WorkParameter.builder().key(GitUtils.getUri()).type(WorkParameterType.TEXT).optional(false)
						.description("Url to clone from").build(),
				WorkParameter.builder().key(GitUtils.getBranch()).type(WorkParameterType.TEXT).optional(true)
						.description("Branch to clone from, default main").build(),
				WorkParameter.builder().key("credentials").type(WorkParameterType.TEXT).optional(false)
						.description("Git credential").build());
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String gitUri = null;
		String destination = null;
		String gitBranch = null;

		try {
			gitUri = WorkContextDelegate.getRequiredValueFromRequestParams(workContext, GitUtils.getUri());
			gitBranch = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, GitUtils.getBranch(),
					"main");
			destination = cloneRepo(gitUri, gitBranch);
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (TransportException e) {
			log.debug("Cannot connect to repository server '{}' error: {}", gitUri, e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("cannot connect to the repository server"));
		}
		catch (InvalidRemoteException e) {
			log.debug("remote repository server '{}' is not available, error: {}", gitUri, e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("Remote repository " + gitUri + " is not available"));
		}
		catch (IOException | GitAPIException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("cannot clone repository, error: " + e.getMessage()));
		}

		workContext.put(GitUtils.getContextUri(), gitUri);
		workContext.put(GitUtils.getContextDestination(), destination);
		workContext.put(GitUtils.getContextBranch(), gitBranch);
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, null);
	}

	private String cloneRepo(String gitUri, String gitBranch)
			throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		String tmpDir = Files.createTempDirectory("GitTaskClone").toAbsolutePath().toString();
		Git.cloneRepository().setURI(gitUri).setBranch("refs/heads/" + gitBranch).setDirectory(new File(tmpDir)).call();
		return tmpDir;
	}

}
