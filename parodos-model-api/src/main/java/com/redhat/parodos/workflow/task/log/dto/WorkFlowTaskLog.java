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
package com.redhat.parodos.workflow.task.log.dto;

import java.time.Instant;

import com.redhat.parodos.workflow.enums.WorkFlowLogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dto to represent workFlow tasks' log objects
 *
 * @author Richard Wang (Github: richardW98)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowTaskLog {

	private String logText;

	private WorkFlowLogLevel workFlowLoglevel = WorkFlowLogLevel.INFO;

	@Override
	public String toString() {
		return Instant.now().toString() + " " + workFlowLoglevel.getCode() + workFlowLoglevel.name() + "\u001B[39m "
				+ logText;
	}

}
