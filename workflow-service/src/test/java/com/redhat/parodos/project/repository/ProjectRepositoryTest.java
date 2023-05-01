package com.redhat.parodos.project.repository;

import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.repository.RepositoryTestBase;
import com.redhat.parodos.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
		createProject(UUID.randomUUID().toString());
		createProject(UUID.randomUUID().toString());

		// when
		List<Project> projects = projectRepository.findAll();

		// then
		assertNotNull(projects);
		assertEquals(2, projects.size());
	}

	@Test
	public void testFindByName() {
		// given
		var name = UUID.randomUUID().toString();
		createProject(name);
		createProject(UUID.randomUUID().toString());

		// when
		var project = projectRepository.findByNameIgnoreCase(name);

		// then
		assertNotNull(project);
		assertTrue(project.isPresent());
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
		final var name = UUID.randomUUID().toString();
		assertEquals(0, projectRepository.count());

		// when
		createProject(name);
		assertEquals(1, projectRepository.count());

		// then
		assertThrows(PersistenceException.class, () -> createProject(name));
	}

	@Test
	void testFindAllByUsername() {
		// given
		Project project = createProject("test-project", "test-user");

		// then
		assertThat(projectRepository.findAllByUserUsername("test-user")).hasSize(1).contains(project);
	}

	@Test
	void testFindByIdAndUsername() {
		// given
		Project project = createProject("test-project", "test-user");

		// then
		assertThat(projectRepository.findByIdAndUserUsername(project.getId(), "test-user")).hasValue(project);
	}

	private Project createProject(String name, String username) {
		User user = User.builder().username(username).build();
		entityManager.persistAndFlush(user);

		Project project = Project.builder().name(name).description(name + " test").user(user).build();
		return entityManager.persistAndFlush(project);
	}

	private void createProject(String name) {
		Project project = Project.builder().name(name).description(name + " test").build();
		entityManager.persistAndFlush(project);
	}

}
