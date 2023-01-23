package com.redhat.parodos.workflow.execution.repository;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkFlowExecutionRepository extends JpaRepository<WorkFlowExecutionEntity, UUID> {
}
