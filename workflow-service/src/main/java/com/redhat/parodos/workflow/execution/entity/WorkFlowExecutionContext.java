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

import com.redhat.parodos.common.AbstractEntity;
import com.redhat.parodos.workflow.execution.entity.converter.WorkContextConverter;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * WorkFlow Context Entity
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "workflow_execution_context")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowExecutionContext extends AbstractEntity {

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "workflow_execution_id")
	private WorkFlowExecution masterWorkFlowExecution;

	@Convert(converter = WorkContextConverter.class)
	private WorkContext workContext;

}
