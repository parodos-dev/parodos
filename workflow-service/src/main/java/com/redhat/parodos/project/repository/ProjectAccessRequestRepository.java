package com.redhat.parodos.project.repository;

import java.util.UUID;

import com.redhat.parodos.project.entity.ProjectAccessRequest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAccessRequestRepository extends JpaRepository<ProjectAccessRequest, UUID> {

}
