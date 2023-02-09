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
package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.task.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.WorkReport;
import java.util.Map;
import java.util.UUID;

/**
 * Workflow execution service
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
public interface WorkFlowService {
	WorkReport execute(String projectId, String workFlowName, Map<String, Map<String, String>> workFlowTaskArguments);

	WorkFlowExecution getWorkFlowById(UUID workFlowExecutionId);

	WorkFlowExecution saveWorkFlow(UUID projectId, UUID workFlowDefinitionId, WorkFlowStatus workFlowStatus);

	WorkFlowExecution updateWorkFlow(WorkFlowExecution workFlowExecution);

	WorkFlowTaskExecution getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId);

	WorkFlowTaskExecution saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId, UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus);

	WorkFlowTaskExecution updateWorkFlowTask(WorkFlowTaskExecution workFlowTaskExecution);
}