package com.redhat.parodos.workflow.definition.dto;

import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class WorkFlowTaskDefinitionListToTaskResponseDTOListConverter implements Converter<List<WorkFlowTaskDefinition>, List<WorkFlowDefinitionResponseDTO.TaskResponseDTO>> {
    @Override
    public List<WorkFlowDefinitionResponseDTO.TaskResponseDTO> convert(MappingContext<List<WorkFlowTaskDefinition>, List<WorkFlowDefinitionResponseDTO.TaskResponseDTO>> context) {
        List<WorkFlowTaskDefinition> source = context.getSource();
        return source.stream().map(workFlowTaskDefinition -> WorkFlowDefinitionResponseDTO.TaskResponseDTO.builder()
                .id(workFlowTaskDefinition.getId())
                .name(workFlowTaskDefinition.getName())
                .build()).collect(Collectors.toList());
    }
}