package com.redhat.parodos.workflow.execution.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nimbusds.jose.shaded.json.annotate.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WorkFlowExecutionResponseDTO<T> {
    private String workFlowExecutionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T output;
}
