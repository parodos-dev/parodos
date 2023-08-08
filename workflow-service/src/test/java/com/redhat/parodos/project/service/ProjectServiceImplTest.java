package com.redhat.parodos.project.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.OperationDeniedException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.project.dto.request.AccessRequestDTO;
import com.redhat.parodos.project.dto.request.ProjectRequestDTO;
import com.redhat.parodos.project.dto.request.UserRoleRequestDTO;
import com.redhat.parodos.project.dto.response.AccessResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectMemberResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectUserRoleResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.entity.ProjectAccessRequest;
import com.redhat.parodos.project.entity.ProjectUserRole;
import com.redhat.parodos.project.entity.Role;
import com.redhat.parodos.project.repository.ProjectAccessRequestRepository;
import com.redhat.parodos.project.repository.ProjectRepository;
import com.redhat.parodos.project.repository.ProjectUserRoleRepository;
import com.redhat.parodos.project.repository.RoleRepository;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ProjectServiceImplTest {

	private final static String TEST_PROJECT_NAME = "test-project-name";

	private final static String TEST_PROJECT_DESCRIPTION = "test-project-description";

	private final static String TEST_PROJECT_ONE_NAME = "test-project-one-name";

	private final static String TEST_PROJECT_ONE_DESCRIPTION = "test-project-one-description";

	private final static String TEST_PROJECT_TWO_NAME = "test-project-two-name";

	private final static String TEST_PROJECT_TWO_DESCRIPTION = "test-project-two-description";

	private final static String TEST_USERNAME = "test-username";

	private final static String TEST_USERNAME_ADMIN = "test-username-admin";

	private final static String TEST_USERNAME_OWNER = "test-username-owner";

	private final static UUID TEST_USER_ID = UUID.randomUUID();

	private final static UUID TEST_USER_ADMIN_ID = UUID.randomUUID();

	private final static UUID TEST_USER_OWNER_ID = UUID.randomUUID();

	private final static String TEST_ROLE_OWNER_NAME = com.redhat.parodos.project.enums.Role.OWNER.name();

	private final static String TEST_ROLE_ADMIN_NAME = com.redhat.parodos.project.enums.Role.ADMIN.name();

	private final static String TEST_ROLE_DEVELOPER_NAME = com.redhat.parodos.project.enums.Role.DEVELOPER.name();

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private ProjectUserRoleRepository projectUserRoleRepository;

	@Mock
	private ProjectAccessRequestRepository projectAccessRequestRepository;

	@Mock
	private UserService userService;

	private ProjectServiceImpl projectService;

	@BeforeEach
	public void initEach() {
		this.projectService = new ProjectServiceImpl(this.projectRepository, this.roleRepository,
				this.projectUserRoleRepository, this.projectAccessRequestRepository, this.userService);
	}

	@Test
	@WithMockUser(username = "test-user")
	public void testGetProjectByIdWithValidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		ProjectUserRole projectUserRole = getProjectUserRole(project, user, role);

		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class))).thenReturn(user);
		when(this.userService.getUserEntityById(eq(user.getId()))).thenReturn(user);
		when(this.projectRepository.findById(eq(project.getId()))).thenReturn(Optional.of(project));
		when(this.projectUserRoleRepository.findByProjectIdAndUserId(eq(project.getId()), eq(user.getId())))
				.thenReturn(List.of(projectUserRole));

		ProjectResponseDTO projectResponseDTO = this.projectService.getProjectById(project.getId());

		// then
		assertNotNull(projectResponseDTO);
		assertEquals(projectResponseDTO.getId(), project.getId());
	}

	@Test
	@WithMockUser(username = "test-user")
	public void testGetProjectByIdAndUsernameWithValidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		ProjectUserRole projectUserRole = getProjectUserRole(project, user, role);

		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class))).thenReturn(user);
		when(this.userService.getUserEntityById(eq(user.getId()))).thenReturn(user);
		when(this.projectRepository.findById(eq(project.getId()))).thenReturn(Optional.of(project));
		when(this.projectUserRoleRepository.findByProjectIdAndUserId(eq(project.getId()), eq(TEST_USER_ID)))
				.thenReturn(List.of(projectUserRole));

		List<ProjectResponseDTO> projectResponseDTOs = this.projectService.getProjectByIdAndUserId(project.getId(),
				TEST_USER_ID);

		// then
		assertNotNull(projectResponseDTOs);
		assertEquals(1, projectResponseDTOs.size());
		assertEquals(projectResponseDTOs.get(0).getId(), project.getId());
	}

	@Test
	@WithMockUser(username = "test-user")
	public void testFindProjectByIdWithInvalidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);

		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class))).thenReturn(user);
		when(this.projectRepository.findById(eq(project.getId()))).thenReturn(Optional.of(project));
		when(this.projectRepository.findById(eq(project.getId()))).thenReturn(Optional.empty());

		// then
		assertThrows(ResourceNotFoundException.class, () -> this.projectService.getProjectById(project.getId()),
				String.format("404 NOT_FOUND \"Project with id: %s not found\"", project.getId()));
	}

	@Test
	@WithMockUser(username = "test-user")
	public void testGetProjectsWithValidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project projectOne = getProject(user.getId(), TEST_PROJECT_ONE_NAME, TEST_PROJECT_ONE_DESCRIPTION);
		Project projectTwo = getProject(user.getId(), TEST_PROJECT_TWO_NAME, TEST_PROJECT_TWO_DESCRIPTION);

		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class))).thenReturn(user);
		when(this.userService.getUserEntityById(eq(user.getId()))).thenReturn(user);
		when(this.projectRepository.findAll()).thenReturn(List.of(projectOne, projectTwo));
		when(this.projectRepository.findById(eq(projectOne.getId()))).thenReturn(Optional.of(projectOne));
		when(this.projectRepository.findById(eq(projectTwo.getId()))).thenReturn(Optional.of(projectTwo));
		when(this.projectUserRoleRepository.findByUserId(TEST_USER_ID)).thenReturn(
				List.of(getProjectUserRole(projectOne, user, role), getProjectUserRole(projectTwo, user, role)));

		List<ProjectResponseDTO> projectResponseDTOs = this.projectService.getProjects();

		// then
		assertNotNull(projectResponseDTOs);
		assertEquals(projectResponseDTOs.size(), 2);
		assertEquals(projectResponseDTOs.get(0).getName(), TEST_PROJECT_ONE_NAME);
		assertEquals(projectResponseDTOs.get(1).getName(), TEST_PROJECT_TWO_NAME);
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	public void testGetProjectsWithInvalidData() {
		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class)))
				.thenReturn(getUser(TEST_USER_ID, TEST_USERNAME));
		when(this.projectUserRoleRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

		List<ProjectResponseDTO> projectResponseDTOs = this.projectService.getProjects();

		// then
		assertNotNull(projectResponseDTOs);
		assertEquals(projectResponseDTOs.size(), 0);
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	public void testSaveWithValidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		User user2 = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		ProjectUserRole projectUserRole = ProjectUserRole.builder().project(project).user(user).role(role).build();
		ProjectRequestDTO projectRequestDTO = ProjectRequestDTO.builder().name(TEST_PROJECT_NAME)
				.description(TEST_PROJECT_DESCRIPTION).build();

		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class))).thenReturn(user);
		when(this.userService.getUserEntityById(eq(user.getId()))).thenReturn(user);
		when(this.roleRepository.findByNameIgnoreCase(nullable(String.class))).thenReturn(Optional.ofNullable(role));
		when(this.projectRepository.save(any(Project.class))).thenReturn(project);
		when(this.projectUserRoleRepository.save(any(ProjectUserRole.class))).thenReturn(projectUserRole);

		ProjectResponseDTO projectResponseDTO = this.projectService.createProject(projectRequestDTO);

		// then
		ArgumentCaptor<Project> projectArgumentCaptor = ArgumentCaptor.forClass(Project.class);
		verify(this.projectRepository, times(1)).save(projectArgumentCaptor.capture());

		ArgumentCaptor<ProjectUserRole> projectUserRoleArgumentCaptor = ArgumentCaptor.forClass(ProjectUserRole.class);
		verify(this.projectUserRoleRepository, times(1)).save(projectUserRoleArgumentCaptor.capture());

		assertEquals(projectArgumentCaptor.getValue().getDescription(), TEST_PROJECT_DESCRIPTION);
		assertEquals(projectArgumentCaptor.getValue().getName(), TEST_PROJECT_NAME);
		assertNotNull(projectResponseDTO);
		assertEquals(projectResponseDTO.getId(), project.getId());
	}

	@Test
	void testUpdateUserRolesToProject() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		ProjectUserRole projectUserRole = ProjectUserRole.builder().project(project).user(user).role(role).build();

		// when
		when(projectRepository.findById(any())).thenReturn(Optional.of(project));
		when(userService.getUserEntityByUsername(anyString())).thenReturn(user);
		when(roleRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(role));
		when(projectUserRoleRepository.deleteAllByIdProjectIdAndIdUserIdIn(any(), any()))
				.thenReturn(List.of(projectUserRole));
		when(projectUserRoleRepository.saveAll(any())).thenReturn(List.of(projectUserRole));

		ProjectUserRoleResponseDTO projectUserRoleResponseDTO = projectService.updateUserRolesToProject(project.getId(),
				List.of(UserRoleRequestDTO.builder().roles(List.of(com.redhat.parodos.project.enums.Role.OWNER))
						.username(user.getUsername()).build()));

		assertThat(projectUserRoleResponseDTO.getProjectName(), equalTo(project.getName()));
		assertThat(projectUserRoleResponseDTO.getUserResponseDTOList(), hasSize(1));
		assertThat(projectUserRoleResponseDTO.getUserResponseDTOList().get(0).getRoles().toArray()[0],
				equalTo(com.redhat.parodos.project.enums.Role.valueOf(role.getName())));
	}

	@Test
	void testRemoveUsersFromProject() {
		// given
		String testUser = "TEST_USER_2";
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		User user2 = getUser(TEST_USER_ID, testUser);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		ProjectUserRole projectUserRole = ProjectUserRole.builder().project(project).user(user).role(role).build();
		ProjectUserRole projectUserRole2 = ProjectUserRole.builder().project(project).user(user2).role(role).build();
		project.setProjectUserRoles(new HashSet<>(List.of(projectUserRole, projectUserRole2)));

		// when
		when(projectRepository.findById(any())).thenReturn(Optional.of(project));
		when(projectUserRoleRepository.deleteAllByIdProjectIdAndIdUserIdIn(any(), any()))
				.thenReturn(List.of(projectUserRole));

		ProjectUserRoleResponseDTO projectUserRoleResponseDTO = projectService.removeUsersFromProject(project.getId(),
				List.of(TEST_USERNAME));

		assertThat(projectUserRoleResponseDTO.getProjectName(), equalTo(project.getName()));

		assertThat(projectUserRoleResponseDTO.getUserResponseDTOList(), hasSize(1));
		assertThat(projectUserRoleResponseDTO.getUserResponseDTOList().get(0).getRoles().toArray()[0],
				equalTo(com.redhat.parodos.project.enums.Role.valueOf(role.getName())));
		assertThat(projectUserRoleResponseDTO.getUserResponseDTOList().get(0).getUsername(), equalTo(testUser));
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void testCreateAccessRequestToProject() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		Role roleDeveloper = getRole(TEST_ROLE_DEVELOPER_NAME);
		ProjectAccessRequest projectAccessRequest = ProjectAccessRequest.builder().project(project).user(user)
				.role(roleDeveloper).build();

		User projectAdmin = getUser(TEST_USER_ADMIN_ID, TEST_USERNAME_ADMIN);
		Role roleAdmin = getRole(TEST_ROLE_ADMIN_NAME);

		User projectOwner = getUser(TEST_USER_OWNER_ID, TEST_USERNAME_OWNER);
		ProjectUserRole projectUserRoleAdmin = ProjectUserRole.builder().project(project).user(projectAdmin)
				.role(roleAdmin).build();

		// when
		when(projectRepository.findById(any())).thenReturn(Optional.of(project));
		when(userService.getUserEntityByUsername(anyString())).thenReturn(user);
		when(userService.getUserEntityById(any())).thenReturn(projectOwner);
		when(roleRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(roleDeveloper));
		when(projectUserRoleRepository.findByProjectIdAndUserIdAndRoleId(any(), any(), any()))
				.thenReturn(Optional.empty());
		when(projectUserRoleRepository.findByProjectIdAndRoleId(any(), any()))
				.thenReturn(List.of(projectUserRoleAdmin));
		when(projectAccessRequestRepository.save(any(ProjectAccessRequest.class))).thenReturn(projectAccessRequest);

		// then
		AccessResponseDTO accessResponseDTO = projectService.createAccessRequestToProject(project.getId(),
				AccessRequestDTO.builder().role(com.redhat.parodos.project.enums.Role.DEVELOPER).build());

		assertEquals(project.getId(), accessResponseDTO.getProject().getId());
		assertEquals(project.getName(), accessResponseDTO.getProject().getName());

		assertThat(accessResponseDTO.getApprovalSentTo(), hasSize(1));
		assertThat(accessResponseDTO.getApprovalSentTo().get(0), equalTo(projectAdmin.getUsername()));
		assertEquals(projectOwner.getUsername(), accessResponseDTO.getEscalationSentTo());
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void testCreateAccessRequestToProjectWhenUserAlreadyAssignedInProject() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Project project = getProject(user.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		Role roleDeveloper = getRole(TEST_ROLE_DEVELOPER_NAME);
		ProjectUserRole projectUserRole = ProjectUserRole.builder().project(project).user(user).role(roleDeveloper)
				.build();

		// when
		when(projectRepository.findById(any())).thenReturn(Optional.of(project));
		when(userService.getUserEntityByUsername(anyString())).thenReturn(user);
		when(roleRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(roleDeveloper));
		when(projectUserRoleRepository.findByProjectIdAndUserIdAndRoleId(any(), any(), any()))
				.thenReturn(Optional.of(projectUserRole));

		// then
		assertThrows(OperationDeniedException.class, () -> {
			projectService.createAccessRequestToProject(project.getId(),
					AccessRequestDTO.builder().role(com.redhat.parodos.project.enums.Role.DEVELOPER).build());
			;
		});

		verify(projectUserRoleRepository, never()).findByProjectIdAndRoleId(any(), any());
		verify(projectAccessRequestRepository, never()).save(any());
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void testCreateAccessRequestToProjectWhenProjectNotFound() {
		// when
		when(projectRepository.findById(any())).thenReturn(Optional.empty());

		// then
		assertThrows(ResourceNotFoundException.class, () -> {
			projectService.createAccessRequestToProject(UUID.randomUUID(),
					AccessRequestDTO.builder().role(com.redhat.parodos.project.enums.Role.DEVELOPER).build());
			;
		});

		verify(projectRepository, times(1)).findById(any());
		verify(userService, never()).getUserEntityByUsername(any());
		verify(roleRepository, never()).findByNameIgnoreCase(any());
		verify(projectUserRoleRepository, never()).findByProjectIdAndRoleId(any(), any());
		verify(projectAccessRequestRepository, never()).save(any());
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void testCreateAccessRequestToProjectWhenUserNotFound() {
		// given
		Project project = getProject(UUID.randomUUID(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);

		// when
		when(projectRepository.findById(any())).thenReturn(Optional.of(project));
		when(userService.getUserEntityByUsername(anyString())).thenThrow(new ResourceNotFoundException(""));

		// then
		assertThrows(ResourceNotFoundException.class, () -> {
			projectService.createAccessRequestToProject(UUID.randomUUID(),
					AccessRequestDTO.builder().role(com.redhat.parodos.project.enums.Role.DEVELOPER).build());
			;
		});

		verify(projectRepository, times(1)).findById(any());
		verify(userService, times(1)).getUserEntityByUsername(any());
		verify(roleRepository, never()).findByNameIgnoreCase(any());
		verify(projectUserRoleRepository, never()).findByProjectIdAndRoleId(any(), any());
		verify(projectAccessRequestRepository, never()).save(any());
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void testGetProjectMembersById() {
		// given
		User userOne = getUser(TEST_USER_OWNER_ID, TEST_USERNAME_OWNER);
		Role roleOwner = getRole(TEST_ROLE_OWNER_NAME);

		User userTwo = getUser(TEST_USER_ADMIN_ID, TEST_USERNAME_ADMIN);
		Role roleAdmin = getRole(TEST_ROLE_ADMIN_NAME);
		Role roleDeveloper = getRole(TEST_ROLE_DEVELOPER_NAME);

		Project project = getProject(userOne.getId(), TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);

		ProjectUserRole projectUserRole1 = ProjectUserRole.builder().project(project).user(userOne).role(roleOwner)
				.build();
		ProjectUserRole projectUserRole2 = ProjectUserRole.builder().project(project).user(userTwo).role(roleAdmin)
				.build();
		ProjectUserRole projectUserRole3 = ProjectUserRole.builder().project(project).user(userTwo).role(roleDeveloper)
				.build();
		project.setProjectUserRoles(new HashSet<>(List.of(projectUserRole1, projectUserRole2, projectUserRole3)));

		// when
		when(projectUserRoleRepository.findByProjectId(eq(project.getId())))
				.thenReturn(List.of(projectUserRole1, projectUserRole2, projectUserRole3));

		List<ProjectMemberResponseDTO> projectMemberResponseDTOs = this.projectService
				.getProjectMembersById(project.getId());

		// then
		assertEquals(2, projectMemberResponseDTOs.size());
		// test-username-admin
		assertEquals(projectMemberResponseDTOs.get(0).getUsername(), userTwo.getUsername());
		assertEquals(projectMemberResponseDTOs.get(0).getRoles().toArray()[0], roleAdmin.getName());
		assertEquals(projectMemberResponseDTOs.get(0).getRoles().toArray()[1], roleDeveloper.getName());
		// test-username-owner
		assertEquals(projectMemberResponseDTOs.get(1).getUsername(), userOne.getUsername());
		assertEquals(projectMemberResponseDTOs.get(1).getRoles().toArray()[0], roleOwner.getName());
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void testGetProjectMembersByIdWhenNoProjectUserRoleFound() {
		// when
		when(projectUserRoleRepository.findByProjectId(any())).thenReturn(Collections.emptyList());

		// then
		assertThrows(ResourceNotFoundException.class,
				() -> this.projectService.getProjectMembersById(UUID.randomUUID()));
	}

	private Project getProject(UUID userId, String name, String description) {
		Project project = Project.builder().name(name).description(description).build();
		project.setId(UUID.randomUUID());
		project.setCreatedBy(userId);
		project.setCreatedDate(new Date());
		project.setModifiedDate(new Date());
		return project;
	}

	private User getUser(UUID userId, String username) {
		User user = User.builder().username(username).firstName(username).lastName(username).build();
		user.setId(userId);
		return user;
	}

	private Role getRole(String roleName) {
		return Role.builder().name(roleName).build();
	}

	private ProjectUserRole getProjectUserRole(Project project, User user, Role role) {
		return ProjectUserRole.builder().project(project).user(user).role(role).build();
	}

}
