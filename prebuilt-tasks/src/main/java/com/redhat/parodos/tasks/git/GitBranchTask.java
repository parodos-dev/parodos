package com.redhat.parodos.tasks.git;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

@Slf4j
@AllArgsConstructor
public class GitBranchTask extends BaseWorkFlowTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(GitUtils.getGitRepoPath()).type(WorkParameterType.TEXT).optional(true)
						.description("path where the git repo is located").build(),
				WorkParameter.builder().key(GitUtils.getBranch()).type(WorkParameterType.TEXT).optional(false)
						.description("branch whichs need to be created").build());
	}

	public String getRepoPath(WorkContext workContext) {
		return GitUtils.getRepoPath(workContext);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String branchName = null;
		try {
			branchName = this.getRequiredParameterValue(workContext, GitUtils.getBranch());
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
		catch (IOException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("No repository at '%s' error: %s".formatted(path, e.getMessage())));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Cannot create the branch on the repository: %s".formatted(e.getMessage())));
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, null);
	}

	private Repository getRepo(String path) throws IOException {
		Path gitDir = Paths.get(path + "/.git");
		return new FileRepositoryBuilder().setGitDir(gitDir.toFile()).build();
	}

}