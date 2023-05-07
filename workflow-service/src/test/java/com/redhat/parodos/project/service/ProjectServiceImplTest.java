package com.redhat.parodos.project.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.repository.ProjectRepository;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserService;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ProjectServiceImplTest {

	private ProjectRepository projectRepository;

	private WorkFlowRepository workFlowRepository;

	private ProjectServiceImpl projectService;

	private UserService userService;

	@BeforeEach
	public void initEach() {
		this.projectRepository = mock(ProjectRepository.class);
		this.workFlowRepository = mock(WorkFlowRepository.class);
		this.userService = mock(UserService.class);
		this.projectService = new ProjectServiceImpl(this.projectRepository, this.workFlowRepository, userService,
				new ModelMapper());
	}

	private Project getSampleProject(String name) {
		Project project = Project.builder().name(name).description("test description").createDate(new Date())
				.modifyDate(new Date()).build();
		project.setId(UUID.randomUUID());
		return project;
	}

	@Test
	public void testFindProjectByIdWithValidData() {
		// given
		Project project = getSampleProject("test");
		when(this.projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

		// when
		ProjectResponseDTO res = this.projectService.getProjectById(project.getId());

		// then
		assertNotNull(res);
		assertEquals(res.getId().toString(), project.getId().toString());
	}

	@Test
	public void testFindProjectByIdAndUserNameWithValidData() {
		String username = "test-user";
		// given
		Project project = getSampleProject("test");
		when(this.projectRepository.findByIdAndUserUsername(project.getId(), username))
				.thenReturn(Optional.of(project));

		// when
		ProjectResponseDTO res = this.projectService.getProjectByIdAndUsername(project.getId(), username);

		// then
		assertNotNull(res);
		assertEquals(res.getId().toString(), project.getId().toString());
	}

	@Test
	public void testFindProjectByIdWithInvalidData() {
		// given
		Project project = getSampleProject("test");
		when(this.projectRepository.findById(project.getId())).thenReturn(Optional.empty());

		// when
		assertThrows(ResponseStatusException.class, () -> this.projectService.getProjectById(project.getId()),
				String.format("404 NOT_FOUND \"Project with id: %s not found\"", project.getId()));
	}

	@Test
	public void testGetProjectsWithValidData() {
		// project one
		UUID projectIdOne = UUID.randomUUID();

		Project projectOne = Project.builder().name("projectOne").description("project one description")
				.createDate(new Date()).modifyDate(new Date()).build();
		projectOne.setId(projectIdOne);

		// project two
		UUID projectIdTwo = UUID.randomUUID();

		Project projectTwo = Project.builder().name("projectTwo").description("project two description")
				.createDate(new Date()).modifyDate(new Date()).build();
		projectTwo.setId(projectIdTwo);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().projectId(projectIdTwo)
				.status(WorkStatus.COMPLETED).mainWorkFlowExecution(null).build();

		// given
		when(this.workFlowRepository
				.findFirstByProjectIdAndMainWorkFlowExecutionIsNullOrderByStartDateDesc(eq(projectIdOne)))
						.thenReturn(null);

		when(this.workFlowRepository
				.findFirstByProjectIdAndMainWorkFlowExecutionIsNullOrderByStartDateDesc(eq(projectIdTwo)))
						.thenReturn(workFlowExecution);

		when(this.projectRepository.findAll()).thenReturn(Arrays.asList(projectOne, projectTwo));

		// when
		List<ProjectResponseDTO> res = this.projectService.getProjects();

		// then
		assertNotNull(res);
		assertEquals(res.size(), 2);
		assertEquals(res.get(0).getName(), "projectOne");
		assertTrue(res.get(0).getStatus().isEmpty());
		assertEquals(res.get(1).getName(), "projectTwo");
		assertEquals(res.get(1).getStatus(), "COMPLETED");
	}

	@Test
	public void testGetProjectsWithInvalidData() {
		// given
		when(this.projectRepository.findAll()).thenReturn(new ArrayList<Project>());

		// when
		List<ProjectResponseDTO> res = this.projectService.getProjects();

		// then
		assertNotNull(res);
		assertEquals(res.size(), 0);
	}

	@Test
	@WithMockUser(username = "test-user")
	public void testSaveWithValidData() {
		// given
		Project project = getSampleProject("test");
		when(this.projectRepository.save(any(Project.class))).thenReturn(project);
		when(userService.getUserEntityByUsername(nullable(String.class)))
				.thenReturn(User.builder().username("test-user").build());
		ProjectRequestDTO projectDTO = ProjectRequestDTO.builder().name("dto").description("dto description").build();

		// when
		ProjectResponseDTO res = this.projectService.save(projectDTO);

		// then
		ArgumentCaptor<Project> argument = ArgumentCaptor.forClass(Project.class);
		verify(this.projectRepository, times(1)).save(argument.capture());

		assertEquals(argument.getValue().getDescription(), "dto description");
		assertEquals(argument.getValue().getName(), "dto");

		assertNotNull(res);
		assertEquals(res.getId(), project.getId());
	}

}
