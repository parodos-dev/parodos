package com.redhat.parodos.workflow.execution.repository;

import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkFlowTaskExecutionRepository extends JpaRepository<WorkFlowTaskExecutionEntity, UUID> {
    List<WorkFlowTaskExecutionEntity> findByWorkFlowExecutionId(UUID workFlowExecutionId);
    List<WorkFlowTaskExecutionEntity> findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId);
}
