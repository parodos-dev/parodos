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

import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An implementation of the WorkflowRegistry that loads all Bean definitions of type WorkFlow into a list
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Component
public class BeanWorkFlowRegistryImpl implements WorkFlowRegistry<String> {
    // Spring will populate this through classpath scanning when the Context starts up
    private static String underscoreChar = "_";
    private final Map<String, WorkFlow> workFlows;

    private final Map<String, WorkFlowTask> workFlowTaskMap;



    public BeanWorkFlowRegistryImpl(Map<String, WorkFlow> workFlows,
                                    Map<String, WorkFlowTask> workFlowTaskMap) {
        this.workFlows = workFlows;
        this.workFlowTaskMap = workFlowTaskMap;

        if (workFlows == null) {
            log.error("No workflows were registered. Initializing an empty collection of workflows so the application can start");
            workFlows = new HashMap<>();
        }
        log.info(">> Detected {} WorkFlow from the Bean Registry", workFlows.size());

    }

    @Override
    public WorkFlow getWorkFlowByName(String workFlowName) {
        return workFlows.get(workFlowName);
    }
}
