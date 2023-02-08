package com.redhat.parodos.workflow.definition.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class WorkFlowTaskDefinitionDTOConverter implements Converter<List<WorkFlowTaskDefinition>, List<WorkFlowTaskDefinitionResponseDTO>> {
    @Override
    public List<WorkFlowTaskDefinitionResponseDTO> convert(MappingContext<List<WorkFlowTaskDefinition>, List<WorkFlowTaskDefinitionResponseDTO>> context) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<WorkFlowTaskDefinition> source = context.getSource();
        return source.stream()
                .map(workFlowTaskDefinition -> {
                    try {
                        return WorkFlowTaskDefinitionResponseDTO.builder()
                                .id(workFlowTaskDefinition.getId().toString())
                                .name(workFlowTaskDefinition.getName())
                                .outputs(objectMapper.readValue(workFlowTaskDefinition.getOutputs(), new TypeReference<>() {
                                }))
                                .parameters(objectMapper.readValue(workFlowTaskDefinition.getParameters(), new TypeReference<>() {
                                }))
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}