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

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import com.redhat.parodos.common.entity.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WorkFlow Context Entity
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "prds_workflow_task_execution_log")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowTaskExecutionLog extends AbstractEntity {

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "workflow_task_execution_id")
	private WorkFlowTaskExecution workFlowTaskExecution;

	private String log;

	public void addLog(String nextLog) {
		log = log + "\n" + nextLog;
	}

}
