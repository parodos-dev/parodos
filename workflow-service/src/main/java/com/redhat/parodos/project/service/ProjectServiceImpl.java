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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import com.redhat.parodos.common.entity.AbstractEntity;
import com.redhat.parodos.common.exceptions.IDType;
import com.redhat.parodos.common.exceptions.OperationDeniedException;
import com.redhat.parodos.common.exceptions.ResourceAlreadyExistsException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.project.dto.request.AccessRequestDTO;
import com.redhat.parodos.project.dto.request.ProjectRequestDTO;
import com.redhat.parodos.project.dto.request.UserRoleRequestDTO;
import com.redhat.parodos.project.dto.response.AccessResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectMemberResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectUserRoleResponseDTO;
import com.redhat.parodos.project.dto.response.UserRoleResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.entity.ProjectAccessRequest;
import com.redhat.parodos.project.entity.ProjectUserRole;
import com.redhat.parodos.project.entity.Role;
import com.redhat.parodos.project.enums.ProjectAccessStatus;
import com.redhat.parodos.project.repository.ProjectAccessRequestRepository;
import com.redhat.parodos.project.repository.ProjectRepository;
import com.redhat.parodos.project.repository.ProjectUserRoleRepository;
import com.redhat.parodos.project.repository.RoleRepository;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;

/**
 * Project service implementation
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Service
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository projectRepository;

	private final RoleRepository roleRepository;

	private final ProjectUserRoleRepository projectUserRoleRepository;

	private final ProjectAccessRequestRepository projectAccessRequestRepository;

	private final UserService userService;

	public ProjectServiceImpl(ProjectRepository projectRepository, RoleRepository roleRepository,
			ProjectUserRoleRepository projectUserRoleRepository,
			ProjectAccessRequestRepository projectAccessRequestRepository, UserService userService) {
		this.projectRepository = projectRepository;
		this.roleRepository = roleRepository;
		this.projectUserRoleRepository = projectUserRoleRepository;
		this.projectAccessRequestRepository = projectAccessRequestRepository;
		this.userService = userService;
	}

	@Override
	public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO) {
		if (projectRepository.findByNameIgnoreCase(projectRequestDTO.getName()).isPresent()) {
			throw new ResourceAlreadyExistsException(ResourceType.PROJECT, IDType.NAME, projectRequestDTO.getName());
		}
		// get owner role entity
		String roleOwner = com.redhat.parodos.project.enums.Role.OWNER.name();
		Role role = roleRepository.findByNameIgnoreCase(roleOwner)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.ROLE, IDType.NAME, roleOwner));
		// get user entity
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		// create project entity
		Project project = projectRepository.save(Project.builder().name(projectRequestDTO.getName())
				.description(projectRequestDTO.getDescription()).build());
		// map project, user and role
		ProjectUserRole projectUserRole = projectUserRoleRepository.save(ProjectUserRole.builder().id(ProjectUserRole.Id
				.builder().projectId(project.getId()).userId(user.getId()).roleId(role.getId()).build())
				.project(project).user(user).role(role).build());

		return buildProjectResponseDTO(project, List.of(projectUserRole));
	}

	@Override
	public ProjectResponseDTO getProjectById(UUID projectId) {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, projectId));
		List<ProjectUserRole> projectUserRole = projectUserRoleRepository.findByProjectIdAndUserId(projectId,
				user.getId());
		return buildProjectResponseDTO(project, projectUserRole);
	}

	@Override
	public List<ProjectResponseDTO> getProjects() {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		Map<UUID, List<ProjectUserRole>> hm = new HashMap<>();
		projectUserRoleRepository.findByUserId(user.getId()).forEach(projectUserRole -> {
			UUID key = projectUserRole.getProject().getId();
			if (hm.containsKey(key)) {
				hm.get(key).add(projectUserRole);
			}
			else {
				List<ProjectUserRole> pur = new ArrayList<>();
				pur.add(projectUserRole);
				hm.put(key, pur);
			}
		});
		return projectRepository.findAll().stream().map(project -> {
			if (hm.containsKey(project.getId())) {
				return buildProjectResponseDTO(project, hm.get(project.getId()));
			}
			return buildProjectResponseDTO(project);
		}).collect(Collectors.toList());
	}

	@Override
	public List<ProjectResponseDTO> getProjectByIdAndUserId(UUID projectId, UUID userId) {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		return projectUserRoleRepository.findByProjectIdAndUserId(projectId, userId).stream()
				.map(this::buildProjectResponseDTO).toList();
	}

	@Override
	public List<ProjectResponseDTO> getProjectsByUserId(UUID userId) {
		return projectUserRoleRepository.findByUserId(userId).stream().map(this::buildProjectResponseDTO).toList();
	}

	@Transactional
	public ProjectUserRoleResponseDTO updateUserRolesToProject(UUID projectId,
			List<UserRoleRequestDTO> userRoleRequestDTOs) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, projectId));

		Set<ProjectUserRole> projectUserRoles = userRoleRequestDTOs.stream()
				.map(userRoleRequestDTO -> userRoleRequestDTO.getRoles().stream()
						.map(role -> Map.entry(userService.getUserEntityByUsername(userRoleRequestDTO.getUsername()),
								roleRepository.findByNameIgnoreCase(role.name())))
						.collect(Collectors.toSet()))
				.flatMap(Set::stream)
				.filter(userRoleMap -> userRoleMap.getKey() != null && userRoleMap.getValue().isPresent())
				.map(userRoleMap -> ProjectUserRole.builder()
						.id(ProjectUserRole.Id.builder().projectId(project.getId()).userId(userRoleMap.getKey().getId())
								.roleId(userRoleMap.getValue().get().getId()).build())
						.project(project).user(userRoleMap.getKey()).role(userRoleMap.getValue().get()).build())
				.collect(Collectors.toSet());

		projectUserRoleRepository.deleteAllByIdProjectIdAndIdUserIdIn(project.getId(),
				projectUserRoles.stream().map(projectUserRole -> projectUserRole.getUser().getId()).toList());
		project.setProjectUserRoles(projectUserRoles);
		projectUserRoleRepository.saveAll(projectUserRoles);
		return getProjectUserRoleResponseDTOFromProject(project);
	}

	@Transactional
	@Override
	public ProjectUserRoleResponseDTO removeUsersFromProject(UUID projectId, List<String> usernames) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, projectId));

		projectUserRoleRepository.deleteAllByIdProjectIdAndIdUserIdIn(project.getId(),
				userService.findAllUserEntitiesByUsernameIn(usernames).stream().map(AbstractEntity::getId).toList());
		project.getProjectUserRoles()
				.removeIf(projectUserRole -> usernames.contains(projectUserRole.getUser().getUsername()));
		return getProjectUserRoleResponseDTOFromProject(project);
	}

	@Override
	public AccessResponseDTO createAccessRequestToProject(UUID projectId, AccessRequestDTO accessRequestDTO) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, projectId));
		if (isNull(accessRequestDTO.getUsername()) || accessRequestDTO.getUsername().isEmpty()) {
			accessRequestDTO.setUsername(SecurityUtils.getUsername());
		}
		User user = userService.getUserEntityByUsername(accessRequestDTO.getUsername());
		if (isNull(accessRequestDTO.getRole())) {
			accessRequestDTO.setRole(com.redhat.parodos.project.enums.Role.DEVELOPER);
		}
		Role role = roleRepository.findByNameIgnoreCase(accessRequestDTO.getRole().name()).orElseThrow(
				() -> new ResourceNotFoundException(ResourceType.ROLE, IDType.NAME, accessRequestDTO.getRole().name()));
		Optional<ProjectUserRole> projectUserRoleOptional = projectUserRoleRepository
				.findByProjectIdAndUserIdAndRoleId(project.getId(), user.getId(), role.getId());
		if (projectUserRoleOptional.isPresent()) {
			throw new OperationDeniedException(String.format("User %s already assigned into project %s with role %s",
					user.getUsername(), project.getName(), projectUserRoleOptional.get().getRole().getName()));
		}
		ProjectAccessRequest projectAccessRequest = projectAccessRequestRepository.save(ProjectAccessRequest.builder()
				.project(project).user(user).role(role).status(ProjectAccessStatus.PENDING).build());
		User projectOwner = userService.getUserEntityById(project.getCreatedBy());
		List<User> projectAdmins = getProjectUsersByRoleName(project.getId(),
				com.redhat.parodos.project.enums.Role.ADMIN);
		return AccessResponseDTO.builder().accessRequestId(projectAccessRequest.getId())
				.project(AccessResponseDTO.ProjectDTO.builder().id(project.getId()).name(project.getName())
						.createdBy(String.format("%s, %s", projectOwner.getFirstName(), projectOwner.getLastName()))
						.createdDate(project.getCreatedDate()).build())
				.approvalSentTo((isNull(projectAdmins) || projectAdmins.isEmpty())
						? Collections.singletonList(projectOwner.getUsername())
						: projectAdmins.stream().map(User::getUsername).collect(Collectors.toList()))
				.escalationSentTo(projectOwner.getUsername()).build();
	}

	@Override
	public List<ProjectMemberResponseDTO> getProjectMembersById(UUID projectId) {
		List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findByProjectId(projectId);
		if (projectUserRoles.isEmpty()) {
			throw new ResourceNotFoundException(ResourceType.PROJECT, projectId);
		}
		Map<UUID, ProjectMemberResponseDTO> hm = new HashMap<>();
		projectUserRoles.forEach(projectUserRole -> {
			UUID key = projectUserRole.getUser().getId();
			if (hm.containsKey(key)) {
				hm.get(key).getRoles().add(projectUserRole.getRole().getName());
			}
			else {
				Set<String> hs = new TreeSet<>();
				hs.add(projectUserRole.getRole().getName());
				hm.put(key,
						ProjectMemberResponseDTO.builder().username(projectUserRole.getUser().getUsername())
								.firstName(projectUserRole.getUser().getFirstName())
								.lastName(projectUserRole.getUser().getLastName()).roles(hs).build());
			}
		});
		return hm.values().stream().sorted(Comparator.comparing(ProjectMemberResponseDTO::getUsername)).toList();
	}

	private ProjectUserRoleResponseDTO getProjectUserRoleResponseDTOFromProject(Project project) {
		return ProjectUserRoleResponseDTO.builder().id(project.getId()).projectName(project.getName())
				.userResponseDTOList(project.getProjectUserRoles().stream()
						.collect(groupingBy(projectUserRole -> projectUserRole.getUser().getUsername())).entrySet()
						.stream()
						.map(projectUserRoleEntry -> UserRoleResponseDTO.builder()
								.username(projectUserRoleEntry.getKey())
								.roles(projectUserRoleEntry.getValue().stream()
										.map(projectUserRole -> com.redhat.parodos.project.enums.Role
												.valueOf(projectUserRole.getRole().getName().toUpperCase()))
										.collect(Collectors.toSet()))
								.build())
						.toList())
				.build();
	}

	private ProjectResponseDTO buildProjectResponseDTO(Project project, List<ProjectUserRole> projectUserRoles) {
		User createdByUser = userService.getUserEntityById(project.getCreatedBy());
		return ProjectResponseDTO.builder().id(project.getId()).name(project.getName())
				.description(project.getDescription()).createdDate(project.getCreatedDate())
				.createdBy(createdByUser.getUsername()).modifiedDate(project.getModifiedDate())
				.modifiedBy(project.getModifiedBy() != null
						? userService.getUserEntityById(project.getModifiedBy()).getUsername() : null)
				.accessRoles((projectUserRoles != null && !projectUserRoles.isEmpty()) ? projectUserRoles.stream()
						.map(projectUserRole -> projectUserRole.getRole().getName()).collect(Collectors.toList())
						: null)
				.build();
	}

	private ProjectResponseDTO buildProjectResponseDTO(Project project) {
		User createdByUser = userService.getUserEntityById(project.getCreatedBy());
		User modifiedByUser = userService.getUserEntityById(project.getModifiedBy());
		return ProjectResponseDTO.builder().id(project.getId()).name(project.getName())
				.description(project.getDescription()).createdDate(project.getCreatedDate())
				.createdBy(createdByUser.getUsername()).modifiedDate(project.getModifiedDate())
				.modifiedBy(modifiedByUser.getUsername()).build();
	}

	private ProjectResponseDTO buildProjectResponseDTO(ProjectUserRole projectUserRole) {
		User createdByUser = userService.getUserEntityById(projectUserRole.getProject().getCreatedBy());
		User modifiedByUser = userService.getUserEntityById(projectUserRole.getProject().getModifiedBy());
		return ProjectResponseDTO.builder().id(projectUserRole.getProject().getId())
				.name(projectUserRole.getProject().getName()).description(projectUserRole.getProject().getDescription())
				.createdDate(projectUserRole.getProject().getCreatedDate()).createdBy(createdByUser.getUsername())
				.modifiedDate(projectUserRole.getProject().getModifiedDate())
				.modifiedBy(modifiedByUser != null ? modifiedByUser.getUsername() : null)
				.accessRoles(List.of(projectUserRole.getRole().getName())).build();
	}

	private List<User> getProjectUsersByRoleName(UUID projectId, com.redhat.parodos.project.enums.Role roleName) {
		Role role = roleRepository.findByNameIgnoreCase(roleName.name())
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.ROLE, IDType.NAME, roleName.name()));
		List<ProjectUserRole> projectUserRoleList = projectUserRoleRepository.findByProjectIdAndRoleId(projectId,
				role.getId());
		return projectUserRoleList.stream().map(ProjectUserRole::getUser).collect(Collectors.toList());
	}

}
