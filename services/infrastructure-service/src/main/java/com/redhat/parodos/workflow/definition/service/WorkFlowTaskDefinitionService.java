package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import com.redhat.parodos.workflows.definition.task.WorkFlowTaskDefinition;

import java.util.List;
import java.util.UUID;

public interface WorkFlowTaskDefinitionService {
    WorkFlowTaskDefinitionEntity getWorkFlowTaskDefinitionById(UUID id);
}
