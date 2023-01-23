package com.redhat.parodos.workflow.definition.repository;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflows.common.enums.WorkFlowType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowTaskDefinitionRepository extends JpaRepository<WorkFlowTaskDefinitionEntity, UUID> {
    List<WorkFlowTaskDefinitionEntity> findByWorkFlowDefinitionEntity(WorkFlowDefinitionEntity workFlowDefinitionEntity);
    List<WorkFlowTaskDefinitionEntity> findByWorkFlowDefinitionEntityAndName(WorkFlowDefinitionEntity workFlowDefinitionEntity, String name);

    WorkFlowTaskDefinitionEntity findFirstByName(String name);
}
