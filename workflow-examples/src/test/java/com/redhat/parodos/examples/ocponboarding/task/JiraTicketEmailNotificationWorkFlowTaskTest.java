package com.redhat.parodos.examples.ocponboarding.task;

import java.util.List;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Jira Ticket Email Notification Workflow Task execution test
 *
 * @author Annel Ketcha (GitHub: anludke)
 */
public class JiraTicketEmailNotificationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String MAIL_SERVICE_URL_TEST = "mail-service-url-test";

	private static final String MAIL_SERVICE_SITE_NAME_TEST = "mail-service-site-name-test";

	private static final String ISSUE_LINK_PARAMETER_NAME = "ISSUE_LINK";

	public static final String JIRA_TICKET_URL_TEST = "jira-ticket-url-test";

	private JiraTicketEmailNotificationWorkFlowTask jiraTicketEmailNotificationWorkFlowTask;

	@Before
	public void setUp() {
		this.jiraTicketEmailNotificationWorkFlowTask = spy(
				(JiraTicketEmailNotificationWorkFlowTask) getConcretePersonImplementation());

		try {
			doReturn(JIRA_TICKET_URL_TEST).when(this.jiraTicketEmailNotificationWorkFlowTask)
					.getRequiredParameterValue(Mockito.any(WorkContext.class), eq(ISSUE_LINK_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new JiraTicketEmailNotificationWorkFlowTask(MAIL_SERVICE_URL_TEST, MAIL_SERVICE_SITE_NAME_TEST);
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.executePost(eq(MAIL_SERVICE_URL_TEST), Mockito.any(HttpEntity.class)))
					.thenReturn(ResponseEntity.ok("Mail Sent"));

			// when
			WorkReport workReport = jiraTicketEmailNotificationWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.executePost(eq(MAIL_SERVICE_URL_TEST), Mockito.any(HttpEntity.class)))
					.thenReturn(ResponseEntity.internalServerError().build());

			// when
			WorkReport workReport = jiraTicketEmailNotificationWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	public void testGetWorkFlowTaskParameters() {
		// when
		List<WorkParameter> workParameters = jiraTicketEmailNotificationWorkFlowTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(workParameters);
		assertEquals(0, workParameters.size());
	}

	@Test
	public void testGetWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = jiraTicketEmailNotificationWorkFlowTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(2, workFlowTaskOutputs.size());
		assertEquals(WorkFlowTaskOutput.EXCEPTION, workFlowTaskOutputs.get(0));
		assertEquals(WorkFlowTaskOutput.OTHER, workFlowTaskOutputs.get(1));
	}

}