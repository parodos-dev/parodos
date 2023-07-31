package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowResponseDTO;
import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowResponseResult;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

public class ServiceNowTicketCreationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String SERVICE_URL_TEST = "service-url-test";

	private static final String USERNAME_TEST = "username-test";

	private static final String PASSWORD_TEST = "password-test";

	private static final String VM_NAME_PARAMETER_NAME = "hostname";

	private static final String VM_NAME_PARAMETER_VALUE_TEST = "hostname-test";

	private static final String VM_NAME_PARAMETER_DEFAULT_VALUE_TEST = "snowrhel";

	private static final String VM_TYPE_PARAMETER_NAME = "VM_TYPE";

	private static final String VM_TYPE_PARAMETER_VALUE_TEST = "vm-type-test";

	private static final String SERVICE_NOW_RESPONSE_RESULT_SYS_ID_TEST = "sys-id-test";

	private static final String SERVICE_NOW_RESPONSE_RESULT_NUMBER = "number-test";

	private static final String SERVICE_NOW_RESPONSE_RESULT_STATE_TEST = "state-test";

	private static final String SERVICE_NOW_RESPONSE_RESULT_DESCRIPTION_TEST = "description-test";

	private ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask;

	@BeforeEach
	public void setUp() {
		this.serviceNowTicketCreationWorkFlowTask = spy((ServiceNowTicketCreationWorkFlowTask) getTaskUnderTest());
		try {
			doReturn(VM_NAME_PARAMETER_VALUE_TEST).when(this.serviceNowTicketCreationWorkFlowTask)
					.getOptionalParameterValue(eq(VM_NAME_PARAMETER_NAME), eq(VM_NAME_PARAMETER_DEFAULT_VALUE_TEST));

			doReturn(VM_TYPE_PARAMETER_VALUE_TEST).when(this.serviceNowTicketCreationWorkFlowTask)
					.getRequiredParameterValue(eq(VM_TYPE_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getTaskUnderTest() {
		return new ServiceNowTicketCreationWorkFlowTask(SERVICE_URL_TEST, USERNAME_TEST, PASSWORD_TEST);
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.executePost(any(String.class), any(), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(ServiceNowResponseDTO.builder()
							.result(ServiceNowResponseResult.builder().sysId(SERVICE_NOW_RESPONSE_RESULT_SYS_ID_TEST)
									.number(SERVICE_NOW_RESPONSE_RESULT_NUMBER)
									.state(SERVICE_NOW_RESPONSE_RESULT_STATE_TEST)
									.description(SERVICE_NOW_RESPONSE_RESULT_DESCRIPTION_TEST).build())
							.build()));
			try (MockedStatic<WorkContextUtils> workContextUtilsMockedStatic = mockStatic(WorkContextUtils.class)) {
				workContextUtilsMockedStatic.when(() -> WorkContextUtils.addParameter(any(WorkContext.class),
						any(String.class), any(String.class))).thenAnswer((Answer<Void>) invocation -> null);
				// when
				WorkReport workReport = serviceNowTicketCreationWorkFlowTask.execute(workContext);

				// then
				assertEquals(WorkStatus.COMPLETED, workReport.getStatus());

			}
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.executePost(any(String.class), any(), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.internalServerError().build());

			// when
			WorkReport workReport = serviceNowTicketCreationWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

}
