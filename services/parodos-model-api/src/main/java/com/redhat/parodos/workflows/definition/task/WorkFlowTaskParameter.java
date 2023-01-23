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
package com.redhat.parodos.workflows.definition.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A description of what a Work unit requires to execute. This is useful to pass to users looking to execute the unit. It is also helpful for a UI layer that is rendering components to collect these values.
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowTaskParameter {
	private String key;
	private String description;
	private boolean optional;
	private WorkFlowTaskParameterType type;
}
