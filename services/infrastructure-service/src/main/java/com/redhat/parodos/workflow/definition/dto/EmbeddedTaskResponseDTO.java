package com.redhat.parodos.workflow.definition.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmbeddedTaskResponseDTO {
    private String id;
    private String name;
}