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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.parameter.WorkFlowParameter;
import com.redhat.parodos.workflow.parameter.WorkFlowParameterType;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

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
		workFlows.forEach((key, value) -> saveWorkFlow(value, key));
		saveChecker(workFlowTasks);
	}

	@Override
	public WorkFlow getWorkFlowByName(String workFlowName) {
		return workFlows.get(workFlowName);
	}

	private void saveWorkFlow(Object bean, String name) {
		List<Work> works = getWorkUnits(bean, name);
		// save workflow -> workFlowTasks
		Map<String, WorkFlowTask> workTasks = new HashMap<>();
		works.stream().filter(work -> work instanceof WorkFlowTask)
				.forEach(work -> workTasks.put(work.getName(), (WorkFlowTask) work));

		Pair<WorkFlowType, Map<String, Object>> workFlowTypeDetailsPair = getWorkFlowTypeDetails(name,
				List.of(Assessment.class, Checker.class, Infrastructure.class));
		AnnotationAttributes[] annotationAttributes = (AnnotationAttributes[]) workFlowTypeDetailsPair.getSecond()
				.get("parameters");
		List<WorkFlowParameter> workFlowParameters = new ArrayList<>();
		if (annotationAttributes != null && annotationAttributes.length > 0) {
			workFlowParameters = Arrays.asList(annotationAttributes).stream()
					.map(annotationAttribute -> WorkFlowParameter.builder().key(annotationAttribute.getString("key"))
							.description(annotationAttribute.getString("description"))
							.type((WorkFlowParameterType) annotationAttribute.get("type"))
							.optional(annotationAttribute.getBoolean("optional")).build())
					.collect(Collectors.toList());
		}
		workFlowDefinitionService.save(name, ((WorkFlow) bean).getName(), workFlowTypeDetailsPair.getFirst(),
				workFlowParameters, workTasks, works, getWorkFlowProcessingType(bean));
	}

	private List<Work> getWorkUnits(Object bean, String name) {
		return Arrays.stream(beanFactory.getDependenciesForBean(name)).filter(dependency -> {
			try {
				beanFactory.getBean(dependency, WorkFlow.class);
				return true;
			}
			catch (BeansException e1) {
				try {
					beanFactory.getBean(dependency, WorkFlowTask.class);
					return true;
				}
				catch (BeansException e2) {
					return false;
				}
			}
		}).map(dependency -> beanFactory.getBean(dependency, Work.class)).collect(Collectors.toList());
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
					catch (IllegalArgumentException ignored) {
						log.info("{} is not a checker for {}", dependency, name);
					}
				}));
	}

	private Pair<WorkFlowType, Map<String, Object>> getWorkFlowTypeDetails(String beanName,
			List<Class<? extends Annotation>> workFlowTypeAnnotations) {
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
		if (beanDefinition.getSource() instanceof AnnotatedTypeMetadata) {
			AnnotatedTypeMetadata metadata = (AnnotatedTypeMetadata) beanDefinition.getSource();
			return workFlowTypeAnnotations.stream()
					.filter(clazz -> metadata.getAnnotationAttributes(clazz.getName()) != null).findFirst()
					.map(clazz -> Pair.of(WorkFlowType.valueOf(clazz.getSimpleName().toUpperCase()),
							metadata.getAnnotationAttributes(clazz.getName())))
					.orElseThrow(() -> new RuntimeException("workflow missing type!"));
		}
		throw new RuntimeException("workflow with no annotated type metadata!");
	}

	private WorkFlowProcessingType getWorkFlowProcessingType(Object bean) {
		String className = bean.getClass().getTypeName();
		if (className.toUpperCase().contains(WorkFlowProcessingType.PARALLEL.name()))
			return WorkFlowProcessingType.PARALLEL;
		if (className.toUpperCase().contains(WorkFlowProcessingType.SEQUENTIAL.name()))
			return WorkFlowProcessingType.SEQUENTIAL;
		return WorkFlowProcessingType.OTHER;
	}

}
