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

import com.redhat.parodos.user.dto.UserResponseDTO;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.repository.UserRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * User service implementation
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
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
		return modelMapper.map(userRepository.findByUsername(username).stream().findFirst(), UserResponseDTO.class);
	}

}
