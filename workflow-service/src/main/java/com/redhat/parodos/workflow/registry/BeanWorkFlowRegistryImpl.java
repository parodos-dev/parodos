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
import com.redhat.parodos.workflows.workflow.WorkFlow;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    // WorkFlow Maps with db id and entities
    private final Map<String, WorkFlow> workFlowExecutionNameMap = new HashMap<>();
    // WorkFlow Task Maps with db id and entities
    private final Map<String, WorkFlowTaskDefinition> workFlowDefinitionTaskNameMap = new HashMap<>();
    private final Map<String, UUID> workFlowDefinitionTaskIdMap = new HashMap<>();

    public BeanWorkFlowRegistryImpl(Map<String, WorkFlow> workFlows) {
        this.workFlows = workFlows;

        if (workFlows == null) {
            log.error("No workflows were registered. Initializing an empty collection of workflows so the application can start");
            workFlows = new HashMap<>();
        }
        log.info(">> Detected {} WorkFlow from the Bean Registry", workFlows.size());

        // TODO: refine into services
        //        this.workFlowDefinitions.forEach(wd -> {
        //            WorkFlowDefinitionEntity workFlowDefinitionEntity = workFlowDefinitionRepository.save(WorkFlowDefinitionEntity.builder()
        //                    .name(wd.getName())
        //                    .description(wd.getDescription())
        //                    .type(wd.getType().name())
        //                    .author(wd.getAuthor())
        //                    .createDate(wd.getCreatedDate())
        //                    .modifyDate(wd.getCreatedDate())
        //                    .build());
        //            wd.getTasks().forEach(wdt -> {
        //                try {
        //                    WorkFlowTaskDefinitionEntity taskEntity = WorkFlowTaskDefinitionEntity.builder()
        //                            .name(wdt.getName())
        //                            .description(wdt.getDescription())
        //                            .createDate(wdt.getCreateDate())
        //                            .modifyDate(wdt.getModifyDate())
        //                            .parameters(objectMapper.writeValueAsString(wdt.getParameters()))
        //                            .outputs(objectMapper.writeValueAsString(wdt.getOutputs()))
        //                            .workFlowDefinitionEntity(workFlowDefinitionEntity)
        //                            .build();
        //
        //                    WorkFlowTaskDefinitionEntity workFlowTaskDefinitionEntity = workFlowTaskDefinitionRepository.save(taskEntity);
        //
        //                    workFlowDefinitionTaskIdMap.put(wdt.getName(), workFlowTaskDefinitionEntity.getId());
        //
        //                    workFlowDefinitionTaskNameMap.put(String.format("%s%s%s", workFlowDefinitionEntity.getName(),
        //                                    underscoreChar,
        //                                    workFlowTaskDefinitionEntity.getName()),
        //                            workFlowTaskDefinitionEntity);
        //                } catch (JsonProcessingException e) {
        //                    throw new RuntimeException(e);
        //                }
        //            });
        //
        //            List<WorkFlowTaskDefinitionEntity> workFlowTaskDefinitionEntityList = workFlowTaskDefinitionRepository.findByWorkFlowDefinitionEntity(workFlowDefinitionEntity);
        //
        //            workFlowTaskDefinitionEntityList.forEach(workFlowTaskDefinitionEntity -> {
        //                WorkFlowTaskDefinition wtd = wd.getTasks().stream().filter(t -> t.getName().equalsIgnoreCase(workFlowTaskDefinitionEntity.getName())).findFirst().get();
        //                if (wtd.getPreviousTask() != null) {
        //                    workFlowTaskDefinitionEntity.setPreviousTask(workFlowDefinitionTaskIdMap.get(wtd.getPreviousTask().getName()));
        //                }
        //                if (wtd.getNextTask() != null) {
        //                    workFlowTaskDefinitionEntity.setNextTask(workFlowDefinitionTaskIdMap.get(wtd.getNextTask().getName()));
        //                }
        //                workFlowTaskDefinitionRepository.save(workFlowTaskDefinitionEntity);
        //            });
        //
        //            workFlowDefinitionIdMap.put(workFlowDefinitionEntity.getId(), wd);
        //        });
        //
        //        workFlowTaskDefinitions.stream()
        //                .filter(workFlowTaskDefinition -> workFlowTaskDefinition.getWorkFlowCheckerDefinition() != null)
        //                .forEach(wtd -> {
        //                    WorkFlowTaskDefinitionEntity workFlowTaskDefinitionEntity = workFlowTaskDefinitionRepository.findFirstByName(wtd.getName());
        //                    workFlowTaskDefinitionEntity.setWorkFlowCheckerDefinitionEntity(
        //                            Optional.ofNullable(wtd.getWorkFlowCheckerDefinition())
        //                                    .map(wdtChecker ->
        //                                            WorkFlowCheckerDefinitionEntity.builder()
        //                                                    .id(WorkFlowCheckerDefinitionPK.builder()
        //                                                            .workFlowCheckerId(workFlowDefinitionRepository.findFirstByName(wdtChecker.getName()).getId())
        //                                                            .taskId(workFlowTaskDefinitionEntity.getId())
        //                                                            .build())
        //                                                    .task(workFlowTaskDefinitionEntity)
        //                                                    .checkWorkFlow(workFlowDefinitionRepository.findFirstByName(wdtChecker.getName()))
        //                                                    .nextWorkFlow(workFlowDefinitionRepository.findFirstByName(wdtChecker.getNextWorkFlowDefinition().getName()))
        //                                                    .cronExpression(wdtChecker.getCronExpression())
        //                                                    .build())
        //                                    .orElse(null)
        //                    );
        //                    workFlowTaskDefinitionRepository.save(workFlowTaskDefinitionEntity);
        //                });
        //
        //        this.workFlows.forEach(we -> workFlowExecutionNameMap.put(we.getName(), we));
    }

    @Override
    public WorkFlow getWorkFlowExecutionByName(String workFlowName) {
        return workFlowExecutionNameMap.get(workFlowName);
    }

//    @Override
//    public WorkFlowDefinition getWorkFlowDefinitionById(UUID workFlowId) {
//        return workFlowDefinitionIdMap.get(workFlowId);
//    }

    @Override
    public UUID getWorkFlowTaskDefinitionId(String workFlowName, String workFlowTaskName) {
        return UUID.fromString(workFlowDefinitionTaskNameMap.get(String.format("%s%s%s",
                workFlowName,
                underscoreChar,
                workFlowTaskName)).getId().toString());
    }
}
