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
package com.redhat.parodos.workflow.execution.entity.converter;

import java.util.Map;
import java.util.Set;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.WorkContext;

/**
 * Converts WorkContext into values that can be persisted into a DB column
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@Converter(autoApply = true)
public class WorkContextConverter implements AttributeConverter<WorkContext, String> {

	@Override
	public String convertToDatabaseColumn(WorkContext workContext) {
		return WorkFlowDTOUtil.writeObjectValueAsString(workContext.getEntrySet());
	}

	@Override
	public WorkContext convertToEntityAttribute(String dbData) {
		WorkContext workContext = new WorkContext();
		WorkFlowDTOUtil.readStringAsObject(dbData, new TypeReference<Set<Map.Entry<String, Object>>>() {
		}, Set.of()).forEach(entry -> workContext.put(entry.getKey(), entry.getValue()));
		return workContext;
	}

}
