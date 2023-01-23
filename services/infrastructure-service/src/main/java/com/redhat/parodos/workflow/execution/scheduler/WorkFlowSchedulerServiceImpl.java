package com.redhat.parodos.workflow.execution.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkFlowSchedulerServiceImpl implements WorkFlowSchedulerService {
    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> hm = new HashMap<>();

    public WorkFlowSchedulerServiceImpl(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void schedule(WorkFlow workFlow, WorkContext workContext, String cronExpression) {
        if (!hm.containsKey(workFlow.getName())) {
            log.info("Scheduling workflow: {} to be executed following cron expression: {}", workFlow.getName(), cronExpression);
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> workFlow.execute(workContext), new CronTrigger(cronExpression,
                    TimeZone.getTimeZone(TimeZone.getDefault().getID())));
            hm.put(workFlow.getName(), scheduledTask);
        }
        else {
            log.info("Workflow: {} is already scheduled!", hm.get(workFlow.getName()));
        }
    }

    @Override
    public boolean stop(WorkFlow workFlow) {
        if (hm.containsKey(workFlow.getName())) {
            log.info("Stopping workflow: {}", workFlow.getName());
            return hm.get(workFlow.getName()).cancel(false);
        }
        log.info("Workflow: {} has not been scheduled!", hm.get(workFlow.getName()));
        return false;
    }
}
