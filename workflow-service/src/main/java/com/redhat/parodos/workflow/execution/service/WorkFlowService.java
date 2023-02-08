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

//import com.redhat.parodos.workflow.WorkFlowDefinition;
import com.redhat.parodos.workflow.WorkFlowStatus;
//import com.redhat.parodos.workflow.annotation.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionEntity;
import com.redhat.parodos.workflow.task.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import java.util.Map;
import java.util.UUID;

/**
 * workflow execution service
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
public interface WorkFlowService {
    WorkReport execute(String workFlowName, Map<String, Map<String, String>> workFlowTaskArguments);

    WorkFlowExecutionEntity getWorkFlowById(UUID workFlowExecutionId);

    WorkFlowExecutionEntity saveWorkFlow(String username, String reason, UUID workFlowDefinitionId, WorkFlowStatus workFlowStatus);

    WorkFlowExecutionEntity updateWorkFlow(WorkFlowExecutionEntity workFlowExecutionEntity);

    WorkFlowTaskExecutionEntity getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId);

    WorkFlowTaskExecutionEntity saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId, UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus);

    WorkFlowTaskExecutionEntity updateWorkFlowTask(WorkFlowTaskExecutionEntity workFlowTaskExecutionEntity);
}