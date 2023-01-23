package com.redhat.parodos.workflow.definition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.redhat.parodos.workflows.common.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.definition.task.WorkFlowTaskParameter;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WorkFlowTaskDefinitionResponseDTO {
    private String id;

    private String name;

    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<WorkFlowTaskParameter> parameters;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<WorkFlowTaskOutput> outputs;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EmbeddedTaskResponseDTO previousTask;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EmbeddedTaskResponseDTO nextTask;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID workFlowChecker;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID nextWorkFlow;
}
