package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class ProcessAnalysisTaskTest {

	ProcessAnalysisTask underTest;

	WorkFlowOption passCriteria = new WorkFlowOption.Builder("pass", "nextWorkflow").displayName("next workflow to run")
			.build();

	WorkFlowOption failCriteria = new WorkFlowOption.Builder("fail", "failWorkflow")
			.displayName("rerun current workflow").build();

	WorkContext ctx;

	@Mock
	Notifier notifier;

	@BeforeEach
	public void setUp() {
		openMocks(this);
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void criteriaCheck() {
		// given
		underTest = new ProcessAnalysisTask(passCriteria, failCriteria,
				analysisIncidents -> analysisIncidents.mandatory() > 0, notifier);
		underTest.setBeanName("wf");
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());

		Path tempFile = Files.createTempFile(this.getClass().getName(), "reports");
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile.toFile()));
		bufferedWriter.write(MTAAnalysisReportTest.validJS);
		bufferedWriter.close();

		var wfParams = Map.of("reportURL", tempFile.toAbsolutePath());
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, "wf",
				WorkContextDelegate.Resource.ARGUMENTS, wfParams);

		underTest.preExecute(ctx);

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		verify(notifier, times(1)).send(eq(passCriteria.getDisplayName()), anyString());
	}

}
