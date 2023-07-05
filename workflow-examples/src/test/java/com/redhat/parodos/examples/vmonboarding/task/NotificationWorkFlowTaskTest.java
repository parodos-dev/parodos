package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class NotificationWorkFlowTaskTest {

	@InjectMocks
	private NotificationWorkFlowTask notificationWorkFlowTask;

	@Mock
	private WorkContext workContext;

	@Mock
	private Notifier notifier;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void executeSuccess() {
		WorkReport workReport = notificationWorkFlowTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

}
