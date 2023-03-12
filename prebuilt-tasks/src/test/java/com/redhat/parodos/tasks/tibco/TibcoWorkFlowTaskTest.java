package com.redhat.parodos.tasks.tibco;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jms.JMSException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TibcoWorkFlowTaskTest {

	private final static String caFile = "/cafile";

	private final static String url = "ssl://localhost:7222";

	private final static String username = "username";

	private final static String passowrd = "password";

	private final static String topic = "topic";

	private final static String message = "test message";

	private final static Tibjms tibjms = Mockito.mock(Tibjms.class);

	private final static TibcoWorkFlowTask task = new TibcoWorkFlowTask(tibjms, url, caFile, username, passowrd);

	@Before
	public void setUp() {
		task.setBeanName("Test");
	}

	@Test
	public void executeMissingParameter() {
		WorkContext ctx = getWorkContext(false);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
	}

	@Test
	public void executeErrorInTibco() throws JMSException {
		WorkContext ctx = getWorkContext(true);
		doThrow(JMSException.class).when(tibjms).sendMessage(any(), any(), any(), any(), any(), any());
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(JMSException.class, result.getError().getClass());
	}

	@Test
	public void executeSuccess() throws JMSException {
		WorkContext ctx = getWorkContext(true);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(tibjms, times(1)).sendMessage(url, caFile, username, passowrd, topic, message);
	}

	private WorkContext getWorkContext(boolean withParams) {
		WorkContext ctx = new WorkContext();
		HashMap<String, String> map = new HashMap<>();
		if (withParams) {
			map.put("topic", topic);
			map.put("message", message);
		}

		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, task.getName(),
				WorkContextDelegate.Resource.ARGUMENTS, map);
		return ctx;
	}

}
