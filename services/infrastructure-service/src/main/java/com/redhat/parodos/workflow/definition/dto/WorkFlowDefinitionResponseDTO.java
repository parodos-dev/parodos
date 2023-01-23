package com.redhat.parodos.workflow.definition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class WorkFlowDefinitionResponseDTO {
    private String id;

    private String name;

    private String description;

    private String type;

    private String author;

    private Date createdDate;

    private Date modifiedDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EmbeddedTaskResponseDTO> tasks;
}