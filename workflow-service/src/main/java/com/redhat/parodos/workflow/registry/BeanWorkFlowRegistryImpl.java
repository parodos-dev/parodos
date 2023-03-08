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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Escalation;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.exceptions.WorkflowDefinitionException;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of the WorkflowRegistry that loads all Bean definitions of type
 * WorkFlow into a list
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Component
public class BeanWorkFlowRegistryImpl implements WorkFlowRegistry<String> {

	// Spring will populate this through classpath scanning when the Context starts up
	private final ConfigurableListableBeanFactory beanFactory;

	private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	private final Map<String, WorkFlow> workFlows;

	private final Map<String, WorkFlowTask> workFlowTaskMap;

	public BeanWorkFlowRegistryImpl(ConfigurableListableBeanFactory beanFactory,
			WorkFlowDefinitionServiceImpl workFlowDefinitionService, Map<String, WorkFlowTask> workFlowTaskMap,
			Map<String, WorkFlow> workFlows) {
		this.workFlows = workFlows;
		this.workFlowTaskMap = workFlowTaskMap;

		if (workFlows == null && workFlowTaskMap == null) {
			log.error(
					"No workflows or workflowTasks were registered. Initializing an empty collection of workflows so the application can start");
			workFlows = new HashMap<>();
			workFlowTaskMap = new HashMap<>();
		}
		log.info(">> Detected {} WorkFlow and {} workFlowTasks from the Bean Registry", workFlows.size(),
				workFlowTaskMap.size());

		this.beanFactory = beanFactory;
		this.workFlowDefinitionService = workFlowDefinitionService;
	}

	@PostConstruct
	void postInit() {
		workFlows.forEach((key, value) -> saveWorkFlow(value, key));
		saveChecker(workFlowTaskMap);
	}

	private void saveWorkFlow(Object bean, String name) {
		Map<String, WorkFlowTask> hmWorkFlowTasks = new HashMap<>();
		Arrays.stream(beanFactory.getDependenciesForBean(name)).filter(dependency -> {
			try {
				beanFactory.getBean(dependency, WorkFlowTask.class);
				return true;
			}
			catch (BeansException e) {
				return false;
			}
		}).forEach(dependency -> hmWorkFlowTasks.put(dependency, beanFactory.getBean(dependency, WorkFlowTask.class)));
		workFlowDefinitionService.save(name, ((WorkFlow) bean).getName(),
				getWorkFlowTypeDetails(name,
						List.of(Assessment.class, Checker.class, Infrastructure.class, Escalation.class)).getFirst(),
				hmWorkFlowTasks);
	}

	private void saveChecker(Map<String, WorkFlowTask> workFlowTaskMap) {
		workFlowTaskMap
				.forEach((name, value) -> Arrays.stream(beanFactory.getDependenciesForBean(name)).filter(dependency -> {
					try {
						beanFactory.getBean(dependency, WorkFlow.class);
						return true;
					}
					catch (BeansException e) {
						return false;
					}
				}).findFirst().ifPresent(dependency -> {
					try {
						WorkFlowCheckerDTO workFlowCheckerDTO = new ObjectMapper().convertValue(
								getWorkFlowTypeDetails(dependency, List.of(Checker.class)).getSecond(),
								WorkFlowCheckerDTO.class);
						workFlowDefinitionService.saveWorkFlowChecker(name, dependency, workFlowCheckerDTO);
					}
					catch (RuntimeException ignored) {
						log.info("{} is not a checker for {}", dependency, name);
					}
				}));
	}

	private Pair<WorkFlowType, Map<String, Object>> getWorkFlowTypeDetails(String beanName,
			List<Class<? extends Annotation>> workFlowTypeList) {
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
		if (beanDefinition.getSource() instanceof AnnotatedTypeMetadata) {
			AnnotatedTypeMetadata metadata = (AnnotatedTypeMetadata) beanDefinition.getSource();
			return workFlowTypeList.stream().filter(clazz -> metadata.getAnnotationAttributes(clazz.getName()) != null)
					.findFirst()
					.map(clazz -> {
						log.info(beanName + ":" + WorkFlowType.valueOf(clazz.getSimpleName().toUpperCase()) + " " + (metadata.getAnnotationAttributes(clazz.getName()) != null));
						return Pair.of(WorkFlowType.valueOf(clazz.getSimpleName().toUpperCase()),
								metadata.getAnnotationAttributes(clazz.getName()));
					})
					.orElseThrow(() -> new RuntimeException("workflow missing type! beanname: " + beanName));
		}
		throw new RuntimeException("workflow with no annotated type metadata!");
	}

	@Override
	public WorkFlow getWorkFlowByName(String workFlowName) {
		return workFlows.get(workFlowName);
	}

}
