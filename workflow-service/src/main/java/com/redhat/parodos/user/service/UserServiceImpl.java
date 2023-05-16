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
package com.redhat.parodos.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.user.dto.UserResponseDTO;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.repository.UserRepository;
import org.modelmapper.ModelMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;

/**
 * User service implementation
 *
 * @author Annel Ketcha (Github: anludke)
 */
@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final ModelMapper modelMapper;

	public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public UserResponseDTO save(User user) {
		return modelMapper.map(userRepository.save(user), UserResponseDTO.class);
	}

	@Override
	public UserResponseDTO getUserById(UUID id) {
		return modelMapper.map(
				userRepository.findById(id)
						.orElseThrow(() -> new RuntimeException(String.format("User with id: %s not found", id))),
				UserResponseDTO.class);
	}

	@Override
	public UserResponseDTO getUserByUsername(String username) {
		Optional<User> user = userRepository.findByUsername(username);
		if (!user.isPresent()) {
			throw new RuntimeException(String.format("User with username: %s not found", username));
		}
		return modelMapper.map(user.get(), UserResponseDTO.class);
	}

	@Override
	public User getUserEntityByUsername(String username) {
		return userRepository.findByUsername(username).stream().findFirst().orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user " + username + " is not found"));
	}

	@Override
	public User getUserEntityById(UUID id) {
		return userRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException(String.format("User with id: %s not found", id)));
	}

	@Override
	public List<User> findAllUserEntitiesByUsernameIn(List<String> usernames) {
		return userRepository.findAllByUsernameIn(usernames);
	}

}
