package com.redhat.parodos.project.repository;

import java.util.List;

import com.redhat.parodos.repository.RepositoryTestBase;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserRepositoryTest extends RepositoryTestBase {

	private static final String TEST_USER = "test-user";

	private static final String TEST_USER_2 = "test-user-2";

	@Autowired
	private UserRepository userRepository;

	@Test
	public void injectedComponentsAreNotNull() {
		super.injectedComponentsAreNotNull();
		assertNotNull(userRepository);
	}

	@Test
	void testFindAllByUsernameIn() {
		// given
		createUser(TEST_USER);
		createUser(TEST_USER_2);

		// when
		List<User> users = userRepository.findAllByUsernameIn(List.of(TEST_USER, TEST_USER_2));

		// then
		assertNotNull(users);
		assertEquals(2, users.size());
	}

	private void createUser(String name) {
		User user = User.builder().username(name).build();
		entityManager.persistAndFlush(user);
	}

}
