package com.redhat.parodos.user.repository;

import java.util.UUID;

import jakarta.persistence.PersistenceException;

import com.redhat.parodos.repository.RepositoryTestBase;
import com.redhat.parodos.user.entity.User;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRepositoryTest extends RepositoryTestBase {

	@Autowired
	private UserRepository userRepository;

	@Test
	public void injectedComponentsAreNotNull() {
		super.injectedComponentsAreNotNull();
		assertNotNull(userRepository);
	}

	@Test
	void findByUsername() {
		// given
		final var username = UUID.randomUUID().toString();
		createUser(username);

		// when
		var user = userRepository.findByUsername(username);

		// then
		assertNotNull(user);
		assertThat(user.isPresent(), is(true));
		assertEquals(user.get().getUsername(), username);
	}

	@Test
	public void testUniqueNameConstraint() {
		// given
		final var name = UUID.randomUUID().toString();

		// when
		createUser(name);

		// then
		assertThrows(PersistenceException.class, () -> createUser(name));
	}

	private void createUser(String username) {
		var user = User.builder().username(username).build();
		entityManager.persistAndFlush(user);
	}

}
