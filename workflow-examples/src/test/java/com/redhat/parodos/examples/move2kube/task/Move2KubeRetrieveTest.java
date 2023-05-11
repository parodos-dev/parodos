package com.redhat.parodos.examples.move2kube.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.ApiException;
import dev.parodos.move2kube.api.ProjectOutputsApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class Move2KubeRetrieveTest {

	private static String move2KubeWorkspaceIDCtxKey = "move2KubeWorkspaceID";

	private static String move2KubeProjectIDCtxKey = "move2KubeProjectID";

	private static String move2KubeTransformIDCtxKey = "move2KubeTransformID";

	private static String gitDestinationCtxKey = "gitDestination";

	private ProjectOutputsApi output;

	private Move2KubeRetrieve task;

	@Before
	public void setup() {
		output = Mockito.mock(ProjectOutputsApi.class);
		task = new Move2KubeRetrieve("http://localhost/", output);
	}

	@Test
	public void testValidExecution() {
		// given
		WorkContext context = getSampleWorkContext();
		assertDoesNotThrow(() -> {
			Mockito.when(output.getProjectOutput(Mockito.any(), Mockito.any(), Mockito.any()))
					.thenReturn(createSampleZip());
		});
		// when
		WorkReport report = task.execute(context);

		// then
		assertNull(report.getError());
		assertEquals(report.getStatus(), WorkStatus.COMPLETED);

		String path = (String) context.get(gitDestinationCtxKey);
		assertNotNull(path);
		File gitPath = new File(path);

		String[] fileNames = { "deploy/bar.txt", "deploy/foo.txt", "scripts/bar.sh", "scripts/foo.sh" };

		for (String fileName : fileNames) {
			assertTrue(gitPath.toPath().resolve(fileName).toFile().exists(), "Failed on file '%s'".formatted(fileName));
		}
	}

	@Test
	public void testWithInvalidOutputExecution() {
		// given
		WorkContext context = getSampleWorkContext();

		assertDoesNotThrow(() -> {
			Mockito.when(output.getProjectOutput(Mockito.any(), Mockito.any(), Mockito.any()))
					.thenThrow(ApiException.class);
		});

		// when
		WorkReport report = task.execute(context);

		// then
		assertNotNull(report.getError());
		assertThat(report.getError()).isInstanceOf(ApiException.class);
		assertEquals(report.getStatus(), WorkStatus.FAILED);
	}

	@Test
	public void testWithInvalidOutputFile() {
		// given
		WorkContext context = getSampleWorkContext();

		assertDoesNotThrow(() -> {
			Mockito.when(output.getProjectOutput(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
		});

		// when
		WorkReport report = task.execute(context);

		// then
		assertNotNull(report.getError());
		assertThat(report.getError()).isInstanceOf(RuntimeException.class);
		assertEquals(report.getStatus(), WorkStatus.FAILED);
	}

	public WorkContext getSampleWorkContext() {
		WorkContext workContext = new WorkContext();
		workContext.put(move2KubeTransformIDCtxKey, move2KubeProjectIDCtxKey);
		workContext.put(move2KubeProjectIDCtxKey, move2KubeProjectIDCtxKey);
		workContext.put(move2KubeWorkspaceIDCtxKey, move2KubeWorkspaceIDCtxKey);
		assertDoesNotThrow(() -> {
			workContext.put(gitDestinationCtxKey, createTempDir());
		});
		return workContext;
	}

	private String createTempDir() throws IOException {
		return Files.createTempDirectory("test").toAbsolutePath().toString();
	}

	private static File createSampleZip() throws IOException {
		String zipFileName = "/tmp/test%s.zip".formatted(UUID.randomUUID());
		String tempDir = "/tmp/%s/".formatted(UUID.randomUUID().toString());
		File zipFile = new File(zipFileName);
		String[] fileNames = { "output/project-name/deploy/bar.txt", "output/project-name/deploy/foo.txt",
				"output/project-name/scripts/bar.sh", "output/project-name/scripts/foo.sh" };

		byte[] buffer = new byte[1024];

		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);

		for (String fileName : fileNames) {
			File file = new File("%s%s".formatted(tempDir, fileName));
			if (file.getParentFile() != null && !file.getParentFile().exists()) {
				assertTrue(file.getParentFile().mkdirs());
			}
			assertTrue(file.createNewFile());

			ZipEntry zipEntry = new ZipEntry(fileName);
			zos.putNextEntry(zipEntry);

			FileInputStream fis = new FileInputStream(file);
			int length;
			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}

			fis.close();
			zos.closeEntry();
		}

		zos.close();
		return zipFile;
	}

}
