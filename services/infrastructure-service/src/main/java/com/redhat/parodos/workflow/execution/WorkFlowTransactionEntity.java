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
package com.redhat.parodos.workflow.execution;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Entity(name = "work_flow_transaction")
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowTransactionEntity extends AbstractEntity {

    private String projectName;

    private String executedBy;

    private String details;

    private String status;

    private String workflowType;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

}
