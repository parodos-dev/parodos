package com.redhat.parodos.workflow.definition.repository;

import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowWorkRepository extends JpaRepository<WorkFlowWorkDefinition, UUID> {

	List<WorkFlowWorkDefinition> findByWorkFlowDefinitionIdOrderByCreateDateAsc(UUID workFlowDefinitionId);

	List<WorkFlowWorkDefinition> findByWorkDefinitionId(UUID workDefinitionId);

}