package com.redhat.parodos.examples.ocponboarding.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.ocponboarding.task.dto.notification.NotificationRequest;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Notification Workflow Task execution test
 *
 * @author Annel Ketcha (GitHub: anludke)
 */
public class NotificationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String JIRA_TICKET_URL_WORKFLOW_TASK_PARAMETER_NAME_TEST = "ISSUE_LINK";

	private static final String JIRA_TICKET_URL_WORKFLOW_TASK_PARAMETER_VALUE_TEST = "jira-ticket-url-value-test";

	private static final String OCP_APP_LINK_WORKFLOW_TASK_PARAMETER_NAME_TEST = "APP_LINK";

	private static final String OCP_APP_LINK_WORKFLOW_TASK_PARAMETER_VALUE_TEST = "ocp-app-link-test";

	private static final String NOTIFICATION_SERVICE_URL = "notification-service-url";

	private NotificationWorkFlowTask notificationWorkFlowTask;

	@Before
	public void setUp() {
		this.notificationWorkFlowTask = spy((NotificationWorkFlowTask) getConcretePersonImplementation());
		try {
			doReturn(JIRA_TICKET_URL_WORKFLOW_TASK_PARAMETER_VALUE_TEST).when(this.notificationWorkFlowTask)
					.getRequiredParameterValue(Mockito.any(WorkContext.class),
							eq(JIRA_TICKET_URL_WORKFLOW_TASK_PARAMETER_NAME_TEST));

			doReturn(OCP_APP_LINK_WORKFLOW_TASK_PARAMETER_VALUE_TEST).when(this.notificationWorkFlowTask)
					.getRequiredParameterValue(Mockito.any(WorkContext.class),
							eq(OCP_APP_LINK_WORKFLOW_TASK_PARAMETER_NAME_TEST));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new NotificationWorkFlowTask(NOTIFICATION_SERVICE_URL);
	}

	@Test
	public void executeSuccess() {
		WorkContext workContext = Mockito.mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.getRequestWithHeaders(any(NotificationRequest.class),
					any(String.class), any(String.class)))
					.thenReturn(new HttpEntity<>(NotificationRequest.builder().build()));

			restUtilsMockedStatic.when(() -> RestUtils.executePost(any(String.class), any(HttpEntity.class)))
					.thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

			// when
			WorkReport workReport = notificationWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		}
	}

	@Test
	public void executeFail() {
		WorkContext workContext = Mockito.mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.getRequestWithHeaders(any(NotificationRequest.class),
					any(String.class), any(String.class)))
					.thenReturn(new HttpEntity<>(NotificationRequest.builder().build()));

			restUtilsMockedStatic.when(() -> RestUtils.executePost(any(String.class), any(HttpEntity.class)))
					.thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

			// when
			WorkReport workReport = notificationWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	public void testGetWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = notificationWorkFlowTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(2, workFlowTaskOutputs.size());
		assertEquals(WorkFlowTaskOutput.HTTP2XX, workFlowTaskOutputs.get(0));
		assertEquals(WorkFlowTaskOutput.OTHER, workFlowTaskOutputs.get(1));
	}

}
