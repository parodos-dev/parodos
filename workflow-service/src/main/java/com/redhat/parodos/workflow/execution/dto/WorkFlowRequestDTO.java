package com.redhat.parodos.workflow.execution.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WorkFlowRequestDTO {
    private String name;
    private List<WorkFlowTaskRequestDTO> tasks;

    @Data
    @Builder
    public static class WorkFlowTaskRequestDTO {
        String name;
        List<ArgumentRequestDTO> args;

        @Data
        @Builder
        public static class ArgumentRequestDTO {
            String key;
            String value;
        }
    }
}
