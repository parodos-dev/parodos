package com.redhat.parodos.user.service;

import com.redhat.parodos.user.dto.UserResponseDTO;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceImplTest {

	private UserRepository userRepository;

	private UserServiceImpl service;

	@BeforeEach
	public void initEach() {
		this.userRepository = Mockito.mock(UserRepository.class);
		this.service = new UserServiceImpl(this.userRepository, new ModelMapper());
	}

	@Test
	void saveTestWithValidData() {
		// given
		User user = getSampleUser("test");
		Mockito.when(this.userRepository.save(Mockito.any(User.class))).thenReturn(user);

		// when
		UserResponseDTO res = this.service.save(user);

		// then
		assertNotNull(res);
		assertEquals(res.getId(), user.getId().toString());
		assertEquals(res.getUsername(), user.getUsername());
		assertEquals(res.getEmail(), user.getEmail());

		Mockito.verify(this.userRepository, Mockito.times(1)).save(Mockito.any());
	}

	@Test
	void GetUserByIdWithValidData() {
		// given
		User user = getSampleUser("test");
		Mockito.when(this.userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		// when
		UserResponseDTO res = this.service.getUserById(user.getId());

		// then
		assertNotNull(res);
		assertEquals(res.getId(), user.getId().toString());
		assertEquals(res.getUsername(), user.getUsername());
		assertEquals(res.getEmail(), user.getEmail());
		Mockito.verify(this.userRepository, Mockito.times(1)).findById(Mockito.any());
	}

	@Test
	void GetUserByIdWithInvalidData() {
		// given
		User user = getSampleUser("test");
		Mockito.when(this.userRepository.findById(user.getId())).thenReturn(Optional.empty());

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.service.getUserById(user.getId());
		});

		// then
		assertNotNull(exception);
		assertEquals(exception.getMessage(), format("User with id: %s not found", user.getId()));
		Mockito.verify(this.userRepository, Mockito.times(1)).findById(Mockito.any());
	}

	@Test
	void GetUserByNameWithValidData() {
		// given
		User user = getSampleUser("test");
		Mockito.when(this.userRepository.findByUsername(user.getUsername())).thenReturn(List.of(user));

		// when
		UserResponseDTO res = this.service.getUserByUsername(user.getUsername());

		// then
		assertNotNull(res);
		assertEquals(res.getId().toString(), user.getId().toString());
		assertEquals(res.getUsername(), user.getUsername());
		assertEquals(res.getEmail(), user.getEmail());
		Mockito.verify(this.userRepository, Mockito.times(1)).findByUsername(Mockito.any());
	}

	@Test
	void GetUserByNameWithInvalidData() {
		// given
		User user = getSampleUser("test");
		Mockito.when(this.userRepository.findByUsername(user.getUsername())).thenReturn(Collections.emptyList());

		// when
		UserResponseDTO res = this.service.getUserByUsername(user.getUsername());

		// then
		assertNull(res);
		Mockito.verify(this.userRepository, Mockito.times(1)).findByUsername(Mockito.any());
	}

	private User getSampleUser(String name) {
		User user = User.builder().username(name).email("test@test.com").build();
		user.setId(UUID.randomUUID());
		return user;
	}

}