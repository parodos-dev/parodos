package com.redhat.parodos.examples.project.task;

import java.util.UUID;

import com.redhat.parodos.examples.project.client.ProjectRequester;
import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAccessRequestApprovalWorkFlowTaskTest {

	private static final String ACCESS_REQUEST_ID_PARAMETER_NAME = "ACCESS_REQUEST_ID";

	private static final String ACCESS_REQUEST_APPROVAL_USERNAMES_PARAMETER_NAME = "ACCESS_REQUEST_APPROVAL_USERNAMES";

	private static final String ACCESS_REQUEST_APPROVAL_USERNAMES_VALUE_TEST = "approval-username-test";

	@Mock
	private ProjectRequester projectRequester;

	@Mock
	private Notifier notifier;

	private WorkContext workContext;

	private ProjectAccessRequestApprovalWorkFlowTask projectAccessRequestApprovalWorkFlowTask;

	@Before
	public void setUp() {
		this.projectAccessRequestApprovalWorkFlowTask = Mockito
				.spy(new ProjectAccessRequestApprovalWorkFlowTask(projectRequester, notifier));
		this.projectAccessRequestApprovalWorkFlowTask.setBeanName("projectAccessRequestApprovalWorkFlowTask");
		workContext = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void executeSuccess() {
		doReturn(String.valueOf(UUID.randomUUID())).when(this.projectAccessRequestApprovalWorkFlowTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));
		doReturn(ACCESS_REQUEST_APPROVAL_USERNAMES_VALUE_TEST).when(this.projectAccessRequestApprovalWorkFlowTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_APPROVAL_USERNAMES_PARAMETER_NAME));
		doNothing().when(notifier).send(any());

		WorkReport workReport = projectAccessRequestApprovalWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	@SneakyThrows
	public void executeFail() {
		doThrow(MissingParameterException.class).when(this.projectAccessRequestApprovalWorkFlowTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));
		WorkReport workReport = projectAccessRequestApprovalWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
