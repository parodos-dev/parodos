package com.redhat.parodos.examples.project.task;

import java.util.UUID;

import com.redhat.parodos.examples.project.client.ProjectRequester;
import com.redhat.parodos.sdk.model.AccessResponseDTO;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAccessRequestWorkFlowTaskTest {

	private static final String USERNAME_PARAMETER_NAME = "USERNAME";

	private static final String USERNAME_VALUE_TEST = "username-test";

	private static final String ROLE_PARAMETER_NAME = "ROLE";

	private static final String ROLE_DEFAULT_VALUE = "DEVELOPER";

	private static final String ROLE_VALUE_TEST = "ADMIN";

	private static final String INVALID_ROLE_VALUE_TEST = "INVALID_ROLE";

	private static final String APPROVAL_USERNAME_TEST = "test";

	private static final String ESCALATION_USERNAME_TEST = "test";

	@Mock
	private ProjectRequester projectRequester;

	private WorkContext workContext;

	private ProjectAccessRequestWorkFlowTask projectAccessRequestWorkFlowTask;

	@Before
	public void setUp() {
		this.projectAccessRequestWorkFlowTask = spy(new ProjectAccessRequestWorkFlowTask(projectRequester));
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

		AccessResponseDTO accessResponseDTO = new AccessResponseDTO();
		accessResponseDTO.setAccessRequestId(UUID.randomUUID());
		accessResponseDTO.addApprovalSentToItem(APPROVAL_USERNAME_TEST);
		accessResponseDTO.setEscalationSentTo(ESCALATION_USERNAME_TEST);

		when(projectRequester.createAccess(any(), any())).thenReturn(accessResponseDTO);

		doNothing().when(projectAccessRequestWorkFlowTask).addParameter(any(), any());

		WorkReport workReport = projectAccessRequestWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
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

}
