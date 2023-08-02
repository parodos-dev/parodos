package com.redhat.parodos.examples.project.checker;

import java.util.UUID;

import com.redhat.parodos.examples.project.client.ProjectRequester;
import com.redhat.parodos.sdk.model.AccessStatusResponseDTO;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAccessRequestApprovalWorkFlowCheckerTaskTest {

	private static final String ACCESS_REQUEST_ID_PARAMETER_NAME = "ACCESS_REQUEST_ID";

	private static final long SLA_TEST = 100L;

	@Mock
	private ProjectRequester projectRequester;

	@Mock
	private WorkFlow workFlow;

	private WorkContext workContext;

	private ProjectAccessRequestApprovalWorkFlowCheckerTask projectAccessRequestApprovalWorkFlowCheckerTask;

	@Before
	public void setUp() {
		this.projectAccessRequestApprovalWorkFlowCheckerTask = spy(
				new ProjectAccessRequestApprovalWorkFlowCheckerTask(workFlow, SLA_TEST, projectRequester));
		this.projectAccessRequestApprovalWorkFlowCheckerTask
				.setBeanName("projectAccessRequestApprovalWorkFlowCheckerTask");
		workContext = new WorkContext();
		WorkContextUtils.setProjectId(workContext, UUID.randomUUID());
	}

	@Test
	@SneakyThrows
	public void checkWorkFlowStatusCompleted() {
		UUID accessRequestId = UUID.randomUUID();
		doReturn(String.valueOf(accessRequestId)).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));

		AccessStatusResponseDTO accessStatusResponseDTO = new AccessStatusResponseDTO();
		accessStatusResponseDTO.setAccessRequestId(accessRequestId);
		accessStatusResponseDTO.setStatus(AccessStatusResponseDTO.StatusEnum.APPROVED);
		when(projectRequester.getAccessStatus(any())).thenReturn(accessStatusResponseDTO);

		WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	@SneakyThrows
	public void checkWorkFlowStatusRejected() {
		UUID accessRequestId = UUID.randomUUID();
		doReturn(String.valueOf(accessRequestId)).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));

		AccessStatusResponseDTO accessStatusResponseDTO = new AccessStatusResponseDTO();
		accessStatusResponseDTO.setAccessRequestId(accessRequestId);
		accessStatusResponseDTO.setStatus(AccessStatusResponseDTO.StatusEnum.REJECTED);
		when(projectRequester.getAccessStatus(any())).thenReturn(accessStatusResponseDTO);

		WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
		assertEquals(WorkStatus.REJECTED, workReport.getStatus());
	}

	@Test
	@SneakyThrows
	public void checkWorkFlowStatusPending() {
		UUID accessRequestId = UUID.randomUUID();
		doReturn(String.valueOf(accessRequestId)).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));

		AccessStatusResponseDTO accessStatusResponseDTO = new AccessStatusResponseDTO();
		accessStatusResponseDTO.setAccessRequestId(accessRequestId);
		accessStatusResponseDTO.setStatus(AccessStatusResponseDTO.StatusEnum.PENDING);
		when(projectRequester.getAccessStatus(any())).thenReturn(accessStatusResponseDTO);

		WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

	@Test
	@SneakyThrows
	public void executeFailForMissingRequiredParameter() {
		doThrow(MissingParameterException.class).when(this.projectAccessRequestApprovalWorkFlowCheckerTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));
		WorkReport workReport = projectAccessRequestApprovalWorkFlowCheckerTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
