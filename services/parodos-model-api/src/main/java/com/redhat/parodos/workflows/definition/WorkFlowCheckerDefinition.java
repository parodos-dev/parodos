package com.redhat.parodos.workflows.definition;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class WorkFlowCheckerDefinition extends WorkFlowDefinition {
    private WorkFlowDefinition nextWorkFlowDefinition;
    private String cronExpression;
}
