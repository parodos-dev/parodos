package com.redhat.parodos.workflow.definition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowCheckerDTO {
    private String nextWorkFlowName;
    private String cronExpression;
}
