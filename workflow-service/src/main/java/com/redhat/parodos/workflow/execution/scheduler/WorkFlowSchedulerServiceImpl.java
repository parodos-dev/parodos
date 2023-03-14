/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.execution.scheduler;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

/**
 * workflow scheduler service implementation
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

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
			log.info("Scheduling workflow: {} to be executed following cron expression: {}", workFlow.getName(),
					cronExpression);
			ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> workFlow.execute(workContext),
					new CronTrigger(cronExpression, TimeZone.getTimeZone(TimeZone.getDefault().getID())));
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
			boolean stopped = hm.get(workFlow.getName()).cancel(false);
			if (stopped)
				hm.remove(workFlow.getName());
			return stopped;
		}
		log.info("Workflow: {} has not been scheduled!", hm.get(workFlow.getName()));
		return false;
	}

}
