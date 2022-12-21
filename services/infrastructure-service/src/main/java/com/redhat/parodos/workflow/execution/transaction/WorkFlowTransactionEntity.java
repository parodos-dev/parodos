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
package com.redhat.parodos.workflow.execution.transaction;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.redhat.parodos.workflow.execution.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity for persisting Existing Infrastructure. Existing Infrastructure refers to InfrastructureOption(s) that have already been executed by a Parodos workflow
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "workflow_transaction")
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowTransactionEntity extends AbstractEntity {

	private String projectName;

    private String details;

    @Column(name = "workflow_id")
    private String workFlowId;

    @Column(name = "workflow_type")
    private String workFlowType;

    private String status;

    private String executedBy;
    
    @Column(name = "workflow_checker_id")
    private String workFlowCheckerId;
    
    @Convert(converter = WorkFlowArgumentConverter.class)
    @Column(name = "workflow_checker_arguments")
    private Map<String, String> workFlowCheckerArguments;

    @Column(name = "next_workflow_id")
    private String nextWorkFlowId;

    @Convert(converter = WorkFlowArgumentConverter.class)
    @Column(name = "next_workflow_arguments")
    private Map<String, String> nextWorkFlowArguments;
    
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "workFlowTransaction", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<TaskTransactionEntity> taskTransactions = Collections.synchronizedList(new ArrayList<>());
}
