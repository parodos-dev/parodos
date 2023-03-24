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
package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerMappingDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * unit test for WorkFlowDefinitionService Database operation
 *
 * @author Richard Wang (Github: richardW98)
 */

@DataJpaTest
class WorkFlowDefinitionServiceDataTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	@Autowired
	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	@Autowired
	private WorkFlowCheckerMappingDefinitionRepository workFlowCheckerMappingDefinitionRepository;

	@Autowired
	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowDefinitionService workFlowDefinitionService;

	@BeforeEach
	void init() {
		workFlowDefinitionService = new WorkFlowDefinitionServiceImpl(workFlowDefinitionRepository,
				workFlowTaskDefinitionRepository, workFlowCheckerMappingDefinitionRepository, workFlowWorkRepository,
				new ModelMapper());

		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name("test-workflow").numberOfWorks(1)
				.parameters("{}").processingType(WorkFlowType.INFRASTRUCTURE.name()).type(WorkFlowType.INFRASTRUCTURE)
				.build();
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name("test-workflow-task")
				.workFlowDefinition(workFlowDefinition).parameters("{}").outputs("[]").build();
		WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = WorkFlowCheckerMappingDefinition.builder()
				.tasks(List.of(workFlowTaskDefinition)).checkWorkFlow(workFlowDefinition).cronExpression("*/5 * * * *")
				.build();
		WorkFlowWorkDefinition workFlowWorkDefinition = WorkFlowWorkDefinition.builder()
				.workDefinitionId(UUID.randomUUID()).workFlowDefinition(workFlowDefinition).build();
		entityManager.persist(workFlowDefinition);
		entityManager.persist(workFlowTaskDefinition);
		entityManager.persist(workFlowCheckerMappingDefinition);
		entityManager.persist(workFlowWorkDefinition);
	}

	@Test
	void cleanAllDefinitionMappings_should_returnEmpty_when_MappingTableIsCleaned() {
		assertThat(workFlowDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowTaskDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowCheckerMappingDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowWorkRepository.findAll()).hasSize(1);

		workFlowDefinitionService.cleanAllDefinitionMappings();

		assertThat(workFlowDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowTaskDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowCheckerMappingDefinitionRepository.findAll()).isEmpty();
		assertThat(workFlowWorkRepository.findAll()).isEmpty();
	}

}
