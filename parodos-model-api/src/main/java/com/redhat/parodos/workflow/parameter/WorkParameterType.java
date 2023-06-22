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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow parameter type
 *
 * @author Annel Ketcha (Github: anludke)
 *
 */
public enum WorkParameterType {

	PASSWORD, TEXT, EMAIL, DATE, NUMBER, URI, SELECT, MULTI_SELECT;

	private EnumSet<WorkParameterType> selectedTypes() {
		return EnumSet.of(SELECT, MULTI_SELECT);
	}

	public boolean isSelect() {
		return selectedTypes().contains(this);
	}

	public Map<String, Object> getAsJsonSchema() {
		Map<String, Object> properties = new HashMap<>();
		switch (this) {
			case PASSWORD -> {
				properties.put("type", "string");
				properties.put("format", "password");
			}
			case TEXT -> {
				properties.put("type", "string");
				properties.put("format", "text");
			}
			case EMAIL -> {
				properties.put("type", "string");
				properties.put("format", "email");
			}
			case NUMBER -> properties.put("type", "number");
			case URI -> {
				properties.put("type", "string");
				properties.put("format", "uri");
			}
			case DATE -> {
				properties.put("type", "string");
				properties.put("format", "date");
			}
			case SELECT -> {
				properties.put("type", "string");
				properties.put("format", "select");
			}
			case MULTI_SELECT -> {
				properties.put("type", "string");
				properties.put("format", "multi-select");
			}
			default -> throw new IllegalArgumentException("Invalid parameter type");
		}
		return properties;
	}

}
