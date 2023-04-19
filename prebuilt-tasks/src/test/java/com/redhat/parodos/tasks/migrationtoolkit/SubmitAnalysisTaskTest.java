package com.redhat.parodos.tasks.migrationtoolkit;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.redhat.parodos.workflows.workflow.WorkContextAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubmitAnalysisTaskTest {

	SubmitAnalysisTask underTest;

	@Mock
	MTATaskGroupClient mockClient;

	WorkContext ctx;

	@Before
	public void setUp() {
		underTest = new SubmitAnalysisTask();
		underTest.mtaClient = mockClient;
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void missingMandatoryParams() {
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isInstanceOf(MissingParameterException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(execute.getWorkContext().get("taskGroup")).isNull();
		verify(mockClient, Mockito.times(0)).create(anyInt());
	}

	@Test
	@SneakyThrows
	public void failsCreatingTaskGroup() {
		when(mockClient.create(anyInt())).thenReturn(new Result.Failure<>(new Exception("not found")));
		ctx.put("applicationID", "123");
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isNotInstanceOf(MissingParameterException.class);
		assertThat(execute.getError()).isInstanceOf(Exception.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(execute.getWorkContext().get("taskGroup")).isNull();
		verify(mockClient, Mockito.times(1)).create(anyInt());
	}

	@Test
	@SneakyThrows
	public void createCompletes() {
		int taskGroupID = 1;
		int appID = 123;
		ctx.put("applicationID", Integer.toString(appID));
		when(mockClient.create(appID)).thenReturn(new Result.Success<>(of(taskGroupID, appID)));

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isNull();
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		assertThat(execute.getWorkContext()).hasEntryKey("analysisTaskGroup");
		assertThat(((TaskGroup) execute.getWorkContext().get("analysisTaskGroup")).id()).isEqualTo(taskGroupID);
		verify(mockClient, Mockito.times(1)).create(appID);
	}

	@NotNull
	private static TaskGroup of(int id, int appID) {
		return new TaskGroup(id, "", "", "", null, "",
				new Task[] { new Task(new App(appID, "", null), "", "", "", null, "") });
	}

}
