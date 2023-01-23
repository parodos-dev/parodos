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

import com.redhat.parodos.workflow.registry.BeanWorkFlowRegistryImpl;
import com.redhat.parodos.workflows.common.context.WorkContextUtil;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Provides functionality that is common to any WorkFlow composition in Parodos
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Component
public class WorkFlowDelegate {
    private static final String WORK_UNITS = "workUnits";
    private final BeanWorkFlowRegistryImpl workFlowRegistry;

    public WorkFlowDelegate(BeanWorkFlowRegistryImpl workFlowRegistry) {
        this.workFlowRegistry = workFlowRegistry;
    }

    public WorkContext getWorkFlowContext(WorkFlowDefinition workFlowDefinition, Map<String, Map<String, String>> workFlowTaskParameterValues) {
        WorkContext workContext = new WorkContext();
        workFlowDefinition.getTasks().forEach(workFlowTaskDefinition -> {
            log.info("****** workflow task name: {}, parameter values: {}", workFlowTaskDefinition.getName(), workFlowTaskParameterValues.get(workFlowTaskDefinition.getName()));
            WorkContextUtil.write(workContext,
                    WorkContextUtil.ProcessType.WORKFLOW_TASK_DEFINITION,
                    workFlowTaskDefinition.getName(),
                    WorkContextUtil.Resource.NAME,
                    workFlowTaskDefinition.getName());
            WorkContextUtil.write(workContext,
                    WorkContextUtil.ProcessType.WORKFLOW_TASK_DEFINITION,
                    workFlowTaskDefinition.getName(),
                    WorkContextUtil.Resource.ID,
                    getWorkFlowTaskDefinitionId(workFlowDefinition.getName(),
                            workFlowTaskDefinition.getName()));
            WorkContextUtil.write(workContext,
                    WorkContextUtil.ProcessType.WORKFLOW_TASK_EXECUTION,
                    workFlowTaskDefinition.getName(),
                    WorkContextUtil.Resource.ARGUMENTS,
                    workFlowTaskParameterValues.get(workFlowTaskDefinition.getName()) == null ?
                            Map.of() : workFlowTaskParameterValues.get(workFlowTaskDefinition.getName()));
        });
        return workContext;
    }

    public WorkFlowDefinition getWorkFlowDefinitionById(UUID workFlowId) {
        return workFlowRegistry.getWorkFlowDefinitionById(workFlowId);
    }

    public WorkFlow getWorkFlowExecutionByName(String workFlowName) {
        return workFlowRegistry.getWorkFlowExecutionByName(workFlowName);
    }

    public UUID getWorkFlowTaskDefinitionId(String workFlowName, String workFlowTaskName) {
        return workFlowRegistry.getWorkFlowTaskDefinitionId(workFlowName, workFlowTaskName);
    }

//    /*
//     * WorkFlows keep their composing units private. To iterate through them and get the Parameters, need to change the accessibility of the field
//     */
//	@SuppressWarnings("unchecked")
//	private List<WorkFlowTaskParameter> getWorkFlowParameters(WorkFlowDefinition workFlow) {
//		List<WorkFlowTaskParameter> listOfParameters = new ArrayList<>();
//		Field field = ReflectionUtils.findField(workFlow.getClass(), WORK_UNITS, java.util.List.class);
//		ReflectionUtils.makeAccessible(field);
//		for (WorkFlowTask work : (List<WorkFlowTask>) ReflectionUtils.getField(field,workFlow)) {
//			listOfParameters.addAll(work.getWorkFlowTaskParameters());
//		}
//		return listOfParameters;
//	}
//
//	public WorkFlowDefinition getWorkFlowById(String id) {
//		return workFlowRegistry.getWorkFlowById(id);
//	}
//
//	public Collection<String> getWorkFlowIdsByWorkFlowType(String workFlowType) {
//		return workFlowRegistry.getWorkFlowIdsByWorkType(workFlowType);
//	}
//
//	public List<WorkFlowTaskParameter> getWorkFlowParametersForWorkFlow(String id) {
//		WorkFlowDefinition workFlow = getWorkFlowById(id);
//		if (workFlow != null) {
//			return getWorkFlowParameters(workFlow);
//		}
//		return new ArrayList<>();
//	}
//
//	public WorkContext getWorkContextWithParameters(WorkFlowExecuteRequestDTO workFlowRequestDto) {
//		WorkContext context = new WorkContext();
//		//a workflow might run with no parameters
//		if (workFlowRequestDto.getWorkFlowParameters() != null && workFlowRequestDto.getWorkFlowParameters().keySet() != null) {
//	        for (String key : workFlowRequestDto.getWorkFlowParameters().keySet()) {
//	        	context.put(key, workFlowRequestDto.getWorkFlowParameters().get(key).trim());
//	        }
//		}
//		return context;
//	}
//
//	public WorkContext getWorkContextWithParameters(WorkFlowTransactionDTO workFlowTransactionDTO) {
//		WorkContext context = new WorkContext();
//		for (String key : workFlowTransactionDTO.getWorkFlowCheckerArguments().keySet()) {
//        	context.put(key, workFlowTransactionDTO.getWorkFlowCheckerArguments().get(key).trim());
//        }
//		return context;
//	}
}
