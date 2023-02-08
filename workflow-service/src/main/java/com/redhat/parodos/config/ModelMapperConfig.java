package com.redhat.parodos.config;

import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowTaskDefinitionDTOConverter;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

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
                using(new WorkFlowTaskDefinitionDTOConverter()).map(source.getWorkFlowTaskDefinitions()).setTasks(null);
            }
        };
        modelMapper.addMappings(workFlowDefinitionResponseDTOMap);
    }
}
