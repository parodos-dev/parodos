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
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Move2KubePlanTest {

	private static String workspace = "workspace";

	private static String project = "project";

	Move2KubePlan task;

	ProjectInputsApi projectInputsApi;

	PlanApi planApi;

	@Before
	public void setup() {
		projectInputsApi = Mockito.mock(ProjectInputsApi.class);
		planApi = Mockito.mock(PlanApi.class);

		task = new Move2KubePlan("http://localhost:8080", planApi, projectInputsApi);
	}

	@Test
	public void testParameters() {
		assertEquals(task.getWorkFlowTaskParameters().size(), 0);
	}

	@Test
	public void testExecute() {
		// given
		WorkContext context = getWorkContext();
		assertDoesNotThrow(() -> {
			Mockito.when(projectInputsApi.createProjectInput(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.any())).thenReturn(null);
		});

		// when

		WorkReport report = task.execute(context);

		// then
		assertNull(report.getError());
		assertEquals(report.getStatus(), WorkStatus.COMPLETED);

		assertDoesNotThrow(() -> {
			Mockito.verify(projectInputsApi, Mockito.times(1)).createProjectInput(Mockito.eq(workspace),
					Mockito.eq(project), Mockito.eq("sources"), Mockito.eq("Id"), Mockito.anyString(), Mockito.any());
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
