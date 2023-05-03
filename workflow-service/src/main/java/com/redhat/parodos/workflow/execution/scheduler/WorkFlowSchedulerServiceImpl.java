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

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;
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

	private final Map<UUID, Map<String, ScheduledFuture<?>>> hm = new ConcurrentHashMap<>();

	public WorkFlowSchedulerServiceImpl(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	@Override
	public void schedule(UUID projectId, WorkFlow workFlow, WorkContext workContext, String cronExpression) {
		hm.computeIfAbsent(projectId, key -> new HashMap<>());
		if (!hm.get(projectId).containsKey(workFlow.getName())) {
			log.info("Scheduling workflow: {} for project: {} to be executed following cron expression: {}",
					workFlow.getName(), projectId, cronExpression);
			ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> workFlow.execute(workContext),
					new CronTrigger(cronExpression, TimeZone.getTimeZone(TimeZone.getDefault().getID())));
			hm.get(projectId).put(workFlow.getName(), scheduledTask);
		}
		else {
			log.info("Workflow: {} is already scheduled for project: {}!", workFlow.getName(), projectId);
		}
	}

	@Override
	public boolean stop(UUID projectId, WorkFlow workFlow) {
		if (hm.containsKey(projectId) && hm.get(projectId).containsKey(workFlow.getName())) {
			log.info("Stopping workflow: {} for project: {}", workFlow.getName(), projectId);
			boolean stopped = hm.get(projectId).get(workFlow.getName()).cancel(false);
			if (stopped) {
				hm.get(projectId).remove(workFlow.getName());
				if (hm.get(projectId).isEmpty())
					hm.remove(projectId);
			}
			return stopped;
		}
		log.info("Workflow: {} has not been scheduled for project: {}!", workFlow.getName(), projectId);
		return false;
	}

}
