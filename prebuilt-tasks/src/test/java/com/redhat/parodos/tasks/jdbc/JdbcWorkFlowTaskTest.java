package com.redhat.parodos.tasks.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JdbcWorkFlowTaskTest {

	private final JdbcService service = mock(JdbcService.class);

	private final JdbcWorkFlowTask task = new JdbcWorkFlowTask("Test", service);

	@Test
	public void missingArgs() {
		WorkContext ctx = createWorkContext(null);
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
	}

	@Test
	public void query() {
		WorkContext ctx = createWorkContext(JdbcWorkFlowTask.OperationType.QUERY.name());
		List<Map<String, Object>> resultSet = List.of(Map.of("name", "value"));
		String resultJson = "[{\"name\":\"value\"}]";
		doReturn(resultSet).when(service).query(any(), any());
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		assertEquals(resultJson, ctx.get("thekey"));
	}

	@Test
	public void update() {
		WorkContext ctx = createWorkContext(JdbcWorkFlowTask.OperationType.UPDATE.name());
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(service, times(1)).update("theurl", "thestatement");
	}

	@Test
	public void execute() {
		WorkContext ctx = createWorkContext(JdbcWorkFlowTask.OperationType.EXECUTE.name());
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(service, times(1)).execute("theurl", "thestatement");
	}

	private WorkContext createWorkContext(String operation) {
		WorkContext ctx = new WorkContext();
		HashMap<String, String> map = new HashMap<>();
		map.put("url", "theurl");

		if (operation != null) {
			map.put("operation", operation);
		}

		map.put("statement", "thestatement");
		map.put("result-ctx-key", "thekey");

		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, task.getName(),
				WorkContextDelegate.Resource.ARGUMENTS, map);
		WorkContextUtils.setMainExecutionId(ctx, UUID.randomUUID());
		return ctx;
	}

}
