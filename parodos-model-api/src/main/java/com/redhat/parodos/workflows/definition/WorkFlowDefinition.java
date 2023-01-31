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
package com.redhat.parodos.workflows.definition;

import com.redhat.parodos.workflows.common.enums.WorkFlowType;
import com.redhat.parodos.workflows.definition.task.WorkFlowTaskDefinition;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * WorkFlow Definition
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
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
