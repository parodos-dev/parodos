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
package com.redhat.parodos.workflow.registry;

import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Contract for Registering and Obtaining Workflows. Implementations could use the classpath, Spring Bean registry, a Database or some other custom means.
 *
 * For the WorkFlowRegistry in-memory collection the Key: BeanId, Value: WorkFlow
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 * @author Richard Wang (Github: richardw98)
 */
public interface WorkFlowRegistry<T> {
    WorkFlow getWorkFlowByName(T workFlowName);
}
