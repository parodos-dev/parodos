package com.redhat.parodos.workflow.execution.entity;

import com.redhat.parodos.workflows.common.enums.WorkFlowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "workflow_execution")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowExecutionEntity extends AbstractEntity {
    private String executedBy;

    private String executedFor;

    private WorkFlowStatus status;

    @Column(updatable = false)
    private Date startDate;

    private Date endDate;

    @Column(name="workflow_definition_id")
    private UUID workFlowDefinitionId;
}
