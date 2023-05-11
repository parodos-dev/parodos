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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
			throw new EntityExistsException(
					String.format("Project with name: %s already exists", projectRequestDTO.getName()));
		}
		// get owner role entity
		String roleOwner = com.redhat.parodos.project.enums.Role.OWNER.name();
		Role role = roleRepository.findByNameIgnoreCase(roleOwner).orElseThrow(() -> {
			throw new EntityNotFoundException(String.format("Role with name: %s not found", roleOwner));
		});
		// get user entity
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		// create project entity
		Project project = projectRepository.save(Project.builder().name(projectRequestDTO.getName())
				.description(projectRequestDTO.getDescription()).createDate(new Date()).modifyDate(new Date()).build());
		// map project, user and role
		ProjectUserRole projectUserRole = projectUserRoleRepository.save(ProjectUserRole.builder().id(ProjectUserRole.Id
				.builder().projectId(project.getId()).userId(user.getId()).roleId(role.getId()).build())
				.project(project).user(user).role(role).build());
		return modelMapper.map(projectUserRole, ProjectResponseDTO.class);
	}

	@Override
	public ProjectResponseDTO getProjectById(UUID id) {
		return modelMapper
				.map(projectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("Project with id: %s not found", id))), ProjectResponseDTO.class);
	}

	@Override
	public List<ProjectResponseDTO> getProjects() {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		return projectUserRoleRepository.findByUserId(user.getId()).stream()
				.map(projectUserRole -> modelMapper.map(projectUserRole, ProjectResponseDTO.class)).toList();
	}

	@Override
	public List<ProjectResponseDTO> getProjectByIdAndUsername(UUID id, String username) {
		User user = userService.getUserEntityByUsername(username);
		return projectUserRoleRepository.findByProjectIdAndUserId(id, user.getId()).stream()
				.map(projectUserRole -> modelMapper.map(projectUserRole, ProjectResponseDTO.class)).toList();
	}

	@Override
	public List<ProjectResponseDTO> getProjectsByUsername(String username) {
		User user = userService.getUserEntityByUsername(username);
		return projectUserRoleRepository.findByUserId(user.getId()).stream()
				.map(projectUserRole -> modelMapper.map(projectUserRole, ProjectResponseDTO.class)).toList();
	}

}
