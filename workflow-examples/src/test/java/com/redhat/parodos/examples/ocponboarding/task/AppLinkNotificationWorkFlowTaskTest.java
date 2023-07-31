package com.redhat.parodos.examples.ocponboarding.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.MockitoAnnotations.openMocks;

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

	@BeforeEach
	public void setUp() {
		openMocks(this);
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
