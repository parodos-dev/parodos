/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * workflow execution entity
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
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
