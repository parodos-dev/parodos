package com.redhat.parodos.examples.vmonboarding.task;

import java.util.UUID;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.log.service.WorkFlowLogService;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class NotificationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String NOTIFICATION_SUBJECT_TEST = "notification-subject-test";

	@Mock
	private Notifier notifier;

	private WorkContext workContext;

	private NotificationWorkFlowTask notificationWorkFlowTask;

	@Before
	public void setUp() {
		this.notificationWorkFlowTask = spy((NotificationWorkFlowTask) getTaskUnderTest());
		this.notificationWorkFlowTask.setBeanName("test");
		workContext = new WorkContext();
		WorkFlowLogService workFlowLogService = mock(WorkFlowLogService.class);
		doNothing().when(workFlowLogService).writeLog(any(), any(), any());
		ReflectionTestUtils.setField(notificationWorkFlowTask, "workFlowLogService", workFlowLogService);
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getTaskUnderTest() {
		return new NotificationWorkFlowTask(notifier, NOTIFICATION_SUBJECT_TEST);
	}

	@Test
	public void executeSuccess() {
		WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());
		notificationWorkFlowTask.preExecute(workContext);
		doNothing().when(notifier).send(any(), any());
		WorkReport workReport = notificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

}
