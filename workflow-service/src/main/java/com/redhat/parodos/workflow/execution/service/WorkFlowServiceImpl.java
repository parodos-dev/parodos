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

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowStatus;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionEntity;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * workflow execution service implementation
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Slf4j
@Service
public class WorkFlowServiceImpl implements WorkFlowService {
    private final WorkFlowDelegate workFlowDelegate;
    private final WorkFlowRepository workFlowRepository;
    private final WorkFlowTaskRepository workFlowTaskRepository;

    public WorkFlowServiceImpl(WorkFlowDelegate workFlowDelegate,
                               WorkFlowDefinitionServiceImpl workFlowDefinitionService,
                               WorkFlowRepository workFlowRepository,
                               WorkFlowTaskRepository workFlowTaskRepository) {
        this.workFlowDelegate = workFlowDelegate;
        this.workFlowRepository = workFlowRepository;
        this.workFlowTaskRepository = workFlowTaskRepository;
    }

    @Override
    public WorkReport execute(WorkFlow workFlow, Map<String, Map<String, String>> workFlowTaskArguments) {
        log.info("execute workFlow: {}", workFlow);
        if (null != workFlow) {
            WorkContext workContext = workFlowDelegate.getWorkFlowContext(workFlowTaskArguments);
            return WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, workContext);
        } else {
            return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext());
        }
    }

    @Override
    public WorkFlowExecutionEntity getWorkFlowById(UUID workFlowExecutionId) {
        return this.workFlowRepository.findById(workFlowExecutionId).get();
    }

    @Override
    public WorkFlowExecutionEntity saveWorkFlow(String username, String reason, UUID workFlowDefinitionId, WorkFlowStatus workFlowStatus) {
        return workFlowRepository.save(WorkFlowExecutionEntity.builder()
                .workFlowDefinitionId(workFlowDefinitionId)
                .executedBy(username)
                .executedFor(reason)
                .status(workFlowStatus)
                .startDate(new Date())
                .build());
    }

    @Override
    public WorkFlowExecutionEntity updateWorkFlow(WorkFlowExecutionEntity workFlowExecutionEntity) {
        return workFlowRepository.save(workFlowExecutionEntity);
    }

    @Override
    public WorkFlowTaskExecutionEntity getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId) {
        List<WorkFlowTaskExecutionEntity> workFlowTaskExecutionEntityList = workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowExecutionId, workFlowTaskDefinitionId);
        return (workFlowTaskExecutionEntityList == null || workFlowTaskExecutionEntityList.isEmpty()) ? null : workFlowTaskExecutionEntityList.get(0);
    }

    @Override
    public WorkFlowTaskExecutionEntity saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId, UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus) {
        return workFlowTaskRepository.save(WorkFlowTaskExecutionEntity.builder()
                .workFlowExecutionId(workFlowExecutionId)
                .workFlowTaskDefinitionId(workFlowTaskDefinitionId)
                .arguments(arguments)
                .status(workFlowTaskStatus)
                .startDate(new Date())
                .build());
    }

    @Override
    public WorkFlowTaskExecutionEntity updateWorkFlowTask(WorkFlowTaskExecutionEntity workFlowTaskExecutionEntity) {
        return workFlowTaskRepository.save(workFlowTaskExecutionEntity);
    }
    
//    public Map<String, Map<String, String>> getWorkflowTaskArguments(
//			List<WorkFlowTaskExecutionRequestDTO> workFlowTaskExecutionRequestDTOList) {
//		Map<String, Map<String, String>> workFlowTaskArguments = new HashMap<>();
//        workFlowTaskExecutionRequestDTOList.forEach(arg -> {
//            Map<String, String> tasksValuesMap = new HashMap<>();
//            arg.getArguments().forEach(i -> tasksValuesMap.put(i.getKey(), i.getValue()));
//            workFlowTaskArguments.put(arg.getTaskName(), tasksValuesMap);
//        });
//		return workFlowTaskArguments;
//	}
}
