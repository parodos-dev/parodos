package com.redhat.parodos.workflow.execution.scheduler;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkFlowSchedulerServiceImplTest {

	private static final String CRON_EXPRESSION = "* * * * * *";

	private static final String TEST = "test";

	private TaskScheduler taskScheduler;

	private WorkFlowSchedulerServiceImpl service;

	private Work work;

	private SequentialFlow workFlow;

	@BeforeEach
	public void eachInit() {
		this.taskScheduler = mock(TaskScheduler.class);
		this.service = new WorkFlowSchedulerServiceImpl(taskScheduler);

		this.work = mock(Work.class);
		this.workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();
	}

	@Test
	void scheduleWithValidData() {
		// given
		when(this.taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class))).thenReturn(null);

		// when
		this.service.schedule(UUID.randomUUID(), this.workFlow, new WorkContext(), CRON_EXPRESSION);

		// then
		verify(this.taskScheduler, times(1)).schedule(any(Runnable.class), any(CronTrigger.class));
	}

	@Test
	void workFlowIsNotScheduledTwice() {
		UUID projectId = UUID.randomUUID();
		// given
		when(this.taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class))).thenReturn(null);
		this.service.schedule(projectId, this.workFlow, new WorkContext(), CRON_EXPRESSION);

		// when
		this.service.schedule(projectId, this.workFlow, new WorkContext(), CRON_EXPRESSION);

		// then
		verify(this.taskScheduler, times(1)).schedule(any(Runnable.class), any(CronTrigger.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	void workFlowCanBeCancel() {
		UUID projectId = UUID.randomUUID();
		// given
		var mockScheduledFuture = mock(ScheduledFuture.class);
		when(mockScheduledFuture.cancel(false)).thenReturn(true);
		when(this.taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class))).thenReturn(mockScheduledFuture);
		this.service.schedule(projectId, this.workFlow, new WorkContext(), CRON_EXPRESSION);

		// when
		this.service.stop(projectId, this.workFlow);

		// then
		verify(mockScheduledFuture, times(1)).cancel(anyBoolean());
	}

	@Test
	void workFlowIsNotCalledIfNoPresent() {
		// given
		@SuppressWarnings("rawtypes")
		ScheduledFuture mockScheduledFuture = mock(ScheduledFuture.class);
		when(mockScheduledFuture.cancel(anyBoolean())).thenReturn(true);
		// when
		boolean res = this.service.stop(UUID.randomUUID(), this.workFlow);

		// then
		assertFalse(res);
		verify(mockScheduledFuture, times(0)).cancel(anyBoolean());
	}

}
