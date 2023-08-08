package com.redhat.parodos.tasks.migrationtoolkit;

import java.util.UUID;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.log.WorkFlowTaskLogger;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetAnalysisTaskTest {

	GetAnalysisTask underTest;

	MTATaskGroupClient mockClient;

	WorkFlowTaskLogger taskLoggerMock;

	WorkContext ctx;

	@BeforeEach
	public void setUp() {
		this.mockClient = mock(MTATaskGroupClient.class);
		taskLoggerMock = mock(WorkFlowTaskLogger.class);
		Notifier notifier = mock(Notifier.class);
		underTest = spy(new GetAnalysisTask(null, null, notifier));
		underTest.mtaClient = mockClient;
		underTest.taskLogger = taskLoggerMock;
		underTest.setBeanName("MTATaskGroupClient");
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
		assertThat(execute.getWorkContext().get("taskGroupID"), is(nullValue()));
		verify(mockClient, times(0)).getTaskGroup(anyString());
	}

	@Test
	@SneakyThrows
	public void failsGetTaskGroup() {
		when(mockClient.getTaskGroup(anyString())).thenReturn(new Result.Failure<>(new Exception("not found")));
		doReturn("123").when(this.underTest).getRequiredParameterValue(eq("taskGroupID"));

		ctx.put("taskGroupID", "123");
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(not(instanceOf(MissingParameterException.class))));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(execute.getWorkContext().get("taskGroupID"), equalTo("123"));
		verify(mockClient, times(1)).getTaskGroup(anyString());
	}

	@Test
	@SneakyThrows
	public void getByID() {
		var taskGroupID = "1";
		ctx.put("taskGroupID", taskGroupID);
		doReturn(taskGroupID).when(this.underTest).getRequiredParameterValue(eq("taskGroupID"));
		when(mockClient.getTaskGroup(taskGroupID)).thenReturn(new Result.Success<>(successfulGet()));
		doNothing().when(taskLoggerMock).logErrorWithSlf4j(any());

		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(execute.getWorkContext().getEntrySet(), hasItem(entry("taskGroupID", taskGroupID)));
		verify(mockClient, times(1)).getTaskGroup(eq(taskGroupID));
		verify(mockClient, times(0)).create(anyString());
	}

	static TaskGroup successfulGet() {
		return new TaskGroup("0", "taskgroups.windup", "Ready", "windup",
				new Data(new Mode(false, false, false, ""), "/windup/report", new Rules("", null),
						new Scope(false, new Packages(new String[] {}, new String[] {})), new String[] {},
						new String[] { "cloud-readiness" }),
				null, new Task[] { new Task(new App(APP_ID, "parodos", null, null), "Succeeded",
						String.format("parodos.%s.windup", APP_ID), null, null, null) });
	}

}
