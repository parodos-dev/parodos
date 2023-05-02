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
import java.util.stream.Collectors;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.repository.ProjectRepository;

/**
 * Project service implementation
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Service
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository projectRepository;

	private final WorkFlowRepository workFlowRepository;

	private final ModelMapper modelMapper;

	public ProjectServiceImpl(ProjectRepository projectRepository, WorkFlowRepository workFlowRepository,
			ModelMapper modelMapper) {
		this.projectRepository = projectRepository;
		this.workFlowRepository = workFlowRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public ProjectResponseDTO save(ProjectRequestDTO projectRequestDTO) {
		// get user from security utils and set on project
		Project project = projectRepository.save(Project.builder().name(projectRequestDTO.getName())
				.description(projectRequestDTO.getDescription()).createDate(new Date()).modifyDate(new Date()).build());
		return modelMapper.map(project, ProjectResponseDTO.class);
	}

	@Override
	public ProjectResponseDTO getProjectById(UUID id) {
		return modelMapper.map(
				projectRepository.findById(id)
						.orElseThrow(() -> new RuntimeException(String.format("Project with id: %s not found", id))),
				ProjectResponseDTO.class);
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
		}).collect(Collectors.toList());
	}

}
