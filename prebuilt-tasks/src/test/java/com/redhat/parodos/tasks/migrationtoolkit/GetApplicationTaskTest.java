package com.redhat.parodos.tasks.migrationtoolkit;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_ID;
import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_NAME;
import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.REPO_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetApplicationTaskTest {

	GetApplicationTask underTest;

	@Mock
	MTAApplicationClient mockClient;

	WorkContext ctx;

	@Before
	public void setUp() {
		underTest = new GetApplicationTask();
		underTest.mtaClient = mockClient;
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void missingMandatoryParams() {
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isInstanceOf(MissingParameterException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		verify(mockClient, Mockito.times(0)).get(anyString());
	}

	@Test
	@SneakyThrows
	public void failsGettingAppNotFound() {
		when(mockClient.get(anyString())).thenReturn(new Result.Failure<>(new Exception("not found")));
		ctx.put("applicationName", APP_NAME);

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isInstanceOf(Exception.class);
		assertThat(execute.getError()).isNotInstanceOf(MissingParameterException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		verify(mockClient, Mockito.times(1)).get(anyString());
	}

	@Test
	@SneakyThrows
	public void getByName() {
		when(mockClient.get(anyString()))
				.thenReturn(new Result.Success<>(new App(APP_ID, APP_NAME, new Repository("git", REPO_URL))));
		ctx.put("applicationName", APP_NAME);

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isNull();
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		assertThat(execute.getWorkContext().getEntrySet()).contains(entry("applicationID", APP_ID));
		verify(mockClient, Mockito.times(1)).get(anyString());
		verify(mockClient, Mockito.times(0)).create(any());

	}

}