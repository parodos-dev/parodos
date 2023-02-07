package com.redhat.parodos.workflow.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
public class WorkFlowRequestDTO {
    private String name;
    private List<WorkFlowTaskRequestDTO> tasks;

    @Data
    @Builder
    public static class WorkFlowTaskRequestDTO {
        String taskName;
        List<ArgumentRequestDTO> arguments;

        @Data
        @Builder
        public static class ArgumentRequestDTO {
            String key;
            String value;
        }
    }

}
