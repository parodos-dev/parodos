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
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Escalation;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * An implementation of the WorkflowRegistry that loads all Bean definitions of type
 * WorkFlow into a list
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 * @author Richard Wang (Github: richardW98)
 */

@Slf4j
@Component
public class BeanWorkFlowRegistryImpl implements WorkFlowRegistry {

	// constants
	private static final String ROLLBACK_WORKFLOW = "fallbackWorkflow";

	private static final String PARAMETERS = "parameters";

	// Spring will populate this through classpath scanning when the Context starts up
	private final ConfigurableListableBeanFactory beanFactory;

	private final Map<String, WorkFlow> workFlows;

	private final Map<String, WorkFlowTask> workFlowTasks;

	private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	public BeanWorkFlowRegistryImpl(ConfigurableListableBeanFactory beanFactory, Map<String, WorkFlow> workFlows,
			Map<String, WorkFlowTask> workFlowTasks, WorkFlowDefinitionServiceImpl workFlowDefinitionService) {
		this.beanFactory = beanFactory;
		this.workFlows = workFlows;
		this.workFlowTasks = workFlowTasks;
		this.workFlowDefinitionService = workFlowDefinitionService;

		if (workFlows == null && workFlowTasks == null) {
			log.error(
					"No workflows or workflowTasks were registered. Initializing an empty collection of workflows so the application can start");
			workFlows = new HashMap<>();
			workFlowTasks = new HashMap<>();
		}
		log.info(">> Detected {} WorkFlow and {} workFlowTasks from the Bean Registry", workFlows.size(),
				workFlowTasks.size());
	}

	@PostConstruct
	void postInit() {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		workFlowDefinitionService.cleanAllDefinitionMappings();
		saveWorkFlow();
		saveChecker();
		log.info("workflow definitions are loaded in database");
	}

	@Override
	public WorkFlow getWorkFlowByName(String workFlowName) {
		return workFlows.get(workFlowName);
	}

	private void saveWorkFlow() {
		workFlows.keySet().stream()
				.map(workFlowName -> new AbstractMap.SimpleEntry<>(workFlowName,
						getWorkFlowTypeDetails(workFlowName,
								List.of(Assessment.class, Checker.class, Infrastructure.class, Escalation.class))))
				// sort to ensure main workflows to be saved after fallback workflows
				.sorted(Comparator.comparing(e -> String.valueOf(e.getValue().getSecond().get(ROLLBACK_WORKFLOW)),
						Comparator.naturalOrder()))
				.forEachOrdered(workFlowAnnotationEntry -> {
					// extract work units
					List<Work> works = getWorks(workFlowAnnotationEntry.getKey());
					// get workflow type and parameters from annotation
					Pair<WorkFlowType, Map<String, Object>> workFlowTypeDetailsPair = workFlowAnnotationEntry
							.getValue();
					// workflow type
					WorkFlowType workFlowType = workFlowTypeDetailsPair.getFirst();
					// workflow parameters from annotation attributes
					List<WorkParameter> workParameters = WorkFlowRegistryDelegate.getWorkParameters(
							(AnnotationAttributes[]) workFlowTypeDetailsPair.getSecond().get(PARAMETERS));
					String fallbackWorkFlowName = (String) workFlowTypeDetailsPair.getSecond().get(ROLLBACK_WORKFLOW);
					workFlowDefinitionService.save(workFlowAnnotationEntry.getKey(), workFlowType,
							workFlows.get(workFlowAnnotationEntry.getKey()).getProperties(), workParameters, works,
							getWorkFlowProcessingType(workFlows.get(workFlowAnnotationEntry.getKey())),
							fallbackWorkFlowName);
				});
	}

	private List<Work> getWorks(String workFlowName) {
		return Arrays.stream(beanFactory.getDependenciesForBean(workFlowName))
				.filter(dependency -> isBeanInstanceOf(beanFactory, dependency, WorkFlow.class, WorkFlowTask.class))
				.map(dependency -> beanFactory.getBean(dependency, Work.class)).toList();
	}

	private void saveChecker() {
		workFlowTasks.forEach((name, value) -> Arrays.stream(beanFactory.getDependenciesForBean(name))
				.filter(dependency -> isBeanInstanceOf(beanFactory, dependency, WorkFlow.class)).findFirst()
				.ifPresent(dependency -> {
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

	private Pair<WorkFlowType, Map<String, Object>> getWorkFlowTypeDetails(String workFlowBeanName,
			List<Class<? extends Annotation>> workFlowTypeAnnotations) {
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(workFlowBeanName);
		if (beanDefinition.getSource() instanceof AnnotatedTypeMetadata metadata) {
			return workFlowTypeAnnotations.stream()
					.filter(clazz -> metadata.getAnnotationAttributes(clazz.getName()) != null).findFirst()
					.map(clazz -> Pair.of(WorkFlowType.valueOf(clazz.getSimpleName().toUpperCase()),
							metadata.getAnnotationAttributes(clazz.getName())))
					.orElseThrow(() -> new RuntimeException("workflow missing type! beanName: " + workFlowBeanName));
		}
		throw new RuntimeException("workflow with no annotated type metadata!");
	}

	private WorkFlowProcessingType getWorkFlowProcessingType(Object workFlowBean) {
		String className = workFlowBean.getClass().getTypeName();
		if (className.toUpperCase().contains(WorkFlowProcessingType.PARALLEL.name())) {
			return WorkFlowProcessingType.PARALLEL;
		}
		if (className.toUpperCase().contains(WorkFlowProcessingType.SEQUENTIAL.name())) {
			return WorkFlowProcessingType.SEQUENTIAL;
		}
		return WorkFlowProcessingType.OTHER;
	}

	private boolean isBeanInstanceOf(ConfigurableListableBeanFactory configurableListableBeanFactory, String beanName,
			Class<?>... classes) {
		return Arrays.stream(classes).anyMatch(clazz -> {
			try {
				configurableListableBeanFactory.getBean(beanName, clazz);
				return true;
			}
			catch (BeansException e1) {
				return false;
			}
		});
	}

	@PreDestroy
	public void gracefulShutdown() {
		log.info(">> Shutting down the bean workflow registry");
	}

}
