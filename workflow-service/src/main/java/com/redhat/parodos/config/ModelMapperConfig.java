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
package com.redhat.parodos.config;

import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowTaskDefinitionListToTaskResponseDTOListConverter;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * configuration class for modelMapper
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Slf4j
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        addWorkFlowDefinitionResponseDTOMapping(modelMapper);
        return modelMapper;
    }

    private void addWorkFlowDefinitionResponseDTOMapping(ModelMapper modelMapper) {
        PropertyMap<WorkFlowDefinition, WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOMap = new PropertyMap<>() {
            protected void configure() {
                using(new WorkFlowTaskDefinitionListToTaskResponseDTOListConverter()).map(source.getWorkFlowTaskDefinitions()).setTasks(null);
            }
        };
        modelMapper.addMappings(workFlowDefinitionResponseDTOMap);
    }
}
