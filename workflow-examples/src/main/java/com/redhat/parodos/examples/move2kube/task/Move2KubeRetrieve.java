package com.redhat.parodos.examples.move2kube.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.api.ProjectOutputsApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

@Slf4j
public class Move2KubeRetrieve extends Move2KubeBase {

	private String plan;

	private ProjectOutputsApi output;

	public Move2KubeRetrieve(String server) {
		super();
		this.setClient(server);
		output = new ProjectOutputsApi(client);
	}

	public Move2KubeRetrieve(String server, ProjectOutputsApi outputsApi) {
		new Move2KubeRetrieve(server);
		output = outputsApi;
	}

	public WorkReport execute(WorkContext workContext) {
		String workspaceID = (String) workContext.get(getWorkspaceContextKey());
		String projectID = (String) workContext.get(getProjectContextKey());
		String transformID = (String) workContext.get(getTransformContextKey());
		String sourcePath = (String) workContext.get("gitDestination").toString();
		Path tempDir = null;
		try {
			File file = output.getProjectOutput(workspaceID, projectID, transformID);
			if (file == null) {
				return new DefaultWorkReport(WorkStatus.FAILED, workContext,
						new RuntimeException("Couldn't get file from transformation"));
			}
			tempDir = Files.createTempDirectory(String.format("move2kube-transform-%s", transformID));
			extractZipFile(file, tempDir);
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		try {
			Path finalPath = Files.newDirectoryStream(Paths.get(tempDir.toString() + "/output/")).iterator().next();
			log.info("FinalPath is --->{} and GitPath is {}", finalPath, sourcePath);
			FileUtils.deleteDirectory(finalPath.resolve(Paths.get("source/output/src/.git")).toFile());
			FileUtils.copyDirectory(finalPath.resolve(Paths.get("source/output/src")).toFile(),
					Paths.get(sourcePath).toFile());
			FileUtils.copyDirectory(finalPath.resolve(Paths.get("deploy")).toFile(),
					Paths.get(sourcePath).resolve("deploy").toFile());
			FileUtils.copyDirectory(finalPath.resolve(Paths.get("scripts")).toFile(),
					Paths.get(sourcePath).resolve("scripts").toFile());
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	public static void extractZipFile(File zipFile, Path extractPath) throws IOException {
		try (ZipFile zip = new ZipFile(zipFile)) {
			for (ZipArchiveEntry entry : Collections.list(zip.getEntries())) {
				File entryFile = new File(extractPath.toString() + "/" + entry.getName());
				if (entry.isDirectory()) {
					entryFile.mkdirs();
					continue;
				}
				File parentDir = entryFile.getParentFile();
				if (parentDir != null && !parentDir.exists()) {
					parentDir.mkdirs();
				}
				try (FileOutputStream fos = new FileOutputStream(entryFile)) {
					zip.getInputStream(entry).transferTo(fos);
				}
			}
		}
	}

}
