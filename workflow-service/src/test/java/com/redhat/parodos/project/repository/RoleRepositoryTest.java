package com.redhat.parodos.project.repository;

import com.redhat.parodos.repository.RepositoryTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleRepositoryTest extends RepositoryTestBase {

	@Autowired
	private RoleRepository roleRepository;

	@Test
	public void injectedComponentsAreNotNull() {
		super.injectedComponentsAreNotNull();
		assertNotNull(roleRepository);
	}

	@Test
	void testFindByNameIgnoreCase() {

		assertThat(roleRepository.findByNameIgnoreCase("developer")).isNotEmpty();
		assertThat(roleRepository.findByNameIgnoreCase("admin")).isNotEmpty();
		assertThat(roleRepository.findByNameIgnoreCase("owner")).isNotEmpty();

		assertThat(roleRepository.findByNameIgnoreCase("test")).isEmpty();
	}

}
