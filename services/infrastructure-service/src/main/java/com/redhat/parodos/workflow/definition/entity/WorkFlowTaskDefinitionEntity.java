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
package com.redhat.parodos.workflow.definition.entity;

import com.redhat.parodos.workflow.execution.entity.AbstractEntity;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * task entity
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "workflow_task_definition")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowTaskDefinitionEntity extends AbstractEntity {
    private String name;

    private String description;

    private String parameters;

    private String outputs;

    private UUID previousTask;

    private UUID nextTask;

    @Column(updatable = false)
    private Date createDate;

    private Date modifyDate;

    @ManyToOne
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkFlowDefinitionEntity workFlowDefinitionEntity;

    @OneToOne(mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    private WorkFlowCheckerDefinitionEntity workFlowCheckerDefinitionEntity;
}
