package com.redhat.parodos.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
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
