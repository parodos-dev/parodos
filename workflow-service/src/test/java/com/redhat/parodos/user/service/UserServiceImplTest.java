package com.redhat.parodos.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.user.dto.UserResponseDTO;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {

	private UserRepository userRepository;

	private UserServiceImpl service;

	@BeforeEach
	public void initEach() {
		this.userRepository = mock(UserRepository.class);
		this.service = new UserServiceImpl(this.userRepository, new ModelMapper());
	}

	@Test
	void saveTestWithValidData() {
		// given
		User user = getSampleUser();
		when(this.userRepository.save(any(User.class))).thenReturn(user);

		// when
		UserResponseDTO res = this.service.save(user);

		// then
		assertNotNull(res);
		assertEquals(res.getId(), user.getId());
		assertEquals(res.getUsername(), user.getUsername());
		assertEquals(res.getEmail(), user.getEmail());

		verify(this.userRepository, times(1)).save(any());
	}

	@Test
	void GetUserByIdWithValidData() {
		// given
		User user = getSampleUser();
		when(this.userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		// when
		UserResponseDTO res = this.service.getUserById(user.getId());

		// then
		assertNotNull(res);
		assertEquals(res.getId(), user.getId());
		assertEquals(res.getUsername(), user.getUsername());
		assertEquals(res.getEmail(), user.getEmail());
		verify(this.userRepository, times(1)).findById(any());
	}

	@Test
	void GetUserByIdWithInvalidData() {
		// given
		User user = getSampleUser();
		when(this.userRepository.findById(user.getId())).thenReturn(Optional.empty());

		// when
		Exception exception = assertThrows(ResourceNotFoundException.class,
				() -> this.service.getUserById(user.getId()));

		// then
		assertNotNull(exception);
		assertEquals(exception.getMessage(), format("User with ID: %s not found", user.getId()));
		verify(this.userRepository, times(1)).findById(any());
	}

	@Test
	void GetUserByNameWithValidData() {
		// given
		User user = getSampleUser();
		when(this.userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

		// when
		UserResponseDTO res = this.service.getUserByUsername(user.getUsername());

		// then
		assertNotNull(res);
		assertEquals(user.getId(), res.getId());
		assertEquals(user.getUsername(), res.getUsername());
		assertEquals(user.getEmail(), res.getEmail());
		verify(this.userRepository, times(1)).findByUsername(any());
	}

	@Test
	void GetUserByNameWithInvalidData() {
		// given
		User user = getSampleUser();
		when(this.userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

		// then
		assertThrows(ResourceNotFoundException.class, () -> this.service.getUserByUsername(user.getUsername()));
		verify(this.userRepository, times(1)).findByUsername(any());
	}

	@Test
	@WithMockUser("test")
	void testSaveCurrentUser() {
		String username = "test";

		when(userRepository.findAllByUsernameIn(List.of(username))).thenReturn(List.of());
		when(userRepository.save(any(User.class))).thenReturn(User.builder().username(username).build());
		assertEquals(username, service.saveCurrentUser().getUsername());

		// Then
		verify(userRepository, times(1)).findAllByUsernameIn(any());
		verify(userRepository, times(1)).save(any());

	}

	private User getSampleUser() {
		User user = User.builder().username(UUID.randomUUID().toString()).email("test@test.com").build();
		user.setId(UUID.randomUUID());
		return user;
	}

}
