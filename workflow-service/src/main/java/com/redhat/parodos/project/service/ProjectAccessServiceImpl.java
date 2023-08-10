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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.IDType;
import com.redhat.parodos.common.exceptions.OperationDeniedException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.project.dto.request.AccessStatusRequestDTO;
import com.redhat.parodos.project.dto.request.UserRoleRequestDTO;
import com.redhat.parodos.project.dto.response.AccessStatusResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectAccessRequestDTO;
import com.redhat.parodos.project.entity.ProjectAccessRequest;
import com.redhat.parodos.project.entity.ProjectUserRole;
import com.redhat.parodos.project.enums.ProjectAccessStatus;
import com.redhat.parodos.project.enums.Role;
import com.redhat.parodos.project.repository.ProjectAccessRequestRepository;
import com.redhat.parodos.project.repository.ProjectUserRoleRepository;
import com.redhat.parodos.project.repository.RoleRepository;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

/**
 * Project access service implementation
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Service
public class ProjectAccessServiceImpl implements ProjectAccessService {

	private final UserServiceImpl userService;

	private final ProjectServiceImpl projectService;

	private final ProjectAccessRequestRepository projectAccessRequestRepository;

	private final ProjectUserRoleRepository projectUserRoleRepository;

	private final RoleRepository roleRepository;

	public ProjectAccessServiceImpl(UserServiceImpl userService, ProjectServiceImpl projectService,
			ProjectAccessRequestRepository projectAccessRequestRepository,
			ProjectUserRoleRepository projectUserRoleRepository, RoleRepository roleRepository) {
		this.userService = userService;
		this.projectService = projectService;
		this.projectAccessRequestRepository = projectAccessRequestRepository;
		this.projectUserRoleRepository = projectUserRoleRepository;
		this.roleRepository = roleRepository;
	}

	@Override
	public AccessStatusResponseDTO getProjectAccessStatusById(UUID id) {
		ProjectAccessRequest projectAccessRequest = projectAccessRequestRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException(ResourceType.ACCESS_REQUEST, IDType.ID, id.toString()));
		return AccessStatusResponseDTO.builder().accessRequestId(id).status(projectAccessRequest.getStatus()).build();
	}

	@Transactional
	@Override
	public void updateProjectAccessStatusById(UUID id, AccessStatusRequestDTO accessStatusRequestDTO) {
		String username = SecurityUtils.getUsername();
		User user = userService.getUserEntityByUsername(username);
		ProjectAccessRequest projectAccessRequest = projectAccessRequestRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException(ResourceType.ACCESS_REQUEST, IDType.ID, id.toString()));
		if (canUserUpdateProjectAccessStatus(projectAccessRequest.getProject().getId(), user.getId())) {
			projectAccessRequest.setStatus(accessStatusRequestDTO.getStatus());
			projectAccessRequest.setComment(accessStatusRequestDTO.getComment());
			projectAccessRequestRepository.save(projectAccessRequest);
			if (accessStatusRequestDTO.getStatus().equals(ProjectAccessStatus.APPROVED)) {
				projectService.updateUserRolesToProject(projectAccessRequest.getProject().getId(),
						Collections.singletonList(
								UserRoleRequestDTO.builder().username(projectAccessRequest.getUser().getUsername())
										.roles(Collections.singletonList(
												Role.valueOf(projectAccessRequest.getRole().getName().toUpperCase())))
										.build()));
			}
		}
		else {
			throw new OperationDeniedException(String.format("User: %s cannot update access request on project: %s",
					user.getUsername(), projectAccessRequest.getProject().getName()));
		}
	}

	@Override
	public List<ProjectAccessRequestDTO> getPendingProjectAccessRequests() {
		return projectAccessRequestRepository.findAll().stream()
				.filter(projectAccessRequest -> ProjectAccessStatus.PENDING == projectAccessRequest.getStatus())
				.map(projectAccessRequest -> ProjectAccessRequestDTO.builder()
						.accessRequestId(projectAccessRequest.getId())
						.projectId(projectAccessRequest.getProject().getId())
						.role(projectAccessRequest.getRole().getName())
						.username(projectAccessRequest.getUser().getUsername())
						.firstname(projectAccessRequest.getUser().getFirstName())
						.lastname(projectAccessRequest.getUser().getLastName())
						.createDate(projectAccessRequest.getCreatedDate()).comment(projectAccessRequest.getComment())
						.status(projectAccessRequest.getStatus()).build())
				.toList();
	}

	private Boolean canUserUpdateProjectAccessStatus(UUID projectId, UUID userId) {
		List<ProjectUserRole> projectUserRoleList = projectUserRoleRepository.findByProjectIdAndUserId(projectId,
				userId);
		if (!isNull(projectUserRoleList) && !projectUserRoleList.isEmpty()) {
			return projectUserRoleList.stream()
					.anyMatch(projectUserRole -> projectUserRole.getUser().getId().equals(userId)
							&& (projectUserRole.getRole().getName().equalsIgnoreCase(Role.ADMIN.name())
									|| projectUserRole.getRole().getName().equalsIgnoreCase(Role.OWNER.name())));
		}
		return false;
	}

}
