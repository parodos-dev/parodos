package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import java.util.List;
import java.util.UUID;

public interface WorkFlowDefinitionService {
    List<WorkFlowDefinitionEntity> getWorkFlowDefinitions();

    List<WorkFlowTaskDefinitionEntity> getWorkFlowTaskDefinitionById(UUID workFlowDefinitionId);

    WorkFlowDefinitionEntity getWorkFlowDefinitionByName(String workFlowDefinitionName);
}
