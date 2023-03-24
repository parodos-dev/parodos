package com.redhat.parodos.tasks.tibco;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

public class TibcoWorkFlowTaskTest {

	private final static String topic = "topic";

	private final static String message = "test message";

	private final static TibcoMessageService tibjms = Mockito.mock(TibcoMessageService.class);

	private final static TibcoWorkFlowTask task = new TibcoWorkFlowTask(tibjms);

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
		doThrow(JMSException.class).when(tibjms).sendMessage(any(), any());
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(JMSException.class, result.getError().getClass());
	}

	@Test
	public void executeSuccess() throws JMSException {
		WorkContext ctx = getWorkContext(true);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(tibjms, times(1)).sendMessage(topic, message);
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
