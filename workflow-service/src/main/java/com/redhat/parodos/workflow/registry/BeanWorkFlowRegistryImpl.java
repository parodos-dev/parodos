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
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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

	private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	private final Map<String, WorkFlow> workFlows;

	private final Map<String, WorkFlowTask> workFlowTaskMap;

	private Map<String, WorkFlow> masterWorkFlows = new HashMap<>();

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
		log.info("print workflow masterWorkFlow: {}", masterWorkFlows);
		saveChecker(workFlowTaskMap);
	}

	@Override
	public WorkFlow getWorkFlowByName(String workFlowName) {
		return workFlows.get(workFlowName);
	}

	@Override
	public WorkFlow getMasterWorkFlow(String workFlowName) {
		return masterWorkFlows.get(workFlowName);
	}

	private void saveWorkFlow(Object bean, String name) {
		Map<String, WorkFlowTask> hmWorkFlowTasks = new HashMap<>();
		Map<String, WorkFlow> hmWorkFlowCheckers = new HashMap<>();

		List<Work> works = getWorkUnits(bean, name);
		log.info("print workflow: {}, works: {}", name, works);

		List<WorkFlow> workFlowCheckers = new ArrayList<>();
		works.forEach(w -> scanWorkFlowChecker(w, workFlowCheckers));
		log.info("print workFlowCheckers contain in workflow {}: {}", name, workFlowCheckers);
		if (!workFlowCheckers.isEmpty()) {
			WorkFlow masterWorkFlow = buildMasterWorkFlow(bean, name, workFlowCheckers);
			log.info("master workflow from user workflow: {}, is {}, {}", name, masterWorkFlow.getName(),
					masterWorkFlow);
			masterWorkFlows.put(name, masterWorkFlow);
		}

		// workFlowDefinitionService.save(name, ((WorkFlow) bean).getName(),
		// getWorkFlowTypeDetails(name, List.of(Assessment.class, Checker.class,
		// Infrastructure.class)).getFirst(),
		// hmWorkFlowTasks);

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

	private void scanWorkFlowChecker(Work work, List workFlowCheckers) {
		if (work instanceof BaseInfrastructureWorkFlowTask) {
			BaseInfrastructureWorkFlowTask baseInfrastructureWorkFlowTask = (BaseInfrastructureWorkFlowTask) work;
			if (null != baseInfrastructureWorkFlowTask.getWorkFlowChecker()) {
				workFlowCheckers.add(baseInfrastructureWorkFlowTask.getWorkFlowChecker());
			}
		}
		else if (work instanceof WorkFlow) {
			Arrays.stream(beanFactory.getDependenciesForBean(work.getName())).filter(dependency -> {
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
			}).forEach(
					dependency -> scanWorkFlowChecker(beanFactory.getBean(dependency, Work.class), workFlowCheckers));
		}
	}

	private WorkFlow buildMasterWorkFlow(Object bean, String name, List<WorkFlow> workFlowCheckers) {
		List<Work> workUnits = new ArrayList<>();
		workUnits.add((Work) bean);
		workUnits.addAll(workFlowCheckers);
		WorkFlow masterWorkFlow = ParallelFlow.Builder.aNewParallelFlow().named("master_" + name)
				.execute(workUnits.toArray(new Work[0])).with(Executors.newFixedThreadPool(workUnits.size())).build();
		return masterWorkFlow;
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
			List<Class<? extends Annotation>> workFlowTypeList) {
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
		if (beanDefinition.getSource() instanceof AnnotatedTypeMetadata) {
			AnnotatedTypeMetadata metadata = (AnnotatedTypeMetadata) beanDefinition.getSource();
			return workFlowTypeList.stream().filter(clazz -> metadata.getAnnotationAttributes(clazz.getName()) != null)
					.findFirst()
					.map(clazz -> Pair.of(WorkFlowType.valueOf(clazz.getSimpleName().toUpperCase()),
							metadata.getAnnotationAttributes(clazz.getName())))
					.orElseThrow(() -> new RuntimeException("workflow missing type!"));
		}
		throw new RuntimeException("workflow with no annotated type metadata!");
	}

}
