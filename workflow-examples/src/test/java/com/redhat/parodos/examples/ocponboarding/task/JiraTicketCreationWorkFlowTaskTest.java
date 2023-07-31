package com.redhat.parodos.examples.ocponboarding.task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.ocponboarding.task.dto.jira.CreateJiraTicketResponseDto;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.log.service.WorkFlowLogService;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

/**
 * Jira Ticket Creation Workflow Task execution test
 *
 * @author Annel Ketcha (GitHub: anludke)
 */
public class JiraTicketCreationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String JIRA_SERVICE_BASE_URL_TEST = "jira-service-base-url-test";

	private static final String JIRA_USERNAME_TEST = "jira-username-test";

	private static final String JIRA_PASSWORD_TEST = "jira-password-test";

	private static final String APPROVER_ID_TEST = "approver-id-test";

	private static final UUID PROJECT_ID_TEST = UUID.randomUUID();

	private static final String ISSUE_ID_TEST = "issue-ID-test";

	private static final String ISSUE_KEY_TEST = "issue-key-test";

	private static final String WEB_LINK_KEY = "web";

	private static final String WEB_LINK_VALUE = "web-link-test";

	private static final String NAMESPACE_PARAMETER_KEY = "NAMESPACE";

	private static final String NAMESPACE_PARAMETER_VALUE = "namespace-test";

	private JiraTicketCreationWorkFlowTask jiraTicketCreationWorkFlowTask;

	@BeforeEach
	public void setUp() {
		this.jiraTicketCreationWorkFlowTask = spy((JiraTicketCreationWorkFlowTask) getTaskUnderTest());
		WorkFlowLogService workFlowLogService = mock(WorkFlowLogService.class);
		doNothing().when(workFlowLogService).writeLog(any(), any(), any());
		ReflectionTestUtils.setField(jiraTicketCreationWorkFlowTask, "workFlowLogService", workFlowLogService);
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getTaskUnderTest() {
		return new JiraTicketCreationWorkFlowTask(JIRA_SERVICE_BASE_URL_TEST, JIRA_USERNAME_TEST, JIRA_PASSWORD_TEST,
				APPROVER_ID_TEST);
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = mock(WorkContext.class);

		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.executePost(any(String.class), any(), any(String.class), any(String.class), any()))
					.thenReturn(new ResponseEntity<>(CreateJiraTicketResponseDto.builder().issueId(ISSUE_ID_TEST)
							.issueKey(ISSUE_KEY_TEST).links(Map.of(WEB_LINK_KEY, WEB_LINK_VALUE)).build(),
							HttpStatus.OK));

			try (MockedStatic<WorkContextUtils> workContextUtilsMockedStatic = mockStatic(WorkContextUtils.class)) {
				workContextUtilsMockedStatic
						.when(() -> WorkContextUtils.getAllParameters(any(WorkContext.class), any(String.class)))
						.thenReturn(Map.of(NAMESPACE_PARAMETER_KEY, NAMESPACE_PARAMETER_VALUE));

				workContextUtilsMockedStatic.when(() -> WorkContextUtils.getProjectId(any(WorkContext.class)))
						.thenReturn(PROJECT_ID_TEST);
				jiraTicketCreationWorkFlowTask.preExecute(workContext);
				// when
				WorkReport workReport = jiraTicketCreationWorkFlowTask.execute(workContext);
				// then
				assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
			}
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(
					() -> RestUtils.executePost(any(String.class), any(), any(String.class), any(String.class), any()))
					.thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));
			try (MockedStatic<WorkContextUtils> workContextUtilsMockedStatic = mockStatic(WorkContextUtils.class)) {
				workContextUtilsMockedStatic.when(() -> WorkContextUtils.getMainExecutionId(any(WorkContext.class)))
						.thenReturn(UUID.randomUUID());
				jiraTicketCreationWorkFlowTask.preExecute(workContext);
				// when
				WorkReport workReport = jiraTicketCreationWorkFlowTask.execute(workContext);
				// then
				assertEquals(WorkStatus.FAILED, workReport.getStatus());
			}
		}
	}

	@Test
	public void testGetWorkFlowTaskParameters() {
		// when
		List<WorkParameter> workParameters = jiraTicketCreationWorkFlowTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(workParameters);
		assertEquals(0, workParameters.size());
	}

	@Test
	public void testGetWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = jiraTicketCreationWorkFlowTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(2, workFlowTaskOutputs.size());
		assertEquals(WorkFlowTaskOutput.HTTP2XX, workFlowTaskOutputs.get(0));
		assertEquals(WorkFlowTaskOutput.OTHER, workFlowTaskOutputs.get(1));
	}

}
