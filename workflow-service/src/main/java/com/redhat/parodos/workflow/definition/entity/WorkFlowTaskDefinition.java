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
package com.redhat.parodos.workflow.definition.entity;

import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;

import com.redhat.parodos.common.entity.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Workflow task definition entity
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "prds_workflow_task_definition")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowTaskDefinition extends AbstractEntity {

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String parameters;

	@Column(nullable = false)
	private String outputs;

	@Column(updatable = false)
	private Date createDate;

	private Date modifyDate;

	@ManyToOne(optional = false)
	@JoinColumn(name = "workflow_definition_id")
	private WorkFlowDefinition workFlowDefinition;

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "prds_workflow_task_checker_definition_mapping",
			joinColumns = @JoinColumn(name = "workflow_task_definition_id"),
			inverseJoinColumns = @JoinColumn(name = "workflow_checker_definition_id"))
	private WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition;

	private String commitId;

	public void setWorkFlowCheckerMappingDefinition(WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition) {
		this.workFlowCheckerMappingDefinition = workFlowCheckerMappingDefinition;
		workFlowCheckerMappingDefinition.getTasks().add(this);
	}

}
