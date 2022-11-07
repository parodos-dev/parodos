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
package com.redhat.parodos.workflow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * An implementation of the WorkflowRegistry that loads all Bean definitions of type WorkFlow into a list
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Component
@Slf4j
public class BeanWorkflowRegistryImpl implements WorkFlowRegistry<String> {
    
	private final Map<String,WorkFlow> workFlows;

    public BeanWorkflowRegistryImpl(Map<String,WorkFlow> workFlows) {
        this.workFlows = workFlows;
        if (workFlows == null) {
            log.error("No workflows were registered. Initializing an empty collection of workflows so the application can start");
            workFlows = new HashMap<>();
        }
        log.info("Detected {} WorkFlows from the Bean Registry", workFlows.size());
    }
    
    @Override
    public Set<String> getRegisteredWorkFlowNames() {
        return (Set<String>) workFlows.keySet();
    }
    

	@Override
	public WorkFlow getWorkFlowById(String id) {
		return workFlows.get(id);
	}
	
	@Override
	public Collection<String> getRegisteredWorkFlowNamesByWorkType(String typeName) {
		return workFlows.keySet().stream().filter(k -> k.contains(typeName)).collect(Collectors.toList());
	}

	@Override
	public Map<String, WorkFlow> getAllRegisteredWorkFlows() {
		return workFlows;
	}
    
   
}
