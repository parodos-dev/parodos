package com.redhat.parodos.tasks.migrationtoolkit;

import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_ID;
import static com.redhat.parodos.workflows.workflow.WorkContextAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
public class SubmitAnalysisTaskTest {

	SubmitAnalysisTask underTest;

	@Mock
	MTATaskGroupClient mockClient;

	WorkContext ctx;

	@BeforeEach
	public void setUp() {
		underTest = new SubmitAnalysisTask();
		underTest.mtaClient = mockClient;
		underTest.setBeanName("SubmitAnalysisTask");
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void missingMandatoryParams() {
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(instanceOf(MissingParameterException.class)));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(execute.getWorkContext().get("taskGroup"), is(nullValue()));
		verify(mockClient, times(0)).create(anyString());
	}

	@Test
	@SneakyThrows
	public void failsCreatingTaskGroup() {
		when(mockClient.create(anyString())).thenReturn(new Result.Failure<>(new Exception("not found")));
		ctx.put("applicationID", "123");
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(not(instanceOf(MissingParameterException.class))));
		assertThat(execute.getError(), is(instanceOf(Exception.class)));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(execute.getWorkContext().get("taskGroup"), is(nullValue()));
		verify(mockClient, times(1)).create(anyString());
	}

	@Test
	@SneakyThrows
	public void createCompletes() {
		String taskGroupID = "1";
		ctx.put("applicationID", APP_ID);
		when(mockClient.create(APP_ID)).thenReturn(new Result.Success<>(of(taskGroupID, APP_ID)));
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(execute.getWorkContext()).hasEntryKey("analysisTaskGroup");
		assertThat(((TaskGroup) execute.getWorkContext().get("analysisTaskGroup")).id(), equalTo(taskGroupID));
		verify(mockClient, times(1)).create(APP_ID);
	}

	@NotNull
	private static TaskGroup of(String id, String appID) {
		return new TaskGroup(id, "", "", "", null, null,
				new Task[] { new Task(new App(appID, "", null, null), "", "", "", null, "") });
	}

}
