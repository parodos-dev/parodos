package com.redhat.parodos.examples.vmonboarding.checker;

import java.util.UUID;

import com.redhat.parodos.examples.base.BaseWorkFlowCheckerTaskTest;
import com.redhat.parodos.tasks.ansible.AapGetJobResponseArtifacts;
import com.redhat.parodos.tasks.ansible.AapGetJobResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
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

public class AnsibleVMCreationWorkFlowCheckerTaskTest extends BaseWorkFlowCheckerTaskTest {

	private static final String SERVICE_URL_TEST = "service-url-test";

	private static final String USERNAME_TEST = "username-test";

	private static final String PASSWORD_TEST = "password-test";

	private static final String JOB_ID_PARAMETER_NAME = "JOB_ID";

	private static final String VM_TYPE_PARAMETER_NAME = "VM_TYPE";

	private static final String JOB_ID_PARAMETER_VALUE_TEST = "job-id-test";

	private static final String VM_TYPE_PARAMETER_VALUE_TEST = "windows";

	private static final String AAP_GET_JOB_RESPONSE_DTO_STATUS_SUCCESSFUL_TEST = "successful";

	private static final String AAP_GET_JOB_RESPONSE_DTO_STATUS_PENDING_TEST = "pending";

	private AnsibleVMCreationWorkFlowCheckerTask ansibleVMCreationWorkFlowCheckerTask;

	@BeforeEach
	public void setUp() {
		this.ansibleVMCreationWorkFlowCheckerTask = spy((AnsibleVMCreationWorkFlowCheckerTask) getTaskUnderTest());
		try {
			doReturn(JOB_ID_PARAMETER_VALUE_TEST).when(this.ansibleVMCreationWorkFlowCheckerTask)
					.getRequiredParameterValue(eq(JOB_ID_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseWorkFlowCheckerTask getTaskUnderTest() {
		return new AnsibleVMCreationWorkFlowCheckerTask(SERVICE_URL_TEST, USERNAME_TEST, PASSWORD_TEST);
	}

	@Test
	public void executeSuccess() {
		// given
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			AapGetJobResponseDTO aapGetJobResponseDTO = AapGetJobResponseDTO.builder()
					.status(AAP_GET_JOB_RESPONSE_DTO_STATUS_SUCCESSFUL_TEST)
					.extraVars("{\"rhel_admin_user\": \"test\"}")
					.artifacts(AapGetJobResponseArtifacts.builder().azureVmPublicIp("test-ip").build()).build();
			restUtilsMockedStatic.when(
					() -> RestUtils.restExchange(any(), any(String.class), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(aapGetJobResponseDTO));
			// when
			WorkContext workContext = new WorkContext();
			WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());
			ansibleVMCreationWorkFlowCheckerTask.setBeanName("test");
			ansibleVMCreationWorkFlowCheckerTask.preExecute(workContext);
			WorkReport workReport = ansibleVMCreationWorkFlowCheckerTask.checkWorkFlowStatus(workContext);
			// then
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());

			aapGetJobResponseDTO.setExtraVars("\"win_admin_user\": \"test\", \"win_admin_password\": \"test\"");

			doReturn(VM_TYPE_PARAMETER_VALUE_TEST).when(this.ansibleVMCreationWorkFlowCheckerTask)
					.getRequiredParameterValue(eq(VM_TYPE_PARAMETER_NAME));
			workReport = ansibleVMCreationWorkFlowCheckerTask.checkWorkFlowStatus(workContext);
			// then
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
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
			WorkReport workReport = ansibleVMCreationWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

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
			WorkReport workReport = ansibleVMCreationWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

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
			WorkReport workReport = ansibleVMCreationWorkFlowCheckerTask.checkWorkFlowStatus(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

}
