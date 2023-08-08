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

import com.redhat.parodos.project.dto.request.AccessRequestDTO;
import com.redhat.parodos.project.dto.request.ProjectRequestDTO;
import com.redhat.parodos.project.dto.request.UserRoleRequestDTO;
import com.redhat.parodos.project.dto.response.AccessResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectMemberResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectUserRoleResponseDTO;

/**
 * Project service
 *
 * @author Annel Ketcha (Github: anludke)
 */
public interface ProjectService {

	ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO);

	ProjectResponseDTO getProjectById(UUID projectId);

	List<ProjectResponseDTO> getProjects();

	List<ProjectResponseDTO> getProjectsByUserId(UUID userId);

	List<ProjectResponseDTO> getProjectByIdAndUserId(UUID projectId, UUID userId);

	ProjectUserRoleResponseDTO updateUserRolesToProject(UUID projectId, List<UserRoleRequestDTO> userRoleRequestDTOs);

	ProjectUserRoleResponseDTO removeUsersFromProject(UUID projectId, List<String> usernames);

	AccessResponseDTO createAccessRequestToProject(UUID projectId, AccessRequestDTO accessRequestDTO);

	List<ProjectMemberResponseDTO> getProjectMembersById(UUID projectId);

}
