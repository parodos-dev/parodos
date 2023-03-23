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
package com.redhat.parodos.workflow.parameter;

/**
 * Workflow parameter type
 *
 * @author Annel Ketcha (Github: anludke)
 *
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowParameter {

	private String key;

	private WorkFlowParameterType type;

	private String description;

	private boolean optional;

	public Map<String, Object> getAsJsonSchema() {
		if (this.getType() == null) {
			return Map.of();
		}
		Map<String, Object> properties = this.getType().getAsJsonSchema();
		properties.put("required", !this.isOptional());
		properties.put("description", this.getDescription());
		return properties;
	}

}
