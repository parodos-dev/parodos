package com.redhat.parodos.project.repository;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.project.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Role entity
 *
 * @author Annel Ketcha (Github: anludke)
 */
public interface RoleRepository extends JpaRepository<Role, UUID> {

	Optional<Role> findByNameIgnoreCase(String name);

}
