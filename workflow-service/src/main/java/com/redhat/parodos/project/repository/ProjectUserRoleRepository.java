package com.redhat.parodos.project.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.project.entity.ProjectUserRole;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectUserRoleRepository extends JpaRepository<ProjectUserRole, ProjectUserRole.Id> {

	Optional<ProjectUserRole> findByProjectId(UUID projectId);

	List<ProjectUserRole> findByProjectIdAndUserId(UUID projectId, UUID userId);

	List<ProjectUserRole> findByUserId(UUID userId);

	List<ProjectUserRole> deleteAllByIdProjectIdAndIdUserIdIn(UUID projectId, List<UUID> userIds);

}
