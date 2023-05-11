package com.redhat.parodos.tasks.git;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.Getter;

public abstract class GitUtils {

	private GitUtils() {
	}

	@Getter
	static final String gitRepoPath = "path";

	@Getter
	static final String gitCommitMessage = "commitMessage";

	@Getter
	static final String uri = "uri";

	@Getter
	static final String branch = "branch";

	@Getter
	static final String defaultBranch = "main";

	@Getter
	static final String ContextUri = "gitUri";

	@Getter
	static final String ContextBranch = "gitBranch";

	@Getter
	static final String contextDestination = "gitDestination";

	@Getter
	static final String contextArchivePath = "gitArchivePath";

	public static String getRepoPath(WorkContext workContext) {
		var dest = workContext.get(contextDestination);
		if (dest == null) {
			return WorkContextDelegate.getOptionalValueFromRequestParams(workContext, getGitRepoPath(), "");
		}
		return WorkContextDelegate.getOptionalValueFromRequestParams(workContext, getGitRepoPath(), dest.toString());
	}

}
