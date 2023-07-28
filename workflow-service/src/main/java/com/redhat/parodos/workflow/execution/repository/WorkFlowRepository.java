/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.execution.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflows.work.WorkStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * workflow execution repository
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

public interface WorkFlowRepository extends JpaRepository<WorkFlowExecution, UUID> {

	WorkFlowExecution findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(UUID workFlowDefinitionId,
			WorkFlowExecution mainWorkFlowExecution);

	List<WorkFlowExecution> findByMainWorkFlowExecution(WorkFlowExecution mainWorkFlowExecution);

	List<WorkFlowExecution> findByMainWorkFlowExecutionId(UUID mainWorkFlowExecutionId);

	List<WorkFlowExecution> findAllByProjectId(UUID projectId);

	WorkFlowExecution findFirstByMainWorkFlowExecutionIdAndWorkFlowDefinitionId(UUID mainWorkFlowExecutionId,
			UUID workFlowDefinitionId);

	@Query("SELECT w FROM prds_workflow_execution w WHERE w.status IN :statuses AND w.mainWorkFlowExecution IS NULL")
	List<WorkFlowExecution> findByStatusInAndIsMain(@Param("statuses") List<WorkStatus> statuses);

	@Query("SELECT w FROM prds_workflow_execution w WHERE w.status = com.redhat.parodos.workflows.work.WorkStatus.FAILED and w.mainWorkFlowExecution.id = :mainWorkflowId and EXISTS (SELECT f.type FROM prds_workflow_definition f WHERE f.id = w.workFlowDefinition.id AND f.type = com.redhat.parodos.workflow.enums.WorkFlowType.CHECKER)")
	List<WorkFlowExecution> findRunningCheckersById(@Param("mainWorkflowId") UUID mainWorkflowId);

	@Query("SELECT w FROM prds_workflow_execution w WHERE w.mainWorkFlowExecution.id = :mainWorkflowId and EXISTS (SELECT f.type FROM prds_workflow_definition f WHERE f.id = w.workFlowDefinition.id AND f.type = com.redhat.parodos.workflow.enums.WorkFlowType.CHECKER)")
	List<WorkFlowExecution> findCheckers(@Param("mainWorkflowId") UUID mainWorkflowId);

	@Query("SELECT COUNT(*) FROM prds_workflow_execution o JOIN prds_workflow_execution restarted ON o.id = restarted.originalWorkFlowExecution.id WHERE o.id = :originalWorkflowId AND restarted.workFlowDefinition.id = o.workFlowDefinition.id")
	Integer countRestartedWorkflow(@Param("originalWorkflowId") UUID originalWorkflowId);

	@Query("SELECT fallback FROM prds_workflow_execution o JOIN prds_workflow_execution fallback ON o.id = fallback.originalWorkFlowExecution.id WHERE o.id = :originalWorkflowId AND fallback.workFlowDefinition.id <> o.workFlowDefinition.id")
	Optional<WorkFlowExecution> findFallbackWorkFlowExecution(@Param("originalWorkflowId") UUID originalWorkflowId);

	WorkFlowExecution findFirstByProjectIdAndMainWorkFlowExecutionIsNullOrderByStartDateDesc(UUID projectId);

}
