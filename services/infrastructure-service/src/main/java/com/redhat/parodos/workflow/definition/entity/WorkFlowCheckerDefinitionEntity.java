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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

/**
 * entity
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Entity(name = "workflow_checker_definition")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(callSuper = false)
@Data
public class WorkFlowCheckerDefinitionEntity {
    @EmbeddedId
    @AttributeOverride( name = "workFlowCheckerId", column = @Column(name = "workflow_checker_id"))
    @AttributeOverride( name = "taskId", column = @Column(name = "task_id"))
    private WorkFlowCheckerDefinitionPK id;

    @MapsId("workFlowCheckerId")
    @ManyToOne
    @JoinColumn(name = "workflow_checker_id")
    private WorkFlowDefinitionEntity checkWorkFlow;

    @MapsId("taskId")
    @OneToOne
    @JoinColumn(name = "task_id")
    private WorkFlowTaskDefinitionEntity task;

    @Column(name = "cron_expression")
    private String cronExpression;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "next_workflow_id")
    private WorkFlowDefinitionEntity nextWorkFlow;
}
