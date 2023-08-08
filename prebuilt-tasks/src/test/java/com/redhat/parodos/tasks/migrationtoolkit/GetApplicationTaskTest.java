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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_ID;
import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_NAME;
import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.REPO_BRANCH;
import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.REPO_URL;
import static java.util.Map.entry;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetApplicationTaskTest {

	GetApplicationTask underTest;

	MTAApplicationClient mockClient;

	WorkFlowTaskLogger taskLoggerMock;

	WorkContext ctx;

	@BeforeEach
	public void setUp() {
		mockClient = mock(MTAApplicationClient.class);
		taskLoggerMock = mock(WorkFlowTaskLogger.class);
		underTest = spy(new GetApplicationTask());
		underTest.mtaClient = mockClient;
		underTest.taskLogger = taskLoggerMock;
		underTest.setBeanName("GetApplicationTask");
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
		verify(this.mockClient, times(0)).getApp(anyString());
	}

	@Test
	@SneakyThrows
	public void failsGettingAppNotFound() {
		when(mockClient.getApp(anyString())).thenReturn(new Result.Failure<>(new Exception("not found")));
		doReturn(APP_NAME).when(this.underTest).getRequiredParameterValue(eq("applicationName"));
		doNothing().when(taskLoggerMock).logErrorWithSlf4j(any());

		ctx.put("applicationName", APP_NAME);
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(instanceOf(Exception.class)));
		assertThat(execute.getError(), is(not(instanceOf(MissingParameterException.class))));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		verify(mockClient, times(1)).getApp(anyString());
	}

	@Test
	@SneakyThrows
	public void getByName() {
		when(mockClient.getApp(anyString())).thenReturn(
				new Result.Success<>(new App(APP_ID, APP_NAME, new Repository("git", REPO_URL, REPO_BRANCH), null)));
		doReturn(APP_NAME).when(this.underTest).getRequiredParameterValue(eq("applicationName"));
		doNothing().when(taskLoggerMock).logErrorWithSlf4j(anyString(), anyString());

		ctx.put("applicationName", APP_NAME);
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

		assertThat(workflowExecutionArguments, hasKey("applicationID"));
		assertThat(workflowExecutionArguments.get("applicationID"), equalTo((APP_ID)));
		verify(mockClient, times(1)).getApp(anyString());
		verify(mockClient, times(0)).create(any());

	}

}
