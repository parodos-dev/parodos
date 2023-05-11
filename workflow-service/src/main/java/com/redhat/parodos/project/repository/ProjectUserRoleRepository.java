package com.redhat.parodos.project.repository;

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.project.entity.ProjectUserRole;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectUserRoleRepository extends JpaRepository<ProjectUserRole, ProjectUserRole.Id> {

	List<ProjectUserRole> findByProjectIdAndUserId(UUID projectId, UUID userId);

	List<ProjectUserRole> findByUserId(UUID userId);

}
