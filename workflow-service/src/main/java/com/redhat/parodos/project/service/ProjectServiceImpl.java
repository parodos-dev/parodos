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
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityExistsException;

import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.repository.ProjectRepository;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.user.service.UserService;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
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

	private final WorkFlowRepository workFlowRepository;

	private final UserService userService;

	private final ModelMapper modelMapper;

	public ProjectServiceImpl(ProjectRepository projectRepository, WorkFlowRepository workFlowRepository,
			UserService userService, ModelMapper modelMapper) {
		this.projectRepository = projectRepository;
		this.workFlowRepository = workFlowRepository;
		this.userService = userService;
		this.modelMapper = modelMapper;
	}

	@Override
	public ProjectResponseDTO save(ProjectRequestDTO projectRequestDTO) {
		Optional<Project> projectByName = projectRepository.findByNameIgnoreCase(projectRequestDTO.getName());
		if (projectByName.isPresent()) {
			throw new EntityExistsException(
					String.format("Project with name: %s already exists", projectByName.get().getName()));
		}
		// get user from security utils and set on project
		Project project = projectRepository.save(Project.builder().name(projectRequestDTO.getName())
				.description(projectRequestDTO.getDescription()).createDate(new Date()).modifyDate(new Date())
				.user(userService.getUserEntityByUsername(SecurityUtils.getUsername())).build());
		return modelMapper.map(project, ProjectResponseDTO.class);
	}

	@Override
	public ProjectResponseDTO getProjectById(UUID id) {
		return modelMapper
				.map(projectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("Project with id: %s not found", id))), ProjectResponseDTO.class);
	}

	@Override
	public List<ProjectResponseDTO> getProjects() {
		List<Project> projects = projectRepository.findAll();
		return projects.stream().map(project -> {
			WorkFlowExecution workFlowExecution = workFlowRepository
					.findFirstByProjectIdAndMainWorkFlowExecutionIsNullOrderByStartDateDesc(project.getId());
			return ProjectResponseDTO.builder().id(project.getId()).name(project.getName())
					.createDate(project.getCreateDate()).modifyDate(project.getModifyDate())
					.description(project.getDescription())
					.status(null == workFlowExecution ? "" : workFlowExecution.getStatus().name()).build();
		}).toList();
	}

	@Override
	public ProjectResponseDTO getProjectByIdAndUsername(UUID id, String username) {
		return modelMapper.map(
				projectRepository.findByIdAndUserUsername(id, username)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
								"Project with id: %s not found for user: %s", id, SecurityUtils.getUsername()))),
				ProjectResponseDTO.class);
	}

	@Override
	public List<ProjectResponseDTO> findProjectsByUserName(String username) {
		return projectRepository.findAllByUserUsername(username).stream()
				.map(project -> modelMapper.map(project, ProjectResponseDTO.class)).toList();
	}

}
