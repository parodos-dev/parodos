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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * entity
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Data
@Entity(name = "workflow_definition")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowDefinitionEntity extends AbstractEntity {
    private String name;

    private String type;

    private String author;

    @Column(updatable = false)
    private Date createDate;

    private Date modifyDate;

    @OneToMany(mappedBy = "workFlowDefinitionEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorkFlowTaskDefinitionEntity> workFlowTaskDefinitionEntities = new ArrayList<>();

    @OneToMany(mappedBy = "checkWorkFlow", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Set<WorkFlowCheckerDefinitionEntity> workFlowCheckerDefinitionEntities = new HashSet<>();

    @OneToMany(mappedBy = "nextWorkFlow", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Set<WorkFlowCheckerDefinitionEntity> workFlowCheckerDefinitionEntity = new HashSet<>();

    private String commitId;
}


