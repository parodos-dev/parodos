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
package com.redhat.parodos.workflow.execution.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import com.redhat.parodos.common.entity.AbstractEntity;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Workflow execution entity
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "prds_workflow_execution")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowExecution extends AbstractEntity {

	private WorkStatus status;

	private String message;

	@Column(updatable = false)
	private Date startDate;

	private Date endDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workflow_definition_id")
	private WorkFlowDefinition workFlowDefinition;

	@Column(name = "project_id")
	private UUID projectId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "original_workflow_execution_id")
	private WorkFlowExecution originalWorkFlowExecution;

	private String arguments;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "main_workflow_execution_id")
	private WorkFlowExecution mainWorkFlowExecution;

	@OneToMany(mappedBy = "mainWorkFlowExecution", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private List<WorkFlowExecution> subWorkFlowExecution = new ArrayList<>();

	@OneToOne(mappedBy = "mainWorkFlowExecution", cascade = { CascadeType.ALL }, orphanRemoval = true)
	private WorkFlowExecutionContext workFlowExecutionContext;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

}
