package com.redhat.parodos.examples.vmonboarding.task;

import java.util.UUID;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.task.log.service.WorkFlowLogService;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class NotificationWorkFlowTaskTest {

	@Mock
	Notifier notifier;

	@InjectMocks
	private NotificationWorkFlowTask notificationWorkFlowTask;

	private WorkContext workContext;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		workContext = new WorkContext();
		WorkFlowLogService workFlowLogService = mock(WorkFlowLogService.class);
		doNothing().when(workFlowLogService).writeLog(any(), any(), any());
		ReflectionTestUtils.setField(notificationWorkFlowTask, "workFlowLogService", workFlowLogService);
	}

	@Test
	public void executeSuccess() {
		WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());
		notificationWorkFlowTask.setBeanName("test");
		notificationWorkFlowTask.preExecute(workContext);
		WorkReport workReport = notificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

}
