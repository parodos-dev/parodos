package com.redhat.parodos.tasks.git;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

@AllArgsConstructor
@Slf4j
public class GitPushTask extends BaseWorkFlowTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(GitConstants.GIT_REPO_PATH).type(WorkParameterType.TEXT).optional(true)
						.description("path where the git repo is located").build(),
				WorkParameter.builder().key(GitConstants.GIT_REMOTE).type(WorkParameterType.TEXT).optional(false)
						.description("path where the git repo is located").build());
	}

	public String getRepoPath(WorkContext workContext) {
		return GitUtils.getRepoPath(workContext);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String path = getRepoPath(workContext);
		if (Strings.isNullOrEmpty(path)) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new IllegalArgumentException("The path parameter cannot be null or empty"));
		}

		Repository repo = null;
		String remote = "";
		try {
			remote = this.getRequiredParameterValue(GitConstants.GIT_REMOTE);
			repo = getRepo(path);
			push(repo, remote);
		}
		catch (MissingParameterException e) {
			log.debug("Failed to resolve required parameter: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (FileNotFoundException | GitAPIException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Cannot push to the remote %s: %s".formatted(remote, e.getMessage()), e));
		}
		catch (IOException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("No repository at '%s' Error: %s".formatted(path, e.getMessage()), e));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Cannot push to the repository: %s".formatted(e.getMessage()), e));
		}
		finally {
			if (repo != null) {
				repo.close();
			}
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, null);
	}

	private Repository getRepo(String path) throws IOException {
		return GitUtils.getRepo(path);
	}

	private void push(Repository repo, String remoteName) throws FileNotFoundException, IOException, GitAPIException {
		Git git = new Git(repo);

		try {
			git.push().setForce(false).setRemote(remoteName).call();
		}
		finally {
			git.close();
		}
	}

}
