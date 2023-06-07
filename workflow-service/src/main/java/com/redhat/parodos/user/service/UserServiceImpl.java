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

import com.redhat.parodos.common.exceptions.IDType;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.user.dto.UserResponseDTO;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * User service implementation
 *
 * @author Annel Ketcha (Github: anludke)
 */
@Service
@Slf4j
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
	public UserResponseDTO saveCurrentUser() {
		String username = SecurityUtils.getUsername();
		UserResponseDTO userResponseDTO = null;
		if (username != null && findAllUserEntitiesByUsernameIn(List.of(username)).isEmpty()) {
			userResponseDTO = save(User.builder().username(username).firstName(SecurityUtils.getFirstname())
					.lastName(SecurityUtils.getLastname()).email(SecurityUtils.getMail()).enabled(true).build());
			log.info("new logged-in user: {} is added", username);
		}
		return userResponseDTO;
	}

	@Override
	public UserResponseDTO getUserById(UUID id) {
		return modelMapper.map(
				userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ResourceType.USER, id)),
				UserResponseDTO.class);
	}

	@Override
	public UserResponseDTO getUserByUsername(String username) {
		Optional<User> user = userRepository.findByUsername(username);
		if (!user.isPresent()) {
			throw new ResourceNotFoundException(ResourceType.USER, IDType.NAME, username);
		}
		return modelMapper.map(user.get(), UserResponseDTO.class);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public User getUserEntityByUsername(String username) {
		return userRepository.findByUsername(username).stream().findFirst()
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.USER, IDType.NAME, username));
	}

	@Override
	public User getUserEntityById(UUID id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ResourceType.USER, id));
	}

	@Override
	public List<User> findAllUserEntitiesByUsernameIn(List<String> usernames) {
		return userRepository.findAllByUsernameIn(usernames);
	}

}
