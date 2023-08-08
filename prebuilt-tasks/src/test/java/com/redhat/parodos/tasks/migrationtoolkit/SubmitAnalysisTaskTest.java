package com.redhat.parodos.tasks.migrationtoolkit;

import java.util.Map;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.log.WorkFlowTaskLogger;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_ID;
import static com.redhat.parodos.workflows.workflow.WorkContextAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class SubmitAnalysisTaskTest {

	SubmitAnalysisTask underTest;

	@Mock
	MTATaskGroupClient mockClient;

	@Mock
	WorkFlowTaskLogger taskLoggerMock;

	WorkContext ctx;

	@BeforeEach
	public void setUp() {
		openMocks(this);
		underTest = spy(new SubmitAnalysisTask());
		underTest.mtaClient = mockClient;
		underTest.taskLogger = taskLoggerMock;
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
		doNothing().when(taskLoggerMock).logErrorWithSlf4j(any());

		doReturn("123").when(this.underTest).getRequiredParameterValue(eq("applicationID"));

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
		doReturn(APP_ID).when(this.underTest).getRequiredParameterValue(eq("applicationID"));
		ctx.put("applicationID", APP_ID);
		when(mockClient.create(APP_ID)).thenReturn(new Result.Success<>(of(taskGroupID, APP_ID)));
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(execute.getWorkContext().getEntrySet(),
				hasItem(allOf(hasProperty("key", equalTo("WORKFLOW_EXECUTION_ARGUMENTS")),
						hasProperty("value", instanceOf(Map.class)))));

		Map<String, Object> workflowExecutionArguments = (Map<String, Object>) execute.getWorkContext().getEntrySet()
				.stream().filter(entry -> entry.getKey().equals("WORKFLOW_EXECUTION_ARGUMENTS")).findFirst()
				.map(Map.Entry::getValue).orElse(null);

		assertThat(workflowExecutionArguments, hasKey("taskGroupID"));
		assertThat(workflowExecutionArguments.get("taskGroupID"), equalTo(taskGroupID));
		verify(mockClient, times(1)).create(APP_ID);
	}

	@NotNull
	private static TaskGroup of(String id, String appID) {
		return new TaskGroup(id, "", "", "", null, null,
				new Task[] { new Task(new App(appID, "", null, null), "", "", "", null, "") });
	}

}
