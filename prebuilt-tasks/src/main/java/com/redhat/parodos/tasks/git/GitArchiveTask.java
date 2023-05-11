package com.redhat.parodos.tasks.git;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.io.Files;
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
import org.eclipse.jgit.api.ArchiveCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.archive.ZipFormat;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

@Slf4j
@AllArgsConstructor
public class GitArchiveTask extends BaseWorkFlowTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(WorkParameter.builder().key(GitUtils.getGitRepoPath()).type(WorkParameterType.TEXT)
				.optional(true).description("path where the git repo is located").build());
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
		try {
			repo = getRepo(path);
			Path archivePath = archive(repo);
			workContext.put(GitUtils.getContextArchivePath(), archivePath.toAbsolutePath().toString());
		}
		catch (FileNotFoundException | GitAPIException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("Cannot archive the repository: %s".formatted(e.getMessage())));
		}
		catch (IOException e) {
			// This is the catch for Repository clone call or archive, we don't really
			// know.
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("No repository at '%s' Error: %s".formatted(path, e.getMessage())));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("Cannot archive the repository:" + e));
		}
		finally {
			if (repo != null) {
				repo.close();
			}
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, null);
	}

	private Repository getRepo(String path) throws IOException {
		Path gitDir = Paths.get(path + "/.git");
		return new FileRepositoryBuilder().setGitDir(gitDir.toFile()).build();
	}

	private Path archive(Repository repo) throws FileNotFoundException, IOException, GitAPIException {
		// Create a Git instance
		Git git = new Git(repo);
		ArchiveCommand.registerFormat("zip", new ZipFormat());

		// Create a ZipFormat instance
		String tmpdir = Files.createTempDir().getAbsolutePath();
		Path zipFile = Paths.get(tmpdir + "/output.zip");
		try (FileOutputStream out = new FileOutputStream(zipFile.toAbsolutePath().toString())) {
			git.archive().setTree(repo.resolve("HEAD")).setPrefix("src/").setFormat("zip").setOutputStream(out).call();
		}
		finally {
			git.close();
		}
		return zipFile;
	}

}
