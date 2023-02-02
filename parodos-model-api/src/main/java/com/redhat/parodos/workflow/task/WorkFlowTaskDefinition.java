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
package com.redhat.parodos.workflow.task;

import java.util.Date;
import java.util.List;

import com.redhat.parodos.workflow.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.WorkFlowDefinition;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A definition of a @see WorkFlowTask. Can be used for persistence or any other tool/interface that requires a human readable description
 * of what a WorkFlowTask does
 * 
 * @author Annel Ketcha (Github: anludke)
 *
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class WorkFlowTaskDefinition {
    private WorkFlowDefinition workFlowDefinition;
    private String name;
    private String description;
    private WorkFlowTaskType workFlowTaskType;
    private List<WorkFlowTaskParameter> parameters;
    private List<WorkFlowTaskOutput> outputs;
    private WorkFlowTaskDefinition previousTask;
    private WorkFlowTaskDefinition nextTask;
    private Date createDate;
    private Date modifyDate;
    private WorkFlowCheckerDefinition workFlowCheckerDefinition;
}