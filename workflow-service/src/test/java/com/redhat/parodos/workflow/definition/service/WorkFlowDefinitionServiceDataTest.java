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
				.type(WorkFlowType.INFRASTRUCTURE).build();
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name("test-workflow-task")
				.workFlowDefinition(workFlowDefinition).build();
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
	void cleanAllDefinition_should_returnEmpty_when_DatabaseIsCleaned() {
		assertThat(workFlowDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowTaskDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowCheckerMappingDefinitionRepository.findAll()).hasSize(1);
		assertThat(workFlowWorkRepository.findAll()).hasSize(1);

		workFlowDefinitionService.cleanAllDefinitions();

		assertThat(workFlowDefinitionRepository.findAll()).isEmpty();
		assertThat(workFlowTaskDefinitionRepository.findAll()).isEmpty();
		assertThat(workFlowCheckerMappingDefinitionRepository.findAll()).isEmpty();
		assertThat(workFlowWorkRepository.findAll()).isEmpty();
	}

}
