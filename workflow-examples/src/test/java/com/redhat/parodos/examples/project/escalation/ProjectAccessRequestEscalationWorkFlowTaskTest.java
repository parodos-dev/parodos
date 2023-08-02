package com.redhat.parodos.examples.project.escalation;

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
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAccessRequestEscalationWorkFlowTaskTest {

	private static final String ACCESS_REQUEST_ID_PARAMETER_NAME = "ACCESS_REQUEST_ID";

	private static final String ACCESS_REQUEST_ESCALATION_USERNAME_PARAMETER_NAME = "ACCESS_REQUEST_ESCALATION_USERNAME";

	private static final String ACCESS_REQUEST_ESCALATION_USERNAME_VALUE_TEST = "escalation-username-test";

	@Mock
	private ProjectRequester projectRequester;

	@Mock
	private Notifier notifier;

	private WorkContext workContext;

	private ProjectAccessRequestEscalationWorkFlowTask projectAccessRequestEscalationWorkFlowTask;

	@Before
	public void setUp() {
		this.projectAccessRequestEscalationWorkFlowTask = spy(
				new ProjectAccessRequestEscalationWorkFlowTask(projectRequester, notifier));
		this.projectAccessRequestEscalationWorkFlowTask.setBeanName("projectAccessRequestEscalationWorkFlowTask");
		workContext = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void executeSuccess() {
		doReturn(String.valueOf(UUID.randomUUID())).when(this.projectAccessRequestEscalationWorkFlowTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));
		doReturn(ACCESS_REQUEST_ESCALATION_USERNAME_VALUE_TEST).when(this.projectAccessRequestEscalationWorkFlowTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ESCALATION_USERNAME_PARAMETER_NAME));
		doNothing().when(notifier).send(any());

		WorkReport workReport = projectAccessRequestEscalationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	@SneakyThrows
	public void executeFail() {
		doThrow(MissingParameterException.class).when(this.projectAccessRequestEscalationWorkFlowTask)
				.getRequiredParameterValue(eq(ACCESS_REQUEST_ID_PARAMETER_NAME));
		WorkReport workReport = projectAccessRequestEscalationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
