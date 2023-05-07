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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.redhat.parodos.common.AbstractEntity;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 * Workflow definition entity
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "workflow_definition")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@TypeDef(name = "json", typeClass = JsonType.class)
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
	@Type(type = "json")
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

	private String commitId;

}
