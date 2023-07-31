package com.redhat.parodos.examples.ocponboarding.escalation;

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
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.openMocks;

public class JiraTicketApprovalEscalationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String ISSUE_LINK_PARAMETER_NAME = "ISSUE_LINK";

	private static final String JIRA_TICKET_URL_TEST = "jira-ticket-url-test";

	private static final String ESCALATION_USER_ID_TEST = "escalation-user-id-test";

	@Mock
	private Notifier notifier;

	@Mock
	private WorkContext workContext;

	private JiraTicketApprovalEscalationWorkFlowTask jiraTicketApprovalEscalationWorkFlowTask;

	@BeforeEach
	public void setUp() {
		openMocks(this);
		this.jiraTicketApprovalEscalationWorkFlowTask = Mockito
				.spy((JiraTicketApprovalEscalationWorkFlowTask) getTaskUnderTest());
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getTaskUnderTest() {
		return new JiraTicketApprovalEscalationWorkFlowTask(notifier, ESCALATION_USER_ID_TEST);
	}

	@Test
	public void executeSuccess() {
		try {
			doReturn(JIRA_TICKET_URL_TEST).when(this.jiraTicketApprovalEscalationWorkFlowTask)
					.getRequiredParameterValue(eq(ISSUE_LINK_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
		doNothing().when(notifier).send(any());
		WorkReport workReport = jiraTicketApprovalEscalationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

	@Test
	public void executeFail() throws MissingParameterException {
		doThrow(MissingParameterException.class).when(this.jiraTicketApprovalEscalationWorkFlowTask)
				.getRequiredParameterValue(eq(ISSUE_LINK_PARAMETER_NAME));
		WorkReport workReport = jiraTicketApprovalEscalationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.FAILED, workReport.getStatus());
	}

}
