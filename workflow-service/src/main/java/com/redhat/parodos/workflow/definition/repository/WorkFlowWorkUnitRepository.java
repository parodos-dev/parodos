package com.redhat.parodos.workflow.definition.repository;

import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowWorkUnitRepository extends JpaRepository<WorkFlowWorkUnit, UUID> {

	List<WorkFlowWorkUnit> findByWorkFlowDefinitionId(UUID workFlowDefinitionId);

	List<WorkFlowWorkDependency> findByWorkDefinitionId(UUID workDefinitionId);

}