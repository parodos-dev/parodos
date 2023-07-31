package com.redhat.parodos.project.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.PersistenceException;

import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.repository.RepositoryTestBase;
import com.redhat.parodos.user.entity.User;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectRepositoryTest extends RepositoryTestBase {

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	public void injectedComponentsAreNotNull() {
		super.injectedComponentsAreNotNull();
		assertNotNull(projectRepository);
	}

	@Test
	public void testFindAll() {
		// given
		User user = createUser(UUID.randomUUID().toString());
		createProject(user, UUID.randomUUID().toString());
		createProject(user, UUID.randomUUID().toString());

		// when
		List<Project> projects = projectRepository.findAll();

		// then
		assertNotNull(projects);
		assertEquals(2, projects.size());
	}

	@Test
	public void testFindByName() {
		// given
		User user = createUser(UUID.randomUUID().toString());
		var name = UUID.randomUUID().toString();
		createProject(user, name);

		// when
		var project = projectRepository.findByNameIgnoreCase(name);

		// then
		assertNotNull(project);
		assertThat(project.isPresent(), is(true));
		assertEquals(name, project.get().getName());
	}

	@Test
	public void testSave() {
		// given
		Project project = Project.builder().name(UUID.randomUUID().toString()).description("test").build();
		List<Project> projects = projectRepository.findAll();
		assertTrue(projects.isEmpty());

		// when
		Project savedProject = projectRepository.save(project);

		// then
		savedProject = projectRepository.getById(savedProject.getId());
		assertNotNull(savedProject);
		assertNotNull(savedProject.getId());
		assertEquals(project.getName(), savedProject.getName());
		assertEquals(project.getDescription(), savedProject.getDescription());
	}

	@Test
	public void testUniqueNameConstraint() {
		// given
		User user = createUser(UUID.randomUUID().toString());
		final var name = UUID.randomUUID().toString();
		assertEquals(0, projectRepository.count());

		// when
		createProject(user, name);
		assertEquals(1, projectRepository.count());

		// then
		assertThrows(PersistenceException.class, () -> createProject(user, name));
	}

	private void createProject(User user, String name) {
		Project project = Project.builder().name(name).description(name + " test").build();
		project.setCreatedDate(new Date());
		project.setCreatedBy(user.getId());
		entityManager.persistAndFlush(project);
	}

	private User createUser(String username) {
		User user = User.builder().username(username).build();
		return entityManager.persistAndFlush(user);
	}

}
