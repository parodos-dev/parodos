package com.redhat.parodos.project.repository;

import com.redhat.parodos.repository.RepositoryTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

		assertThat(roleRepository.findByNameIgnoreCase("developer").isEmpty(), is(false));
		assertThat(roleRepository.findByNameIgnoreCase("admin").isEmpty(), is(false));
		assertThat(roleRepository.findByNameIgnoreCase("owner").isEmpty(), is(false));

		assertThat(roleRepository.findByNameIgnoreCase("test").isEmpty(), is(true));
	}

}
