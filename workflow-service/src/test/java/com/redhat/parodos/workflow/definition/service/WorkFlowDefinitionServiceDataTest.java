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

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerMappingDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

/**
 * unit test for WorkFlowDefinitionService Database operation
 *
 * @author Richard Wang (Github: richardW98)
 */

@DataJpaTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
		workFlowCheckerMappingDefinitionRepository.deleteAllInBatch();
		workFlowWorkRepository.deleteAllInBatch();
		workFlowTaskDefinitionRepository.deleteAllInBatch();
		workFlowDefinitionRepository.deleteAllInBatch();

		workFlowDefinitionService = new WorkFlowDefinitionServiceImpl(workFlowDefinitionRepository,
				workFlowTaskDefinitionRepository, workFlowCheckerMappingDefinitionRepository, workFlowWorkRepository,
				new ModelMapper());

		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name("test-workflow").numberOfWorks(1)
				.parameters("{}").processingType(WorkFlowProcessingType.SEQUENTIAL).type(WorkFlowType.INFRASTRUCTURE)
				.build();
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name("test-workflow-task")
				.workFlowDefinition(workFlowDefinition).parameters("{}").outputs("[]").build();
		WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = WorkFlowCheckerMappingDefinition.builder()
				.tasks(List.of(workFlowTaskDefinition)).checkWorkFlow(workFlowDefinition).cronExpression("*/5 * * * *")
				.build();
		WorkFlowWorkDefinition workFlowWorkDefinition = WorkFlowWorkDefinition.builder()
				.workDefinitionId(UUID.randomUUID()).workFlowDefinition(workFlowDefinition).build();
		entityManager.persistAndFlush(workFlowDefinition);
		entityManager.persistAndFlush(workFlowTaskDefinition);
		entityManager.persistAndFlush(workFlowCheckerMappingDefinition);
		entityManager.persistAndFlush(workFlowWorkDefinition);
	}

	@Test
	void cleanAllDefinitionMappings_should_returnEmpty_when_MappingTableIsCleaned() {
		assertThat(workFlowDefinitionRepository.findAll(), hasSize(1));
		assertThat(workFlowTaskDefinitionRepository.findAll(), hasSize(1));
		assertThat(workFlowCheckerMappingDefinitionRepository.findAll(), hasSize(1));
		assertThat(workFlowWorkRepository.findAll(), hasSize(1));

		workFlowDefinitionService.cleanAllDefinitionMappings();

		assertThat(workFlowDefinitionRepository.findAll(), hasSize(1));
		assertThat(workFlowTaskDefinitionRepository.findAll(), hasSize(1));
		assertThat(workFlowCheckerMappingDefinitionRepository.findAll(), empty());
		assertThat(workFlowWorkRepository.findAll(), empty());
	}

}
