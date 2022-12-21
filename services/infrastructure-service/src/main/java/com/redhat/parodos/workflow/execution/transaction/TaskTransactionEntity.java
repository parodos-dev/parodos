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

import com.redhat.parodos.workflow.execution.AbstractEntity;
import com.redhat.parodos.workflow.execution.WorkFlowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;

/**
 * Entity for persisting Existing Tasks execution status.
 *
 * @author Richard Wang (Github: RichardW98)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "task_transaction")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTransactionEntity extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "workflow_transaction_id", nullable = false)
    private WorkFlowTransactionEntity workFlowTransaction;

    private String taskName;

    private WorkFlowStatus taskStatus;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime endAt;
}
