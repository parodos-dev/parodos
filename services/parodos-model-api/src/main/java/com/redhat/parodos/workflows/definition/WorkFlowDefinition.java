package com.redhat.parodos.workflows.definition;

import com.redhat.parodos.workflows.common.enums.WorkFlowType;
import com.redhat.parodos.workflows.definition.task.WorkFlowTaskDefinition;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
public class WorkFlowDefinition {
    private String name;
    private String description;
    private WorkFlowType type;
    private String author;
    private Date createdDate;
    private Date modifiedDate;
    private List<WorkFlowTaskDefinition> tasks;
}
