package com.redhat.parodos.workflow.execution.scheduler;

import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;

class WorkFlowSchedulerServiceImplTest {

	private static final String CRON_EXPRESSION = "* * * * * *";

	private static final String TEST = "test";

	private TaskScheduler taskScheduler;

	private WorkFlowSchedulerServiceImpl service;

	private Work work;

	private SequentialFlow workFlow;

	@BeforeEach
	public void eachInit() {
		this.taskScheduler = Mockito.mock(TaskScheduler.class);
		this.service = new WorkFlowSchedulerServiceImpl(taskScheduler);

		this.work = Mockito.mock(Work.class);
		this.workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();
	}

	@Test
	void scheduleWithValidData() {
		// given
		Mockito.when(this.taskScheduler.schedule(Mockito.any(Runnable.class), Mockito.any(CronTrigger.class)))
				.thenReturn(null);
		// when
		this.service.schedule(this.workFlow, new WorkContext(), CRON_EXPRESSION);
		// then
		Mockito.verify(this.taskScheduler, Mockito.times(1)).schedule(Mockito.any(Runnable.class),
				Mockito.any(CronTrigger.class));
	}

	@Test
	void workFlowIsNotScheduledTwice() {
		// given
		Mockito.when(this.taskScheduler.schedule(Mockito.any(Runnable.class), Mockito.any(CronTrigger.class)))
				.thenReturn(null);
		this.service.schedule(this.workFlow, new WorkContext(), CRON_EXPRESSION);

		// when
		this.service.schedule(this.workFlow, new WorkContext(), CRON_EXPRESSION);

		// then
		Mockito.verify(this.taskScheduler, Mockito.times(1)).schedule(Mockito.any(Runnable.class),
				Mockito.any(CronTrigger.class));
	}

	
	@SuppressWarnings("unchecked")
	@Test
	void workFlowCanBeCancel() {
		// given
		var mockScheduledFuture = Mockito.mock(ScheduledFuture.class);
		Mockito.when(mockScheduledFuture.cancel(false)).thenReturn(true);
		Mockito.when(this.taskScheduler.schedule(Mockito.any(Runnable.class), Mockito.any(CronTrigger.class)))
				.thenReturn(mockScheduledFuture);
		this.service.schedule(this.workFlow, new WorkContext(), CRON_EXPRESSION);

		// when
		this.service.stop(this.workFlow);

		// then
		Mockito.verify(mockScheduledFuture, Mockito.times(1)).cancel(Mockito.anyBoolean());
	}

	
	@Test
	void workFlowIsNotCalledIfNoPresent() {
		// given
		@SuppressWarnings("rawtypes")
		ScheduledFuture mockScheduledFuture = Mockito.mock(ScheduledFuture.class);
		Mockito.when(mockScheduledFuture.cancel(Mockito.anyBoolean())).thenReturn(true);
		// when
		boolean res = this.service.stop(this.workFlow);

		// then
		assertFalse(res);
		Mockito.verify(mockScheduledFuture, Mockito.times(0)).cancel(Mockito.anyBoolean());
	}

}
