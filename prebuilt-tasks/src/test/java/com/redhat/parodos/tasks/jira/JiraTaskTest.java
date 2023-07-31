package com.redhat.parodos.tasks.jira;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JiraTaskTest {

	JiraTask underTest;

	JiraTask.JiraClient mockClient;

	WorkContext ctx;

	@BeforeEach
	public void setUp() {
		this.mockClient = mock(JiraTask.JiraClient.class);
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

		assertThat(execute.getError(), is(instanceOf(Exception.class)));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		verify(mockClient, times(1)).get(anyString());
		verify(mockClient, times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, times(0)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void createFails() {
		ctx.put("id", "");
		when(mockClient.create(anyString(), anyString(), anyString()))
				.thenThrow(new Exception("Missing mandatory params to create a ticket"));
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(instanceOf(Exception.class)));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		verify(mockClient, times(0)).get(anyString());
		verify(mockClient, times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, times(1)).create(anyString(), anyString(), anyString());
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

		assertThat(execute.getError(), is(instanceOf(Exception.class)));
		assertThat(execute.getError(), is(not(instanceOf(JSONException.class))));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		verify(mockClient, times(1)).get(anyString());
		verify(mockClient, times(1)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, times(0)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void getById() {
		ctx.put("id", "123");

		when(mockClient.get(anyString())).thenReturn(
				new JSONObject().put("id", "123").put("project", "parodos").put("summary", "some issue summary")
						.put("description", "issue description").put("status", "in-progress"));

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		verify(mockClient, times(1)).get(anyString());
		verify(mockClient, times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, times(0)).create(anyString(), anyString(), anyString());
	}

	@Test
	@SneakyThrows
	public void createCompletes() {
		ctx.put("id", "");

		when(mockClient.create(anyString(), anyString(), anyString())).thenReturn(
				new JSONObject().put("id", "123").put("project", "parodos").put("summary", "some issue summary")
						.put("description", "issue description").put("status", "in-progress"));

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		verify(mockClient, times(0)).get(anyString());
		verify(mockClient, times(0)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, times(1)).create(anyString(), anyString(), anyString());
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

		assertThat(execute.getError(), is(instanceOf(Exception.class)));
		assertThat(execute.getError(), is(not(instanceOf(JSONException.class))));
		assertThat(execute.getStatus(), equalTo(WorkStatus.FAILED));
		verify(mockClient, times(1)).get(anyString());
		verify(mockClient, times(1)).update(anyString(), anyString(), anyString(), anyString());
		verify(mockClient, times(0)).create(anyString(), anyString(), anyString());
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

		assertThat(execute.getError(), is(nullValue()));
		assertThat(execute.getStatus(), equalTo(WorkStatus.COMPLETED));
		verify(mockClient, times(1)).get("123");
		verify(mockClient, times(1)).addComment("123", "new comment");
		verify(mockClient, times(0)).create(anyString(), anyString(), anyString());
	}

}
