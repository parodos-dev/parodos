package com.redhat.parodos.examples.simple.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.simple.task.SecureAPIGetTestTask;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.redhat.parodos.examples.simple.task.SecureAPIGetTestTask.PASSWORD;
import static com.redhat.parodos.examples.simple.task.SecureAPIGetTestTask.SECURED_URL;
import static com.redhat.parodos.examples.simple.task.SecureAPIGetTestTask.USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Secure API Get Test Task execution test
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */

public class SecureAPIGetTestTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private SecureAPIGetTestTask secureAPIGetTestTask;

	private final String testUrl = "test_url";

	private final String testUsername = "test_username";

	private final String testPassword = "test_password";

	private final String expectedBase64Creds = "dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk";

	@Before
	public void setUp() {
		this.secureAPIGetTestTask = spy((SecureAPIGetTestTask) getConcretePersonImplementation());
		try {
			doReturn(testUrl).when(this.secureAPIGetTestTask).getParameterValue(Mockito.any(WorkContext.class),
					eq(SECURED_URL));
			doReturn(testUsername).when(this.secureAPIGetTestTask).getParameterValue(Mockito.any(WorkContext.class),
					eq(USERNAME));
			doReturn(testPassword).when(this.secureAPIGetTestTask).getParameterValue(Mockito.any(WorkContext.class),
					eq(PASSWORD));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new SecureAPIGetTestTask();
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.restExchange(eq(testUrl), eq(testUsername), eq(testPassword)))
					.thenReturn(new ResponseEntity<>("body", HttpStatus.OK));

			// when
			WorkReport workReport = secureAPIGetTestTask.execute(workContext);

			// then
			assertNull(secureAPIGetTestTask.getWorkFlowChecker());
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
			Mockito.verifyNoInteractions(workContext);
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);

		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {

			restUtilsMockedStatic.when(() -> RestUtils.restExchange(eq(testUrl), eq(testUsername), eq(testPassword)))
					.thenReturn(new ResponseEntity<>("body", HttpStatus.BAD_REQUEST));
			// when
			WorkReport workReport = secureAPIGetTestTask.execute(workContext);

			// then
			assertNull(secureAPIGetTestTask.getWorkFlowChecker());
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
			Mockito.verifyNoInteractions(workContext);
		}
	}

	@Test
	public void getRequestWithHeaders() {

		// when
		HttpEntity<String> requestWithHeaders = secureAPIGetTestTask.getRequestWithHeaders(testUsername, testPassword);

		// then
		assertNotNull(requestWithHeaders.getHeaders());
		assertNull(requestWithHeaders.getBody());
		assertNotNull(requestWithHeaders.getHeaders().get("Authorization"));
		assertEquals(1, requestWithHeaders.getHeaders().get("Authorization").size());
		assertEquals("Basic " + expectedBase64Creds, requestWithHeaders.getHeaders().get("Authorization").get(0));
	}

	@Test
	public void getBase64Creds() {
		String base64Creds = RestUtils.getBase64Creds(testUsername, testPassword);
		assertNotNull(expectedBase64Creds, base64Creds);
		assertEquals(expectedBase64Creds, base64Creds);
	}

	@Test
	public void getWorkFlowTaskParameters() {
		List<WorkFlowTaskParameter> workFlowTaskParameters = this.secureAPIGetTestTask.getWorkFlowTaskParameters();
		// then
		assertNotNull(workFlowTaskParameters);
		assertEquals(3, workFlowTaskParameters.size());
		assertEquals(SECURED_URL, workFlowTaskParameters.get(0).getKey());
		assertEquals("The URL of the Secured API you wish to call", workFlowTaskParameters.get(0).getDescription());
		assertEquals(WorkFlowTaskParameterType.URL, workFlowTaskParameters.get(0).getType());

		assertEquals(USERNAME, workFlowTaskParameters.get(1).getKey());
		assertEquals("Please enter your username authentication", workFlowTaskParameters.get(1).getDescription());
		assertEquals(WorkFlowTaskParameterType.TEXT, workFlowTaskParameters.get(1).getType());

		assertEquals(PASSWORD, workFlowTaskParameters.get(2).getKey());
		assertEquals("Please enter your password for authentication (it will not be stored)",
				workFlowTaskParameters.get(2).getDescription());
		assertEquals(WorkFlowTaskParameterType.PASSWORD, workFlowTaskParameters.get(2).getType());
	}

	@Test
	public void getWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = secureAPIGetTestTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(2, workFlowTaskOutputs.size());
		assertEquals(WorkFlowTaskOutput.HTTP2XX, workFlowTaskOutputs.get(0));
		assertEquals(WorkFlowTaskOutput.OTHER, workFlowTaskOutputs.get(1));
	}

}