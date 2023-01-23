package com.redhat.parodos.workflow.execution.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class WorkFlowTaskExecutionRequestDTO {
    String taskName;
    List<ArgumentRequestDTO> arguments;

    @Data
    @Builder
    public static class ArgumentRequestDTO {
        String key;
        String value;
    }
}
