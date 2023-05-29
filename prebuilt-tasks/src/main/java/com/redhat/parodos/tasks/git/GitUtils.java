package com.redhat.parodos.tasks.git;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public abstract class GitUtils {

	private GitUtils() {
	}

	public static String getRepoPath(WorkContext workContext) {
		var dest = workContext.get(GitConstants.CONTEXT_DESTINATION);
		if (dest == null) {
			return WorkContextDelegate.getOptionalValueFromRequestParams(workContext, GitConstants.GIT_REPO_PATH, "");
		}
		return WorkContextDelegate.getOptionalValueFromRequestParams(workContext, GitConstants.GIT_REPO_PATH,
				dest.toString());
	}

	public static Repository getRepo(String path) throws IOException {
		Path gitDir = Paths.get(path).resolve(GitConstants.GIT_FOLDER);
		return new FileRepositoryBuilder().setGitDir(gitDir.toFile()).build();
	}

}
