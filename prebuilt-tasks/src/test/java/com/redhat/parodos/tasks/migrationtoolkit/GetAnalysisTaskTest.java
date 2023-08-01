package com.redhat.parodos.tasks.migrationtoolkit;

import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_ID;
import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
public class GetAnalysisTaskTest {

	GetAnalysisTask underTest;

	MTATaskGroupClient mockClient;

	WorkContext ctx;

	@BeforeEach
	public void setUp() {
		this.mockClient = mock(MTATaskGroupClient.class);
		underTest = new GetAnalysisTask(null, null, null);
		underTest.mtaClient = mockClient;
		underTest.setBeanName("MTATaskGroupClient");
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	@Disabled
	// FIXME
	public void missingMandatoryParams() {
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(instanceOf(MissingParameterException.class)));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(execute.getWorkContext().get("taskGroupID"), is(nullValue()));
		verify(mockClient, times(0)).get(anyInt());
	}

	@Test
	@SneakyThrows
	@Disabled
	// FIXME
	public void failsGetTaskGroup() {
		when(mockClient.get(anyInt())).thenReturn(new Result.Failure<>(new Exception("not found")));

		ctx.put("taskGroupID", "123");
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(not(instanceOf(MissingParameterException.class))));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(execute.getWorkContext().get("taskGroupID"), equalTo("123"));
		verify(mockClient, times(1)).get(anyInt());
	}

	@Test
	@SneakyThrows
	@Disabled
	// FIXME
	public void getByID() {
		var taskGroupID = "1";
		ctx.put("taskGroupID", taskGroupID);
		when(mockClient.get(Integer.parseInt(taskGroupID))).thenReturn(new Result.Success<>(successfulGet()));
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(execute.getWorkContext().getEntrySet(), hasItem(entry("taskGroupID", taskGroupID)));
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
