package com.redhat.parodos.workflow.execution.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;
import java.util.UUID;

import com.redhat.parodos.workflows.common.enums.WorkFlowTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "workflow_task_execution")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowTaskExecutionEntity extends AbstractEntity {
    @Column(name="workflow_execution_id")
    private UUID workFlowExecutionId;

    @Column(name="workflow_task_definition_id")
    private UUID workFlowTaskDefinitionId;

    private String arguments;

    private String results;

    private WorkFlowTaskStatus status;

    @Column(updatable = false)
    private Date startDate;

    private Date endDate;

    private Date lastUpdateDate;
}