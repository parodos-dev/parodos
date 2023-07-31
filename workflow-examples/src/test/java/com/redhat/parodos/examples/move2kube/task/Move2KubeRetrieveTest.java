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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class Move2KubeRetrieveTest {

	private static final String MOVE2KUBE_WORKSPACE_ID = "move2KubeWorkspaceID";

	private static final String MOVE2KUBE_PROJECT_ID = "move2KubeProjectID";

	private static final String MOVE2KUBE_TRANSFORM_ID = "move2KubeTransformID";

	private static final String GIT_DESTINATION_CONTEXT_KEY = "gitDestination";

	private ProjectOutputsApi output;

	private Move2KubeRetrieve task;

	@BeforeEach
	public void setup() {
		output = mock(ProjectOutputsApi.class);
		task = new Move2KubeRetrieve("http://localhost/", output);
	}

	@Test
	public void testValidExecution() {
		// given
		WorkContext context = getSampleWorkContext();
		assertDoesNotThrow(() -> {
			when(output.getProjectOutput(any(), any(), any())).thenReturn(createSampleZip());
		});
		// when
		WorkReport report = task.execute(context);

		// then
		assertThat(report.getError(), is(nullValue()));
		assertThat(report.getStatus(), equalTo(WorkStatus.COMPLETED));

		String path = (String) context.get(GIT_DESTINATION_CONTEXT_KEY);
		assertThat(path, is(notNullValue()));
		File gitPath = new File(path);

		String[] fileNames = { "test.txt", "deploy/bar.txt", "deploy/foo.txt", "scripts/bar.sh", "scripts/foo.sh" };

		for (String fileName : fileNames) {
			assertThat(gitPath.toPath().resolve(fileName).toFile().exists(), is(true));
		}
	}

	@Test
	public void testWithInvalidOutputExecution() {
		// given
		WorkContext context = getSampleWorkContext();

		assertDoesNotThrow(() -> {
			when(output.getProjectOutput(any(), any(), any())).thenThrow(ApiException.class);
		});

		// when
		WorkReport report = task.execute(context);

		// then
		assertThat(report.getError(), is(notNullValue()));
		assertThat(report.getError(), is(instanceOf(ApiException.class)));
		assertThat(report.getStatus(), equalTo(WorkStatus.FAILED));
	}

	@Test
	public void testWithInvalidOutputFile() {
		// given
		WorkContext context = getSampleWorkContext();

		assertDoesNotThrow(() -> {
			when(output.getProjectOutput(any(), any(), any())).thenReturn(null);
		});

		// when
		WorkReport report = task.execute(context);

		// then
		assertThat(report.getError(), is(notNullValue()));
		assertThat(report.getError(), is(instanceOf(RuntimeException.class)));
		assertThat(report.getStatus(), equalTo(WorkStatus.FAILED));
	}

	public WorkContext getSampleWorkContext() {
		WorkContext workContext = new WorkContext();
		workContext.put(MOVE2KUBE_TRANSFORM_ID, MOVE2KUBE_PROJECT_ID);
		workContext.put(MOVE2KUBE_PROJECT_ID, MOVE2KUBE_PROJECT_ID);
		workContext.put(MOVE2KUBE_WORKSPACE_ID, MOVE2KUBE_WORKSPACE_ID);
		assertDoesNotThrow(() -> workContext.put(GIT_DESTINATION_CONTEXT_KEY, createTempDir()));
		return workContext;
	}

	private String createTempDir() throws IOException {
		return Files.createTempDirectory("test").toAbsolutePath().toString();
	}

	private static File createSampleZip() throws IOException {
		String zipFileName = "/tmp/test%s.zip".formatted(UUID.randomUUID());
		String tempDir = "/tmp/%s/".formatted(UUID.randomUUID().toString());
		File zipFile = new File(zipFileName);
		String[] fileNames = { "output/project-name/source/output/src/test.txt", "output/project-name/deploy/bar.txt",
				"output/project-name/deploy/foo.txt", "output/project-name/scripts/bar.sh",
				"output/project-name/scripts/foo.sh" };

		byte[] buffer = new byte[1024];

		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);

		for (String fileName : fileNames) {
			File file = new File("%s%s".formatted(tempDir, fileName));
			if (file.getParentFile() != null && !file.getParentFile().exists()) {
				assertThat(file.getParentFile().mkdirs(), is(true));
			}
			assertThat(file.createNewFile(), is(true));
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
