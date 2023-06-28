package com.redhat.parodos.tasks.migrationtoolkit;

import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetAnalysisTaskTest {

	GetAnalysisTask underTest;

	@Mock
	MTATaskGroupClient mockClient;

	WorkContext ctx;

	@Before
	public void setUp() {
		underTest = new GetAnalysisTask(null, null, null);
		underTest.mtaClient = mockClient;
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void missingMandatoryParams() {
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isInstanceOf(MissingParameterException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(execute.getWorkContext().get("taskGroupID")).isNull();
		verify(mockClient, times(0)).get(anyInt());
	}

	@Test
	@SneakyThrows
	public void failsGetTaskGroup() {
		when(mockClient.get(anyInt())).thenReturn(new Result.Failure<>(new Exception("not found")));

		ctx.put("taskGroupID", "123");
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isNotInstanceOf(MissingParameterException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(execute.getWorkContext().get("taskGroupID")).isEqualTo("123");
		verify(mockClient, times(1)).get(anyInt());
	}

	@Test
	@SneakyThrows
	public void getByID() {
		var taskGroupID = "1";
		ctx.put("taskGroupID", taskGroupID);
		when(mockClient.get(Integer.parseInt(taskGroupID))).thenReturn(new Result.Success<>(successfulGet()));
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isNull();
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		assertThat(execute.getWorkContext().getEntrySet()).contains(entry("taskGroupID", taskGroupID));
		verify(mockClient, times(1)).get(eq(Integer.parseInt(taskGroupID)));
		verify(mockClient, times(0)).create(anyInt());
	}

	static TaskGroup successfulGet() {
		return new TaskGroup(0, "taskgroups.windup", "Ready", "windup",
				new Data(new Mode(false, false, false, ""), "/windup/report", new Rules("", null),
						new Scope(false, new Packages(new String[] {}, new String[] {})), new String[] {},
						new String[] { "cloud-readiness" }),
				null, new Task[] { new Task(new App(APP_ID, "parodos", null, null), "Succeeded",
						String.format("parodos.%s.windup", APP_ID), null, null, null) });
	}

}
