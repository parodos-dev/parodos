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
package com.redhat.parodos.user.dto;

import java.util.Date;
import java.util.UUID;

import jakarta.validation.constraints.Email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User response DTO
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

	private UUID id;

	private String username;

	private String firstName;

	private String lastName;

	@Email
	private String email;

	private Date createDate;

	private Date modifyDate;

}
