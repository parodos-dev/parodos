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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.redhat.parodos.common.entity.AbstractEntity;
import com.redhat.parodos.common.exceptions.IDType;
import com.redhat.parodos.common.exceptions.ResourceAlreadyExistsException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.dto.ProjectUserRoleResponseDTO;
import com.redhat.parodos.project.dto.UserRoleRequestDTO;
import com.redhat.parodos.project.dto.UserRoleResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.entity.ProjectUserRole;
import com.redhat.parodos.project.entity.Role;
import com.redhat.parodos.project.repository.ProjectRepository;
import com.redhat.parodos.project.repository.ProjectUserRoleRepository;
import com.redhat.parodos.project.repository.RoleRepository;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserService;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	private final UserService userService;

	private final ModelMapper modelMapper;

	public ProjectServiceImpl(ProjectRepository projectRepository, RoleRepository roleRepository,
			ProjectUserRoleRepository projectUserRoleRepository, UserService userService, ModelMapper modelMapper) {
		this.projectRepository = projectRepository;
		this.roleRepository = roleRepository;
		this.projectUserRoleRepository = projectUserRoleRepository;
		this.userService = userService;
		this.modelMapper = modelMapper;
	}

	@Override
	public ProjectResponseDTO save(ProjectRequestDTO projectRequestDTO) {
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

		return buildProjectReponseDTO(user, projectUserRole);
	}

	@Override
	public ProjectResponseDTO getProjectById(UUID id) {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		ProjectUserRole projectUserRole = projectUserRoleRepository.findByProjectId(id)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, id));
		return buildProjectReponseDTO(user, projectUserRole);
	}

	@Override
	public List<ProjectResponseDTO> getProjects() {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		return projectUserRoleRepository.findByUserId(user.getId()).stream()
				.map(projectUserRole -> buildProjectReponseDTO(user, projectUserRole)).toList();
	}

	@Override
	public List<ProjectResponseDTO> getProjectByIdAndUserId(UUID projectId, UUID userId) {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		return projectUserRoleRepository.findByProjectIdAndUserId(projectId, userId).stream()
				.map(projectUserRole -> buildProjectReponseDTO(user, projectUserRole)).toList();
	}

	@Override
	public List<ProjectResponseDTO> getProjectsByUserId(UUID userId) {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		return projectUserRoleRepository.findByUserId(userId).stream()
				.map(projectUserRole -> buildProjectReponseDTO(user, projectUserRole)).toList();
	}

	@Transactional
	public ProjectUserRoleResponseDTO updateUserRolesToProject(UUID id, List<UserRoleRequestDTO> userRoleRequestDTOs) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, id));

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
	public ProjectUserRoleResponseDTO removeUsersFromProject(UUID id, List<String> usernames) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, id));

		projectUserRoleRepository.deleteAllByIdProjectIdAndIdUserIdIn(project.getId(),
				userService.findAllUserEntitiesByUsernameIn(usernames).stream().map(AbstractEntity::getId).toList());
		project.getProjectUserRoles()
				.removeIf(projectUserRole -> usernames.contains(projectUserRole.getUser().getUsername()));
		return getProjectUserRoleResponseDTOFromProject(project);
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

	private ProjectResponseDTO buildProjectReponseDTO(User user, ProjectUserRole projectUserRole) {
		return ProjectResponseDTO.builder().id(projectUserRole.getProject().getId())
				.name(projectUserRole.getProject().getName()).description(projectUserRole.getProject().getDescription())
				.createdDate(projectUserRole.getProject().getCreatedDate())
				.createdBy(projectUserRole.getUser().getUsername())
				.modifiedDate(projectUserRole.getProject().getModifiedDate())
				.modifiedBy(projectUserRole.getProject().getModifiedBy() != null
						? userService.getUserEntityById(projectUserRole.getProject().getModifiedBy()).getUsername()
						: null)
				.accessRole(projectUserRole.getUser().getId().equals(user.getId()) ? projectUserRole.getRole().getName()
						: null)
				.build();
	}

}
