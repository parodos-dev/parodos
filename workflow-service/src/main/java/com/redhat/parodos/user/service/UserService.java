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

import java.util.UUID;

/**
 * User service
 *
 * @author Annel Ketcha (Github: anludke)
 */

public interface UserService {
    UserResponseDTO save(User user);
    UserResponseDTO getUserById(UUID id);
    UserResponseDTO getUserByUsername(String username);
}
