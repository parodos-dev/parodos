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

import com.redhat.parodos.common.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow checker definition entity
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "workflow_checker_definition")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkFlowCheckerMappingDefinition extends AbstractEntity {

	@OneToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "workflow_checker_id")
	private WorkFlowDefinition checkWorkFlow;

	@OneToMany(mappedBy = "workFlowCheckerMappingDefinition", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@Builder.Default
	private List<WorkFlowTaskDefinition> tasks = new ArrayList<>();

	@Column(name = "cron_expression")
	private String cronExpression;

}
