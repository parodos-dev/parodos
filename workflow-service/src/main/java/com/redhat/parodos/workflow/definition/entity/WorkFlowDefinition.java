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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import com.redhat.parodos.common.entity.AbstractEntity;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Workflow definition entity
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "prds_workflow_definition")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowDefinition extends AbstractEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@Enumerated(EnumType.STRING)
	private WorkFlowType type;

	private String author;

	@Column(updatable = false)
	private Date createDate;

	private Date modifyDate;

	@Column(nullable = false)
	private String parameters;

	@Enumerated(EnumType.STRING)
	private WorkFlowProcessingType processingType;

	@Column(nullable = false)
	private Integer numberOfWorks;

	@Column(columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private WorkFlowPropertiesDefinition properties;

	@OneToMany(mappedBy = "workFlowDefinition", fetch = FetchType.EAGER,
			cascade = { CascadeType.PERSIST, CascadeType.DETACH })
	@Builder.Default
	private List<WorkFlowTaskDefinition> workFlowTaskDefinitions = new ArrayList<>();

	@OneToMany(mappedBy = "workFlowDefinition", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Builder.Default
	private List<WorkFlowWorkDefinition> workFlowWorkDefinitions = new ArrayList<>();

	@OneToOne(mappedBy = "checkWorkFlow", cascade = CascadeType.ALL)
	private WorkFlowCheckerMappingDefinition checkerWorkFlowDefinition;

	@OneToOne(cascade = { CascadeType.MERGE }, fetch = FetchType.EAGER)
	@JoinTable(name = "prds_workflow_fallback_mapping", joinColumns = @JoinColumn(name = "workflow_definition_id"),
			inverseJoinColumns = @JoinColumn(name = "workflow_fallback_definition_id"))
	private WorkFlowDefinition fallbackWorkFlowDefinition;

	private String commitId;

	@OneToMany(mappedBy = "workFlowDefinition", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Builder.Default
	private List<WorkFlowExecution> workFlowExecutions = new ArrayList<>();

}
