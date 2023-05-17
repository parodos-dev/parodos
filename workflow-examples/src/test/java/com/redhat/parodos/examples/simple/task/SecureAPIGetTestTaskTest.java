package com.redhat.parodos.examples.simple.task;

import java.util.List;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.utils.CredUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.redhat.parodos.examples.simple.task.SecureAPIGetTestTask.PASSWORD;
import static com.redhat.parodos.examples.simple.task.SecureAPIGetTestTask.SECURED_URL;
import static com.redhat.parodos.examples.simple.task.SecureAPIGetTestTask.USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

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
			doReturn(testUrl).when(this.secureAPIGetTestTask).getRequiredParameterValue(eq(SECURED_URL));
			doReturn(testUsername).when(this.secureAPIGetTestTask).getRequiredParameterValue(eq(USERNAME));
			doReturn(testPassword).when(this.secureAPIGetTestTask).getRequiredParameterValue(eq(PASSWORD));
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
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.restExchange(eq(testUrl), eq(testUsername), eq(testPassword)))
					.thenReturn(new ResponseEntity<>("body", HttpStatus.OK));

			// when
			WorkReport workReport = secureAPIGetTestTask.execute(workContext);

			// then
			assertNull(secureAPIGetTestTask.getWorkFlowCheckers());
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
			verifyNoInteractions(workContext);
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = mock(WorkContext.class);

		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {

			restUtilsMockedStatic.when(() -> RestUtils.restExchange(eq(testUrl), eq(testUsername), eq(testPassword)))
					.thenReturn(new ResponseEntity<>("body", HttpStatus.BAD_REQUEST));
			// when
			WorkReport workReport = secureAPIGetTestTask.execute(workContext);

			// then
			assertNull(secureAPIGetTestTask.getWorkFlowCheckers());
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
			verifyNoInteractions(workContext);
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
		String base64Creds = CredUtils.getBase64Creds(testUsername, testPassword);
		assertNotNull(expectedBase64Creds, base64Creds);
		assertEquals(expectedBase64Creds, base64Creds);
	}

	@Test
	public void getWorkFlowTaskParameters() {
		List<WorkParameter> workParameters = this.secureAPIGetTestTask.getWorkFlowTaskParameters();
		// then
		assertNotNull(workParameters);
		assertEquals(3, workParameters.size());
		assertEquals(SECURED_URL, workParameters.get(0).getKey());
		assertEquals("The URL of the Secured API you wish to call", workParameters.get(0).getDescription());
		assertEquals(WorkParameterType.URL, workParameters.get(0).getType());

		assertEquals(USERNAME, workParameters.get(1).getKey());
		assertEquals("Please enter your username authentication", workParameters.get(1).getDescription());
		assertEquals(WorkParameterType.TEXT, workParameters.get(1).getType());

		assertEquals(PASSWORD, workParameters.get(2).getKey());
		assertEquals("Please enter your password for authentication (it will not be stored)",
				workParameters.get(2).getDescription());
		assertEquals(WorkParameterType.PASSWORD, workParameters.get(2).getType());
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
