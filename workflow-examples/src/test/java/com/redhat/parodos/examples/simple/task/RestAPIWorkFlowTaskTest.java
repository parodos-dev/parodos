package com.redhat.parodos.examples.simple.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Rest API WorkFlow Task execution test
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public class RestAPIWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private RestAPIWorkFlowTask restAPIWorkflowTask;

	private final String valueUrl = "value_url";

	private final String valuePayload = "value_payload";

	private static final String PAYLOAD_KEY = "payload";

	private static final String URL_KEY = "url";

	@Before
	public void setUp() {
		this.restAPIWorkflowTask = spy((RestAPIWorkFlowTask) getConcretePersonImplementation());

		try {
			doReturn(valueUrl).when(this.restAPIWorkflowTask).getParameterValue(Mockito.any(WorkContext.class),
					eq(URL_KEY));
			doReturn(valuePayload).when(this.restAPIWorkflowTask).getParameterValue(Mockito.any(WorkContext.class),
					eq(PAYLOAD_KEY));

		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new RestAPIWorkFlowTask();
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.executePost(eq(valueUrl), eq(valuePayload)))
					.thenReturn(new ResponseEntity("body", HttpStatus.OK));
			// when
			WorkReport workReport = restAPIWorkflowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);

		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.executePost(eq(valueUrl), eq(valuePayload)))
					.thenReturn(new ResponseEntity("body", HttpStatus.BAD_REQUEST));

			// when
			WorkReport workReport = restAPIWorkflowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	public void testGetWorkFlowTaskParameters() {
		// when
		List<WorkFlowTaskParameter> workFlowTaskParameters = restAPIWorkflowTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(workFlowTaskParameters);
		assertEquals(2, workFlowTaskParameters.size());
		assertEquals(URL_KEY, workFlowTaskParameters.get(0).getKey());
		assertEquals("The Url of the service (ie: https://httpbin.org/post",
				workFlowTaskParameters.get(0).getDescription());
		assertEquals(WorkFlowTaskParameterType.URL, workFlowTaskParameters.get(0).getType());
		assertEquals(PAYLOAD_KEY, workFlowTaskParameters.get(1).getKey());
		assertEquals("Json of what to provide for data. (ie: 'Hello!')",
				workFlowTaskParameters.get(1).getDescription());
		assertEquals(WorkFlowTaskParameterType.PASSWORD, workFlowTaskParameters.get(1).getType());
	}

	@Test
	public void testGetWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = restAPIWorkflowTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(2, workFlowTaskOutputs.size());
		assertEquals(WorkFlowTaskOutput.HTTP2XX, workFlowTaskOutputs.get(0));
		assertEquals(WorkFlowTaskOutput.OTHER, workFlowTaskOutputs.get(1));
	}

}