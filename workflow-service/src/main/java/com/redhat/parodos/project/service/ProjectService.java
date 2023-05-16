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
package com.redhat.parodos.project.service;

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.dto.ProjectUserRoleResponseDTO;
import com.redhat.parodos.project.dto.UserRoleRequestDTO;

/**
 * Project service
 *
 * @author Annel Ketcha (Github: anludke)
 */
public interface ProjectService {

	ProjectResponseDTO save(ProjectRequestDTO projectRequestDTO);

	ProjectResponseDTO getProjectById(UUID id);

	List<ProjectResponseDTO> getProjects();

	List<ProjectResponseDTO> getProjectsByUserId(UUID userId);

	List<ProjectResponseDTO> getProjectByIdAndUserId(UUID projectId, UUID userId);

	ProjectUserRoleResponseDTO updateUserRolesToProject(UUID id, List<UserRoleRequestDTO> userRoleRequestDTOs);

	ProjectUserRoleResponseDTO removeUsersFromProject(UUID id, List<String> usernames);

}
