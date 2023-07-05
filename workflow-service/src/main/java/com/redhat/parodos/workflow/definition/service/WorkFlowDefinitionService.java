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
package com.redhat.parodos.workflow.definition.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.WorkFlowPropertiesMetadata;

/**
 * workflow definition service
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
public interface WorkFlowDefinitionService {

	WorkFlowDefinitionResponseDTO save(String workFlowName, WorkFlowType workFlowType,
			WorkFlowPropertiesMetadata workFlowPropertiesMetadata, List<WorkParameter> workParameters, List<Work> works,
			WorkFlowProcessingType workFlowProcessingType, String fallbackWorkFlowName);

	WorkFlowDefinitionResponseDTO getWorkFlowDefinitionByName(String name);

	WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(UUID id);

	List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions();

	void saveWorkFlowChecker(String workFlowTaskName, String workFlowCheckerName,
			WorkFlowCheckerDTO workFlowCheckerDTO);

	Map<String, Object> getWorkParametersByWorkName(String workName);

	WorkFlowDefinition getParentWorkFlowByWorkName(String workName);

	void cleanAllDefinitionMappings();

}
