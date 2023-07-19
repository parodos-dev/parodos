package com.redhat.parodos.examples.ocponboarding.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
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
public class JiraTicketUpdateNotificationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String ISSUE_LINK_PARAMETER_NAME = "ISSUE_LINK";

	private static final String JIRA_TICKET_URL_TEST = "jira-ticket-url-test";

	@Mock
	private Notifier notifier;

	@Mock
	private WorkContext workContext;

	private JiraTicketUpdateNotificationWorkFlowTask jiraTicketUpdateNotificationWorkFlowTask;

	@Before
	public void setUp() {
		this.jiraTicketUpdateNotificationWorkFlowTask = Mockito
				.spy((JiraTicketUpdateNotificationWorkFlowTask) getTaskUnderTest());
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getTaskUnderTest() {
		return new JiraTicketUpdateNotificationWorkFlowTask(notifier);
	}

	@Test
	public void executeSuccess() {
		try {
			doReturn(JIRA_TICKET_URL_TEST).when(this.jiraTicketUpdateNotificationWorkFlowTask)
					.getRequiredParameterValue(eq(ISSUE_LINK_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
		doNothing().when(notifier).send(any(), any());
		WorkReport workReport = jiraTicketUpdateNotificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	public void executeFail() throws MissingParameterException {
		doThrow(MissingParameterException.class).when(this.jiraTicketUpdateNotificationWorkFlowTask)
				.getRequiredParameterValue(eq(ISSUE_LINK_PARAMETER_NAME));
		WorkReport workReport = jiraTicketUpdateNotificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}