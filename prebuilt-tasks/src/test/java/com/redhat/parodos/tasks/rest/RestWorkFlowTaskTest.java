package com.redhat.parodos.tasks.rest;

import java.util.HashMap;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

public class RestWorkFlowTaskTest {

	private RestService restService = Mockito.mock(RestService.class);

	private RestWorkFlowTask task = new RestWorkFlowTask("Test", restService);

	HashMap<String, String> map;

	@Before
	public void setUp() {
		map = new HashMap<>();
	}

	@Test
	public void missingUrl() {
		map.put("method", "get");

		WorkReport result = task.execute(createWorkContext(map));

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
	}

	@Test
	public void missingMethod() {
		map.put("url", "http://localhost");

		WorkReport result = task.execute(createWorkContext(map));

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
	}

	@Test
	public void invalidMethod() {
		map.put("url", "http://localhost");
		map.put("method", "drop");

		WorkReport result = task.execute(createWorkContext(map));

		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(IllegalArgumentException.class, result.getError().getClass());
	}

	@Test
	public void get() {
		map.put("url", "http://localhost");
		map.put("method", "get");
		map.put("response-key", "http-body");

		ResponseEntity<String> obj = new ResponseEntity<>("body", HttpStatus.OK);

		doReturn(obj).when(restService).exchange(any(), any(), any());

		WorkContext ctx = createWorkContext(map);

		WorkReport result = task.execute(ctx);

		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		assertEquals(obj.getBody(), ctx.get("http-body"));
	}

	@Test
	public void post() {
		map.put("url", "http://localhost");
		map.put("method", "post");
		map.put("content", "{\"root\": \"value\"}");
		map.put("username", "test");
		map.put("password", "test");

		ResponseEntity<String> obj = new ResponseEntity<>(HttpStatus.OK);

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				HttpEntity<String> request = invocationOnMock.getArgument(2);
				HttpHeaders headers = request.getHeaders();

				assertEquals(map.get("content"), request.getBody());
				assertEquals(1, headers.getAccept().size());
				assertNotNull(headers.getContentType());
				assertTrue(headers.containsKey("Authorization"));

				return obj;
			}
		}).when(restService).exchange(any(), any(), any());

		WorkContext ctx = createWorkContext(map);

		WorkReport result = task.execute(ctx);

		assertEquals(WorkStatus.COMPLETED, result.getStatus());
	}

	@Test
	public void requestNot2xx() {
		map.put("url", "http://localhost");
		map.put("method", "get");

		ResponseEntity<String> obj = new ResponseEntity<>(HttpStatus.MOVED_PERMANENTLY);

		doReturn(obj).when(restService).exchange(any(), any(), any());

		WorkContext ctx = createWorkContext(map);

		assertThrows(RestClientException.class, () -> task.execute(ctx));
	}

	private WorkContext createWorkContext(HashMap<String, String> map) {
		WorkContext ctx = new WorkContext();
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, task.getName(),
				WorkContextDelegate.Resource.ARGUMENTS, map);
		return ctx;
	}

}
