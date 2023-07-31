package com.redhat.parodos.tasks.project;

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.tasks.project.dto.AccessResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class ProjectAccessRequestWorkFlowTaskTest {

	private static final String SERVICE_URL_TEST = "service-url-test";

	private static final String SERVICE_USERNAME_TEST = "service-username-test";

	private static final String SERVICE_PASSWORD_TEST = "service-password-test";

	private static final String USERNAME_PARAMETER_NAME = "USERNAME";

	private static final String USERNAME_VALUE_TEST = "username-test";

	private static final String ROLE_PARAMETER_NAME = "ROLE";

	private static final String ROLE_DEFAULT_VALUE = "DEVELOPER";

	private static final String ROLE_VALUE_TEST = "ADMIN";

	private static final String INVALID_ROLE_VALUE_TEST = "INVALID_ROLE";

	private static final String APPROVAL_USERNAME_TEST = "test";

	private static final String ESCALATION_USERNAME_TEST = "test";

	private WorkContext workContext;

	private ProjectAccessRequestWorkFlowTask projectAccessRequestWorkFlowTask;

	@BeforeEach
	public void setUp() {
		this.projectAccessRequestWorkFlowTask = spy(
				new ProjectAccessRequestWorkFlowTask(SERVICE_URL_TEST, SERVICE_USERNAME_TEST, SERVICE_PASSWORD_TEST));
		this.projectAccessRequestWorkFlowTask.setBeanName("projectAccessRequestWorkFlowTask");
		workContext = new WorkContext();
		WorkContextUtils.setProjectId(workContext, UUID.randomUUID());
	}

	@Test
	@SneakyThrows
	public void executeSuccess() {
		doReturn(USERNAME_VALUE_TEST).when(this.projectAccessRequestWorkFlowTask)
				.getRequiredParameterValue(eq(USERNAME_PARAMETER_NAME));
		doReturn(ROLE_VALUE_TEST).when(this.projectAccessRequestWorkFlowTask)
				.getOptionalParameterValue(eq(ROLE_PARAMETER_NAME), eq(ROLE_DEFAULT_VALUE), eq(false));

		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.executePost(any(String.class), any(), any(String.class), any(String.class), any()))
					.thenReturn(ResponseEntity.ok(AccessResponseDTO.builder().accessRequestId(UUID.randomUUID())
							.approvalSentTo(List.of(APPROVAL_USERNAME_TEST)).escalationSentTo(ESCALATION_USERNAME_TEST)
							.build()));
			doNothing().when(projectAccessRequestWorkFlowTask).addParameter(any(), any());
			WorkReport workReport = projectAccessRequestWorkFlowTask.execute(workContext);
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
	}

	@Test
	@SneakyThrows
	public void executeFailForInvalidRole() {
		doReturn(USERNAME_VALUE_TEST).when(this.projectAccessRequestWorkFlowTask)
				.getRequiredParameterValue(eq(USERNAME_PARAMETER_NAME));
		doReturn(INVALID_ROLE_VALUE_TEST).when(this.projectAccessRequestWorkFlowTask)
				.getOptionalParameterValue(eq(ROLE_PARAMETER_NAME), eq(ROLE_DEFAULT_VALUE), eq(false));
		WorkReport workReport = projectAccessRequestWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

	@Test
	@SneakyThrows
	public void executeFailForMissingRequiredParameter() {
		doThrow(MissingParameterException.class).when(this.projectAccessRequestWorkFlowTask)
				.getRequiredParameterValue(eq(USERNAME_PARAMETER_NAME));
		WorkReport workReport = projectAccessRequestWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
