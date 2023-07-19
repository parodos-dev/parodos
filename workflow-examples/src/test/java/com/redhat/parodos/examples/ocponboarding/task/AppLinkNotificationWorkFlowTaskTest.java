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
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class AppLinkNotificationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String APP_LINK_PARAMETER_NAME = "APP_LINK";

	private static final String APP_LINK_VALUE_TEST = "app-link-test";

	private static final String JIRA_TICKET_PARAMETER_NAME = "ISSUE_LINK";

	private static final String JIRA_TICKET_VALUE_TEST = "issue-link-test";

	@Mock
	private Notifier notifier;

	@Mock
	private WorkContext workContext;

	private AppLinkNotificationWorkFlowTask appLinkNotificationWorkFlowTask;

	@Before
	public void setUp() {
		this.appLinkNotificationWorkFlowTask = spy((AppLinkNotificationWorkFlowTask) getTaskUnderTest());
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getTaskUnderTest() {
		return new AppLinkNotificationWorkFlowTask(notifier);
	}

	@Test
	public void executeSuccess() {
		try {
			doReturn(JIRA_TICKET_VALUE_TEST).when(this.appLinkNotificationWorkFlowTask)
					.getRequiredParameterValue(eq(JIRA_TICKET_PARAMETER_NAME));
			doReturn(APP_LINK_VALUE_TEST).when(this.appLinkNotificationWorkFlowTask)
					.getRequiredParameterValue(eq(APP_LINK_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
		doNothing().when(notifier).send(any(), any());
		WorkReport workReport = appLinkNotificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	public void executeFailForNoJiraIssue() throws MissingParameterException {
		doThrow(MissingParameterException.class).when(this.appLinkNotificationWorkFlowTask)
				.getRequiredParameterValue(eq(JIRA_TICKET_PARAMETER_NAME));
		WorkReport workReport = appLinkNotificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

	@Test
	public void executeFailForNoAppLink() throws MissingParameterException {
		doReturn(JIRA_TICKET_VALUE_TEST).when(this.appLinkNotificationWorkFlowTask)
				.getRequiredParameterValue(eq(JIRA_TICKET_PARAMETER_NAME));
		doThrow(MissingParameterException.class).when(this.appLinkNotificationWorkFlowTask)
				.getRequiredParameterValue(eq(APP_LINK_PARAMETER_NAME));
		WorkReport workReport = appLinkNotificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
