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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Workflow parameter type
 *
 * @author Annel Ketcha (Github: anludke)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkParameter {

	// constants
	private static final String REQUIRED = "required";

	private static final String DESCRIPTION = "description";

	private static final String VALUE_PROVIDER_NAME = "valueProviderName";

	private static final String ENUM = "enum";

	private String key;

	private WorkParameterType type;

	private String description;

	private boolean optional;

	private List<String> selectOptions;

	private Map<String, Object> jsonSchemaOptions;

	private String valueProviderName;

	public Map<String, Object> getAsJsonSchema() {
		if (this.type == null) {
			return Map.of();
		}
		Map<String, Object> properties = type.getAsJsonSchema();
		properties.put(REQUIRED, !optional);
		properties.put(DESCRIPTION, description);
		if (valueProviderName != null && !valueProviderName.isEmpty())
			properties.put(VALUE_PROVIDER_NAME, valueProviderName);
		if (type.isSelect() && selectOptions != null && !selectOptions.isEmpty()) {
			properties.put(ENUM, selectOptions);
		}

		if (jsonSchemaOptions != null)
			properties.putAll(jsonSchemaOptions);

		return properties;
	}

}
