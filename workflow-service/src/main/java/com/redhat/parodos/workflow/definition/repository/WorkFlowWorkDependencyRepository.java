package com.redhat.parodos.workflow.definition.repository;

import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowWorkDependencyRepository extends JpaRepository<WorkFlowWorkDependency, UUID> {

	List<WorkFlowWorkDependency> findByWorkFlowDefinitionId(UUID workFlowDefinitionId);

}