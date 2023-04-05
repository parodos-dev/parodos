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

import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * workflow execution repository
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

public interface WorkFlowRepository extends JpaRepository<WorkFlowExecution, UUID> {

	WorkFlowExecution findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(UUID workFlowDefinitionId,
			WorkFlowExecution masterWorkFlowExecution);

	List<WorkFlowExecution> findByMasterWorkFlowExecution(WorkFlowExecution masterWorkFlowExecution);

	WorkFlowExecution findFirstByMasterWorkFlowExecutionAndWorkFlowDefinitionId(
			WorkFlowExecution masterWorkFlowExecution, UUID workFlowDefinitionId);

	@Query("SELECT w FROM workflow_execution w WHERE w.status IN :statuses")
	List<WorkFlowExecution> findByStatusIn(@Param("statuses") List<WorkFlowStatus> statuses);

	@Query("SELECT w FROM workflow_execution w WHERE w.masterWorkFlowExecution.id = :masterWorkflowId and EXISTS (SELECT f.type FROM workflow_definition f WHERE f.id = w.workFlowDefinitionId AND f.type = com.redhat.parodos.workflow.enums.WorkFlowType.CHECKER)")
	List<WorkFlowExecution> findCheckers(@Param("masterWorkflowId") UUID masterWorkflowId);

}
