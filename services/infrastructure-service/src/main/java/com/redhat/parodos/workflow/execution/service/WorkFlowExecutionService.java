package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionEntity;
import com.redhat.parodos.workflows.common.enums.WorkFlowStatus;
import com.redhat.parodos.workflows.common.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import java.util.Map;
import java.util.UUID;

public interface WorkFlowExecutionService {
    WorkReport execute(WorkFlowDefinition workFlowDefinition, WorkFlow workFlow, Map<String, Map<String, String>> workFlowTaskArguments);

    WorkFlowExecutionEntity getWorkFlowById(UUID workFlowExecutionId);

    WorkFlowExecutionEntity saveWorkFlow(String username, String reason, UUID workFlowDefinitionId, WorkFlowStatus workFlowStatus);

    WorkFlowExecutionEntity updateWorkFlow(WorkFlowExecutionEntity workFlowExecutionEntity);

    WorkFlowTaskExecutionEntity getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId);

    WorkFlowTaskExecutionEntity saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId, UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus);

    WorkFlowTaskExecutionEntity updateWorkFlowTask(WorkFlowTaskExecutionEntity workFlowTaskExecutionEntity);
}