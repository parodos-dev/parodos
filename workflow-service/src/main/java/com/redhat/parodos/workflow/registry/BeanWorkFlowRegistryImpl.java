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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import com.redhat.parodos.workflow.annotation.Escalation;

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
		workFlowDefinitionService.cleanAllDefinitions();
		workFlows.forEach(this::saveWorkFlow);
		saveChecker(workFlowTasks);
		log.info("workflow definitions are loaded in database");
	}

	@Override
	public WorkFlow getWorkFlowByName(String workFlowName) {
		return workFlows.get(workFlowName);
	}

	private void saveWorkFlow(String workFlowBeanName, Object workFlowBean) {
		// extract work units
		List<Work> works = getWorks(workFlowBeanName);
		// get workflow type and parameters from annotation
		Pair<WorkFlowType, Map<String, Object>> workFlowTypeDetailsPair = getWorkFlowTypeDetails(workFlowBeanName,
				List.of(Assessment.class, Checker.class, Infrastructure.class, Escalation.class));
		// workflow type
		WorkFlowType workFlowType = workFlowTypeDetailsPair.getFirst();
		// workflow parameters from annotation attributes
		AnnotationAttributes[] annotationAttributes = (AnnotationAttributes[]) workFlowTypeDetailsPair.getSecond()
				.get("parameters");
		List<WorkFlowParameter> workFlowParameters = new ArrayList<>();
		if (annotationAttributes != null && annotationAttributes.length > 0) {
			workFlowParameters = Arrays.stream(annotationAttributes)
					.map(annotationAttribute -> WorkFlowParameter.builder().key(annotationAttribute.getString("key"))
							.description(annotationAttribute.getString("description"))
							.type((WorkFlowParameterType) annotationAttribute.get("type"))
							.optional(annotationAttribute.getBoolean("optional")).build())
					.collect(Collectors.toList());
		}
		workFlowDefinitionService.save(workFlowBeanName, workFlowType, workFlowParameters, works,
				getWorkFlowProcessingType(workFlowBean));
	}

	private List<Work> getWorks(String workFlowName) {
		return Arrays.stream(beanFactory.getDependenciesForBean(workFlowName))
				.filter(dependency -> isBeanInstanceOf(beanFactory, dependency, WorkFlow.class, WorkFlowTask.class))
				.map(dependency -> beanFactory.getBean(dependency, Work.class)).collect(Collectors.toList());
	}

	private void saveChecker(Map<String, WorkFlowTask> workFlowTasks) {
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
		if (beanDefinition.getSource() instanceof AnnotatedTypeMetadata) {
			AnnotatedTypeMetadata metadata = (AnnotatedTypeMetadata) beanDefinition.getSource();
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
		if (className.toUpperCase().contains(WorkFlowProcessingType.PARALLEL.name()))
			return WorkFlowProcessingType.PARALLEL;
		if (className.toUpperCase().contains(WorkFlowProcessingType.SEQUENTIAL.name()))
			return WorkFlowProcessingType.SEQUENTIAL;
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

}
