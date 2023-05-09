package com.redhat.parodos.project.repository;

import java.util.HashSet;
import java.util.List;

import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.entity.ProjectUserRole;
import com.redhat.parodos.project.entity.Role;
import com.redhat.parodos.repository.RepositoryTestBase;
import com.redhat.parodos.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjectUserRoleRepositoryTest extends RepositoryTestBase {

	private User user;

	private Project project;

	@Autowired
	private ProjectUserRoleRepository projectUserRoleRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	public void injectedComponentsAreNotNull() {
		super.injectedComponentsAreNotNull();
		assertNotNull(projectUserRoleRepository);
	}

	@BeforeEach
	public void init() {
		Role role = roleRepository.findByNameIgnoreCase("developer").get();
		user = entityManager.persistAndFlush(User.builder().username("test-user").build());
		project = entityManager
				.persistAndFlush(Project.builder().name("test-project").projectUserRoles(new HashSet<>()).build());
		createProjectUserRole(user, project, role);
	}

	@Test
	void testFindByProjectIdAndUserId() {
		List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findByProjectIdAndUserId(project.getId(),
				user.getId());

		// then
		assertNotNull(projectUserRoles);
		assertEquals(1, projectUserRoles.size());
	}

	@Test
	void testFindByUserId() {
		// when
		List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findByUserId(user.getId());

		// then
		assertNotNull(projectUserRoles);
		assertEquals(1, projectUserRoles.size());
	}

	@Test
	void testDeleteAllByIdProjectIdAndIdUserIdIn() {
		// when
		List<ProjectUserRole> projectUserRoles = projectUserRoleRepository
				.deleteAllByIdProjectIdAndIdUserIdIn(project.getId(), List.of(user.getId()));

		// then
		assertNotNull(projectUserRoles);
		assertEquals(0, projectUserRoleRepository.findAll().size());
	}

	private ProjectUserRole createProjectUserRole(User user, Project project, Role role) {
		ProjectUserRole projectUserRole = ProjectUserRole.builder().role(role).user(user).project(project)
				.id(ProjectUserRole.Id.builder().roleId(role.getId()).userId(user.getId()).projectId(project.getId())
						.build())
				.build();
		project.getProjectUserRoles().add(projectUserRole);
		return entityManager.persistAndFlush(projectUserRole);
	}

}
