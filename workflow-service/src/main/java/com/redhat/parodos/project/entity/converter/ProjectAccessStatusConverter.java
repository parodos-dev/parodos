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
package com.redhat.parodos.project.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.redhat.parodos.project.enums.ProjectAccessStatus;

/**
 * Converts project access statys into values that can be persisted into a DB column
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Converter(autoApply = true)
public class ProjectAccessStatusConverter implements AttributeConverter<ProjectAccessStatus, String> {

	@Override
	public String convertToDatabaseColumn(ProjectAccessStatus status) {
		return status.name();
	}

	@Override
	public ProjectAccessStatus convertToEntityAttribute(String dbData) {
		return ProjectAccessStatus.valueOf(dbData);
	}

}
