package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.examples.base.BaseWorkFlowCheckerTaskTest;
import com.redhat.parodos.examples.vmonboarding.dto.AapGetJobResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

public class AnsibleCompletionWorkFlowCheckerTaskTest extends BaseWorkFlowCheckerTaskTest {

	private static final String SERVICE_URL_TEST = "service-url-test";

	private static final String USERNAME_TEST = "username-test";

	public static final String PASSWORD_TEST = "password-test";

	public static final String JOB_ID_PARAMETER_NAME = "JOB_ID";

	public static final String JOB_ID_PARAMETER_VALUE_TEST = "job-id-test";

	public static final String AAP_GET_JOB_RESPONSE_DTO_STATUS_SUCCESSFUL_TEST = "successful";

	public static final String AAP_GET_JOB_RESPONSE_DTO_STATUS_PENDING_TEST = "pending";

	private AnsibleCompletionWorkFlowCheckerTask ansibleCompletionWorkFlowCheckerTask;

	@Before
	public void setUp() {
		this.ansibleCompletionWorkFlowCheckerTask = spy(
				(AnsibleCompletionWorkFlowCheckerTask) getConcretePersonImplementation());
		try {
			doReturn(JOB_ID_PARAMETER_VALUE_TEST).when(this.ansibleCompletionWorkFlowCheckerTask)
					.getRequiredParameterValue(eq(JOB_ID_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseWorkFlowCheckerTask getConcretePersonImplementation() {
		return new AnsibleCompletionWorkFlowCheckerTask(SERVICE_URL_TEST, USERNAME_TEST, PASSWORD_TEST);
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.restExchange(any(), any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(AapGetJobResponseDTO.builder()
							.status(AAP_GET_JOB_RESPONSE_DTO_STATUS_SUCCESSFUL_TEST).build()));
			// when
			WorkReport workReport = ansibleCompletionWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
	}

	@Test
	public void executePending() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.restExchange(any(), any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(AapGetJobResponseDTO.builder()
							.status(AAP_GET_JOB_RESPONSE_DTO_STATUS_PENDING_TEST).build()));
			// when
			WorkReport workReport = ansibleCompletionWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	public void executeRejected() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.restExchange(any(), any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(AapGetJobResponseDTO.builder().status("").build()));
			// when
			WorkReport workReport = ansibleCompletionWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.REJECTED, workReport.getStatus());
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.restExchange(any(), any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.internalServerError().build());

			// when
			WorkReport workReport = ansibleCompletionWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

}
