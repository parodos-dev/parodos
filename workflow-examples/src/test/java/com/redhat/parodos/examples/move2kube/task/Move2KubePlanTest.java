package com.redhat.parodos.examples.move2kube.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import dev.parodos.move2kube.ApiException;
import dev.parodos.move2kube.api.PlanApi;
import dev.parodos.move2kube.api.ProjectInputsApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Move2KubePlanTest {

	private static final String WORKSPACE = "workspace";

	private static final String PROJECT = "project";

	private Move2KubePlan task;

	private ProjectInputsApi projectInputsApi;

	private PlanApi planApi;

	@BeforeEach
	public void setup() {
		projectInputsApi = mock(ProjectInputsApi.class);
		planApi = mock(PlanApi.class);

		task = new Move2KubePlan("http://localhost:8080", planApi, projectInputsApi);
		task.setSleepTime(1);
	}

	@Test
	public void testParameters() {
		assertThat(task.getWorkFlowTaskParameters(), hasSize(0));
	}

	@Test
	public void testExecute() throws ApiException {
		// given
		WorkContext context = getWorkContext();
		assertDoesNotThrow(() -> {
			when(projectInputsApi.createProjectInput(any(), any(), any(), any(), any(), any())).thenReturn(null);
		});
		when(planApi.getPlan(any(), any())).thenThrow(IllegalArgumentException.class);
		// when
		WorkReport report = task.execute(context);

		// then
		assertThat(report.getError(), is(nullValue()));
		assertThat(report.getStatus(), equalTo(WorkStatus.COMPLETED));

		assertDoesNotThrow(() -> {
			verify(projectInputsApi, times(1)).createProjectInput(eq(WORKSPACE), eq(PROJECT), eq("sources"), eq("Id"),
					anyString(), any());
		});
	}

	@Test
	public void testExecuteFail() throws ApiException {
		// given
		WorkContext context = getWorkContext();
		assertDoesNotThrow(() -> {
			when(projectInputsApi.createProjectInput(any(), any(), any(), any(), any(), any())).thenReturn(null);
		});
		when(planApi.getPlan(any(), any())).thenThrow(ApiException.class);
		// when
		WorkReport report = task.execute(context);

		// then
		assertThat(report.getStatus(), equalTo(WorkStatus.FAILED));
	}

	WorkContext getWorkContext() {
		WorkContext context = new WorkContext();
		context.put("move2KubeWorkspaceID", "workspace");
		context.put("move2KubeProjectID", "project");
		assertDoesNotThrow(() -> context.put("gitArchivePath", createSampleZip().getAbsolutePath()));
		return context;
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
