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
package com.redhat.parodos.project.controller;

import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.service.ProjectServiceImpl;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Project controller
 *
 * @author Annel Ketcha (Github: anludke)
 */

@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectServiceImpl projectService;

    public ProjectController(ProjectServiceImpl projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO projectResponseDTO = projectService.save(projectRequestDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(projectResponseDTO.getId()).toUri();
        return ResponseEntity.created(location).body(projectResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getProjects() {
        return ResponseEntity.ok(projectService.getProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getProjectById(UUID.fromString(id)));
    }
}
