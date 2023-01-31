package com.redhat.parodos.workflows.definition.task;

import com.redhat.parodos.workflows.definition.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import com.redhat.parodos.workflows.common.enums.WorkFlowTaskOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class WorkFlowTaskDefinition {
    private WorkFlowDefinition workFlowDefinition;
    private String name;
    private String description;
    private List<WorkFlowTaskParameter> parameters;
    private List<WorkFlowTaskOutput> outputs;
    private WorkFlowTaskDefinition previousTask;
    private WorkFlowTaskDefinition nextTask;
    private Date createDate;
    private Date modifyDate;
    private WorkFlowCheckerDefinition workFlowCheckerDefinition;
}