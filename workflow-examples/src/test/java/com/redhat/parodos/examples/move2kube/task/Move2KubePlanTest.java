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
import dev.parodos.move2kube.api.PlanApi;
import dev.parodos.move2kube.api.ProjectInputsApi;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class Move2KubePlanTest {

	private static String workspace = "workspace";

	private static String project = "project";

	private Move2KubePlan task;

	private ProjectInputsApi projectInputsApi;

	private PlanApi planApi;

	@Before
	public void setup() {
		projectInputsApi = mock(ProjectInputsApi.class);
		planApi = mock(PlanApi.class);

		task = new Move2KubePlan("http://localhost:8080", planApi, projectInputsApi);
	}

	@Test
	public void testParameters() {
		assertThat(task.getWorkFlowTaskParameters().size()).isEqualTo(0);
	}

	@Test
	public void testExecute() {
		// given
		WorkContext context = getWorkContext();
		assertDoesNotThrow(() -> {
			when(projectInputsApi.createProjectInput(any(), any(), any(), any(), any(), any())).thenReturn(null);
		});

		// when
		WorkReport report = task.execute(context);

		// then
		assertThat(report.getError()).isNull();
		assertThat(report.getStatus()).isEqualTo(WorkStatus.COMPLETED);

		assertDoesNotThrow(() -> {
			verify(projectInputsApi, times(1)).createProjectInput(eq(workspace), eq(project), eq("sources"), eq("Id"),
					anyString(), any());
		});
	}

	WorkContext getWorkContext() {
		WorkContext context = new WorkContext();
		context.put("move2KubeWorkspaceID", "workspace");
		context.put("move2KubeProjectID", "project");
		assertDoesNotThrow(() -> {
			context.put("gitArchivePath", createSampleZip().getAbsolutePath().toString());
		});
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
				assertThat(file.getParentFile().mkdirs()).isTrue();
			}
			assertThat(file.createNewFile()).isTrue();

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
