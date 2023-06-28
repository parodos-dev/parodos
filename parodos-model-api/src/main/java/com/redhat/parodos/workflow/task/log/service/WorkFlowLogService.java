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
package com.redhat.parodos.workflow.task.log.service;

import java.util.UUID;

import com.redhat.parodos.workflow.task.log.dto.WorkFlowTaskLog;

/**
 * Service interface for recording workFlow tasks' log
 *
 * @author Richard Wang (Github: richardW98)
 */
public interface WorkFlowLogService {

	/**
	 * contract to get log for a task execution
	 * @param workFlowExecutionId main WorkFlow Execution's ID
	 * @param taskName task name for the log
	 * @return log of the task execution
	 */
	String getLog(UUID workFlowExecutionId, String taskName);

	/**
	 * contract to add log to a task execution
	 * @param workFlowExecutionId main WorkFlow Execution's ID
	 * @param taskName task name for the log
	 * @param log log dto object of the task execution
	 */
	void writeLog(UUID workFlowExecutionId, String taskName, WorkFlowTaskLog log);

}
