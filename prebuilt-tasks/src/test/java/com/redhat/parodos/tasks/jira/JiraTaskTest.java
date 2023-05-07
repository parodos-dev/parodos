package com.redhat.parodos.tasks.jira;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraTaskTest {

	JiraTask underTest;

	@Mock
	JiraTask.JiraClient mockClient;

	WorkContext ctx;

	@Before
	public void setUp() {
		underTest = new JiraTask();
		underTest.setBeanName("jiraTask");
		underTest.jiraClient = mockClient;
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void failsNotFound() {
		ctx.put("id", "non-existing");
		when(mockClient.get(anyString())).thenThrow(new Exception("not found"));

		WorkReport execute = underTest.execute(ctx);

		Assertions.assertThat(execute.getError()).isInstanceOf(Exception.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		verify(mockClient, Mockito.times(1)).get(anyString());
		verify(mockClient, Mockito.times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, Mockito.times(0)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void createFails() {
		ctx.put("id", "");
		when(mockClient.create(anyString(), anyString(), anyString()))
				.thenThrow(new Exception("Missing mandatory params to create a ticket"));
		WorkReport execute = underTest.execute(ctx);

		Assertions.assertThat(execute.getError()).isInstanceOf(Exception.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		verify(mockClient, Mockito.times(0)).get(anyString());
		verify(mockClient, Mockito.times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, Mockito.times(1)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void updateFails() {
		ctx.put("id", "123");
		ctx.put("description", "new description");
		when(mockClient.get(anyString())).thenReturn(
				new JSONObject().put("id", "123").put("project", "parodos").put("summary", "some issue summary")
						.put("description", "issue description").put("status", "in-progress"));
		doThrow(new Exception()).when(mockClient).update(anyString(), anyString(), anyString(), anyString());

		WorkReport execute = underTest.execute(ctx);

		Assertions.assertThat(execute.getError()).isInstanceOf(Exception.class);
		Assertions.assertThat(execute.getError()).isNotInstanceOf(JSONException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		verify(mockClient, Mockito.times(1)).get(anyString());
		verify(mockClient, Mockito.times(1)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, Mockito.times(0)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void getById() {
		ctx.put("id", "123");

		when(mockClient.get(anyString())).thenReturn(
				new JSONObject().put("id", "123").put("project", "parodos").put("summary", "some issue summary")
						.put("description", "issue description").put("status", "in-progress"));

		WorkReport execute = underTest.execute(ctx);

		Assertions.assertThat(execute.getError()).isNull();
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		verify(mockClient, Mockito.times(1)).get(anyString());
		verify(mockClient, Mockito.times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, Mockito.times(0)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void createCompletes() {
		ctx.put("id", "");

		when(mockClient.create(anyString(), anyString(), anyString())).thenReturn(
				new JSONObject().put("id", "123").put("project", "parodos").put("summary", "some issue summary")
						.put("description", "issue description").put("status", "in-progress"));

		WorkReport execute = underTest.execute(ctx);

		Assertions.assertThat(execute.getError()).isNull();
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		verify(mockClient, Mockito.times(0)).get(anyString());
		verify(mockClient, Mockito.times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, Mockito.times(1)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void updateCompletes() {
		ctx.put("id", "123");
		ctx.put("description", "new description");
		when(mockClient.get(anyString())).thenReturn(
				new JSONObject().put("id", "123").put("project", "parodos").put("summary", "some issue summary")
						.put("description", "issue description").put("status", "in-progress"));
		doThrow(new Exception()).when(mockClient).update(anyString(), anyString(), anyString(), anyString());

		WorkReport execute = underTest.execute(ctx);

		Assertions.assertThat(execute.getError()).isInstanceOf(Exception.class);
		Assertions.assertThat(execute.getError()).isNotInstanceOf(JSONException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		verify(mockClient, Mockito.times(1)).get(anyString());
		verify(mockClient, Mockito.times(1)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, Mockito.times(0)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void AddCommentCompletes() {
		ctx.put("id", "123");
		ctx.put("comment", "new comment");
		when(mockClient.get(anyString())).thenReturn(
				new JSONObject().put("id", "123").put("project", "parodos").put("summary", "some issue summary")
						.put("description", "issue description").put("status", "in-progress"));
		doNothing().when(mockClient).update(anyString(), anyString(), anyString(), anyString());
		doNothing().when(mockClient).addComment(anyString(), anyString());

		WorkReport execute = underTest.execute(ctx);

		Assertions.assertThat(execute.getError()).isNull();
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		verify(mockClient, Mockito.times(1)).get("123");
		verify(mockClient, Mockito.times(1)).addComment("123", "new comment");
		verify(mockClient, Mockito.times(0)).create(anyString(), anyString(), anyString());
	}

}