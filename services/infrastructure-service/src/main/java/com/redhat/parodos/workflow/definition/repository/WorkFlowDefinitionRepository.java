package com.redhat.parodos.workflow.definition.repository;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowDefinitionRepository extends JpaRepository<WorkFlowDefinitionEntity, UUID> {
    List<WorkFlowDefinitionEntity> findByName(String name);

    WorkFlowDefinitionEntity findFirstByName(String name);
}