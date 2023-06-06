package com.redhat.parodos.tasks.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

@Slf4j
@AllArgsConstructor
public class GitArchiveTask extends BaseWorkFlowTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(WorkParameter.builder().key(GitConstants.GIT_REPO_PATH).type(WorkParameterType.TEXT)
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
			workContext.put(GitConstants.CONTEXT_ARCHIVE_PATH, archivePath.toAbsolutePath().toString());
		}
		catch (FileNotFoundException | GitAPIException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("Cannot archive the repository: %s".formatted(e.getMessage()), e));
		}
		catch (IOException e) {
			// This is the catch for Repository clone call or archive, we don't really
			// know.
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("No repository at '%s' Error: %s".formatted(path, e.getMessage()), e));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new Exception("Cannot archive the repository: %s".formatted(e.getMessage()), e));
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

	private static void addFolderContentsToZip(File folder, String parentFolderPath, ZipOutputStream zos)
			throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				addFolderContentsToZip(file, parentFolderPath + "/" + file.getName(), zos);
			}
			else {
				byte[] buffer = new byte[1024];
				try (FileInputStream fis = new FileInputStream(file)) {
					String entryPath = parentFolderPath + "/" + file.getName();
					ZipEntry entry = new ZipEntry(entryPath);
					zos.putNextEntry(entry);
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
				}
			}
		}
	}

	private Path archive(Repository repo) throws FileNotFoundException, IOException, GitAPIException {

		// Create a ZipFormat instance
		String tmpdir = Files.createTempDir().getAbsolutePath();
		Path zipFile = Paths.get(tmpdir + "/output.zip");
		Path repoDir = Path.of(repo.getDirectory().getAbsolutePath()).resolve("..");

		try (FileOutputStream fos = new FileOutputStream(zipFile.toAbsolutePath().toString());
				ZipOutputStream zos = new ZipOutputStream(fos)) {
			addFolderContentsToZip(repoDir.toFile(), GitConstants.SRC_FOLDER, zos);
		}
		return zipFile;
	}

}
