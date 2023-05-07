package com.redhat.parodos.repository;

import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public abstract class RepositoryTestBase {

	@Autowired
	protected WorkFlowRepository workFlowRepository;

	@Autowired
	protected TestEntityManager entityManager;

	protected void injectedComponentsAreNotNull() {
		assertNotNull(workFlowRepository);
		assertNotNull(entityManager);
	}

}
