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

import java.util.HashMap;
import java.util.Map;

/**
 * Workflow parameter type
 *
 * @author Annel Ketcha (Github: anludke)
 *
 */
public enum WorkFlowParameterType {

	PASSWORD, TEXT, EMAIL, DATE, NUMBER, URL;

	public Map<String, String> getAsJsonSchema() {
		Map<String, String> properties = new HashMap<>();
		switch (this) {
			case PASSWORD:
				properties.put("type", "string");
				properties.put("format", "password");
				break;
			case TEXT:
				properties.put("type", "string");
				properties.put("format", "text");
				break;
			case EMAIL:
				properties.put("type", "string");
				properties.put("format", "email");
				break;
			case NUMBER:
				properties.put("type", "number");
				break;
			case URL:
				properties.put("type", "string");
				properties.put("format", "url");
				break;
			case DATE:
				properties.put("type", "string");
				properties.put("format", "date");
				break;
			default:
				throw new IllegalArgumentException("Invalid parameter type");
		}
		return properties;
	}

}
