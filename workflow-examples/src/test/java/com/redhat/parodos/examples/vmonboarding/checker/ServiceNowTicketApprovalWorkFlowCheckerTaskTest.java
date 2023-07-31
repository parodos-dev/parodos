package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.examples.base.BaseWorkFlowCheckerTaskTest;
import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowResponseDTO;
import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowResponseResult;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

public class ServiceNowTicketApprovalWorkFlowCheckerTaskTest extends BaseWorkFlowCheckerTaskTest {

	private static final String SERVICE_URL_TEST = "service-url-test";

	private static final String USERNAME_TEST = "username-test";

	private static final String PASSWORD_TEST = "password-test";

	private static final String INCIDENT_ID_PARAMETER_NAME = "INCIDENT_ID";

	private static final String INCIDENT_ID_PARAMETER_VALUE_TEST = "incident-id-test";

	private static final String SERVICE_NOW_RESPONSE_RESULT_STATE_SUCCESS_TEST = "success-test";

	private static final String SERVICE_NOW_RESPONSE_RESULT_STATE_NON_APPROVED_TEST = "1";

	private static final String SERVICE_NOW_RESPONSE_RESULT_STATE_REJECTED_TEST = "8";

	private ServiceNowTicketApprovalWorkFlowCheckerTask serviceNowTicketApprovalWorkFlowCheckerTask;

	@BeforeEach
	public void setUp() {
		this.serviceNowTicketApprovalWorkFlowCheckerTask = spy(
				(ServiceNowTicketApprovalWorkFlowCheckerTask) getTaskUnderTest());
		try {
			doReturn(INCIDENT_ID_PARAMETER_VALUE_TEST).when(this.serviceNowTicketApprovalWorkFlowCheckerTask)
					.getRequiredParameterValue(eq(INCIDENT_ID_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseWorkFlowCheckerTask getTaskUnderTest() {
		return new ServiceNowTicketApprovalWorkFlowCheckerTask(SERVICE_URL_TEST, USERNAME_TEST, PASSWORD_TEST);
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.restExchange(any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(
							ResponseEntity
									.ok(ServiceNowResponseDTO.builder()
											.result(ServiceNowResponseResult.builder()
													.state(SERVICE_NOW_RESPONSE_RESULT_STATE_SUCCESS_TEST).build())
											.build()));
			// when
			WorkReport workReport = serviceNowTicketApprovalWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
	}

	@Test
	public void executeRejected() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.restExchange(any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(
							ResponseEntity
									.ok(ServiceNowResponseDTO.builder()
											.result(ServiceNowResponseResult.builder()
													.state(SERVICE_NOW_RESPONSE_RESULT_STATE_REJECTED_TEST).build())
											.build()));
			// when
			WorkReport workReport = serviceNowTicketApprovalWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.REJECTED, workReport.getStatus());
		}
	}

	@Test
	public void executeNonApproved() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.restExchange(any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(
							ResponseEntity.ok(ServiceNowResponseDTO.builder()
									.result(ServiceNowResponseResult.builder()
											.state(SERVICE_NOW_RESPONSE_RESULT_STATE_NON_APPROVED_TEST).build())
									.build()));
			// when
			WorkReport workReport = serviceNowTicketApprovalWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.restExchange(any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.internalServerError().build());

			// when
			WorkReport workReport = serviceNowTicketApprovalWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

}
