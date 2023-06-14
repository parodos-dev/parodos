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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

@Slf4j
@AllArgsConstructor
public class GitBranchTask extends BaseWorkFlowTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(GitConstants.GIT_REPO_PATH).type(WorkParameterType.TEXT).optional(true)
						.description("path where the git repo is located").build(),
				WorkParameter.builder().key(GitConstants.BRANCH).type(WorkParameterType.TEXT).optional(false)
						.description("branch whichs need to be created").build());
	}

	public String getRepoPath(WorkContext workContext) {
		return GitUtils.getRepoPath(workContext);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String branchName = null;
		try {
			branchName = this.getRequiredParameterValue(GitConstants.BRANCH);
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		String path = getRepoPath(workContext);
		if (Strings.isNullOrEmpty(path)) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new IllegalArgumentException("The path parameter cannot be null or empty"));
		}

		try (Repository repo = getRepo(path)) {
			Git git = new Git(repo);
			Ref branchRef = repo.findRef(branchName);
			if (branchRef != null) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext,
						new IllegalArgumentException("Branch '%s' is already created".formatted(branchName)));
			}
			git.branchCreate().setName(branchName).call();
			git.checkout().setName(branchName).call();
		}
		catch (FileNotFoundException | GitAPIException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new RuntimeException(
					"Cannot create the branch for the repository: %s".formatted(e.getMessage()), e));
		}
		catch (IOException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("No repository at '%s' error: %s".formatted(path, e.getMessage()), e));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new RuntimeException(
					"Cannot create the branch on the repository: %s".formatted(e.getMessage()), e));
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private Repository getRepo(String path) throws IOException {
		return GitUtils.getRepo(path);
	}

}