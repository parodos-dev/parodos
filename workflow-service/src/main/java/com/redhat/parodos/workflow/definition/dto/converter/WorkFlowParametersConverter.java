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
package com.redhat.parodos.workflow.definition.dto.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.parodos.workflow.parameter.WorkFlowParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

/**
 * Workflow parameters converter
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Converter
public class WorkFlowParametersConverter implements AttributeConverter<List<WorkFlowParameter>, String> {

	@Override
	public String convertToDatabaseColumn(List<WorkFlowParameter> parameters) {
		return WorkFlowDTOUtil.writeObjectValueAsString(parameters);
	}

	@Override
	public List<WorkFlowParameter> convertToEntityAttribute(String parameters) {
		return WorkFlowDTOUtil.readStringAsObject(parameters, new TypeReference<>() {
		}, List.of());
	}

}
