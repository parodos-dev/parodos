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

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

import com.redhat.parodos.common.entity.AbstractEntity;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WorkFlow task execution entity
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "prds_workflow_task_execution")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowTaskExecution extends AbstractEntity {

	@Column(name = "workflow_execution_id")
	private UUID workFlowExecutionId;

	@Column(name = "workflow_task_definition_id")
	private UUID workFlowTaskDefinitionId;

	private String arguments;

	private String results;

	private WorkStatus status;

	private String message;

	private String alertMessage;

	@Column(updatable = false)
	private Date startDate;

	private Date endDate;

	private Date lastUpdateDate;

	@OneToOne(mappedBy = "workFlowTaskExecution", cascade = { CascadeType.ALL })
	private WorkFlowTaskExecutionLog workFlowTaskExecutionLog;

}
