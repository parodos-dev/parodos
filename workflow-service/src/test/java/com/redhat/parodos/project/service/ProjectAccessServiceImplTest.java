package com.redhat.parodos.project.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.OperationDeniedException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.project.dto.request.AccessStatusRequestDTO;
import com.redhat.parodos.project.dto.response.AccessStatusResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectAccessRequestDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.entity.ProjectAccessRequest;
import com.redhat.parodos.project.entity.ProjectUserRole;
import com.redhat.parodos.project.entity.Role;
import com.redhat.parodos.project.enums.ProjectAccessStatus;
import com.redhat.parodos.project.repository.ProjectAccessRequestRepository;
import com.redhat.parodos.project.repository.ProjectUserRoleRepository;
import com.redhat.parodos.project.repository.RoleRepository;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ProjectAccessServiceImplTest {

	private final static String TEST_USERNAME = "test-username";

	private final static String TEST_USERNAME_ADMIN = "test-username-admin";

	private final static String TEST_USERNAME_DEVELOPER = "test-username-developer";

	private final static String TEST_PROJECT_NAME = "test-project-name";

	private final static String TEST_PROJECT_DESCRIPTION = "test-project-description";

	private final static UUID TEST_USER_ID = UUID.randomUUID();

	private final static UUID TEST_USER_ADMIN_ID = UUID.randomUUID();

	private final static UUID TEST_USER_DEVELOPER_ID = UUID.randomUUID();

	private final static String TEST_ROLE_ADMIN_NAME = com.redhat.parodos.project.enums.Role.OWNER.name();

	private final static String TEST_ROLE_DEVELOPER_NAME = com.redhat.parodos.project.enums.Role.DEVELOPER.name();

	private final static UUID TEST_ACCESS_REQUEST_ID = UUID.randomUUID();

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private ProjectUserRoleRepository projectUserRoleRepository;

	@Mock
	private ProjectAccessRequestRepository projectAccessRequestRepository;

	@Mock
	private UserServiceImpl userService;

	@Mock
	private ProjectServiceImpl projectService;

	private ProjectAccessServiceImpl projectAccessService;

	@BeforeEach
	public void initEach() {
		this.projectAccessService = new ProjectAccessServiceImpl(this.userService, this.projectService,
				this.projectAccessRequestRepository, this.projectUserRoleRepository, this.roleRepository);
	}

	@Test
	void getProjectAccessStatusById() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		Role role = getRole(TEST_ROLE_DEVELOPER_NAME);
		ProjectAccessRequest projectAccessRequest = getProjectAccessRequest(TEST_ACCESS_REQUEST_ID, project, user,
				role);

		// when
		when(this.projectAccessRequestRepository.findById(ArgumentMatchers.eq(TEST_ACCESS_REQUEST_ID)))
				.thenReturn(Optional.of(projectAccessRequest));

		AccessStatusResponseDTO accessStatusResponseDTO = this.projectAccessService
				.getProjectAccessStatusById(projectAccessRequest.getId());

		// then
		assertNotNull(accessStatusResponseDTO);
		assertEquals(accessStatusResponseDTO.getAccessRequestId(), projectAccessRequest.getId());
		assertEquals(accessStatusResponseDTO.getStatus(), ProjectAccessStatus.PENDING);
	}

	@Test
	void getProjectAccessStatusByIdWhenIdNotFound() {
		// when
		doThrow(ResourceNotFoundException.class).when(projectAccessRequestRepository)
				.findById(ArgumentMatchers.eq(TEST_ACCESS_REQUEST_ID));

		// then
		assertThrows(ResourceNotFoundException.class, () -> {
			projectAccessService.getProjectAccessStatusById(TEST_ACCESS_REQUEST_ID);
		});
	}

	@Test
	@WithMockUser(username = TEST_USERNAME_ADMIN)
	void updateProjectAccessStatusById() {
		// given
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_DEVELOPER_NAME);
		ProjectAccessRequest projectAccessRequest = getProjectAccessRequest(TEST_ACCESS_REQUEST_ID, project, user,
				role);

		// updating user
		User userAdmin = getUser(TEST_USER_ADMIN_ID, TEST_USERNAME_ADMIN);
		Role roleAdmin = getRole(TEST_ROLE_ADMIN_NAME);
		ProjectUserRole projectUserRoleAdmin = ProjectUserRole.builder().project(project).user(userAdmin)
				.role(roleAdmin).build();

		// when
		when(projectAccessRequestRepository.findById(ArgumentMatchers.eq(TEST_ACCESS_REQUEST_ID)))
				.thenReturn(Optional.of(projectAccessRequest));
		when(projectUserRoleRepository.findByProjectIdAndUserId(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(Collections.singletonList(projectUserRoleAdmin));
		when(userService.getUserEntityByUsername(ArgumentMatchers.anyString())).thenReturn(userAdmin);
		AccessStatusRequestDTO accessStatusRequestDTO = AccessStatusRequestDTO.builder()
				.status(ProjectAccessStatus.APPROVED).build();

		projectAccessService.updateProjectAccessStatusById(TEST_ACCESS_REQUEST_ID, accessStatusRequestDTO);

		ArgumentCaptor<ProjectAccessRequest> projectAccessRequestArgumentCaptor = ArgumentCaptor
				.forClass(ProjectAccessRequest.class);
		verify(projectAccessRequestRepository, times(1)).save(projectAccessRequestArgumentCaptor.capture());
		assertEquals(projectAccessRequestArgumentCaptor.getValue().getStatus(), ProjectAccessStatus.APPROVED);
		assertEquals(projectAccessRequestArgumentCaptor.getValue().getProject().getId(), project.getId());

		verify(projectService, times(1)).updateUserRolesToProject(any(), any());
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void updateProjectAccessStatusByIdWhenUserIsNotAdminOrOwner() {
		// given
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		User user = getUser(TEST_USER_DEVELOPER_ID, TEST_USERNAME_DEVELOPER);
		Role role = getRole(TEST_ROLE_DEVELOPER_NAME);
		ProjectAccessRequest projectAccessRequest = getProjectAccessRequest(TEST_ACCESS_REQUEST_ID, project, user,
				role);

		// updating user
		User userDeveloper = getUser(TEST_USER_ID, TEST_USERNAME_DEVELOPER);
		Role roleDeveloper = getRole(TEST_ROLE_DEVELOPER_NAME);
		ProjectUserRole projectUserRoleDeveloper = ProjectUserRole.builder().project(project).user(userDeveloper)
				.role(roleDeveloper).build();

		// when
		when(projectAccessRequestRepository.findById(ArgumentMatchers.eq(TEST_ACCESS_REQUEST_ID)))
				.thenReturn(Optional.of(projectAccessRequest));
		when(projectUserRoleRepository.findByProjectIdAndUserId(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(Collections.singletonList(projectUserRoleDeveloper));
		when(userService.getUserEntityByUsername(ArgumentMatchers.anyString())).thenReturn(user);
		AccessStatusRequestDTO accessStatusRequestDTO = AccessStatusRequestDTO.builder()
				.status(ProjectAccessStatus.APPROVED).build();

		// then
		assertThrows(OperationDeniedException.class, () -> {
			projectAccessService.updateProjectAccessStatusById(TEST_ACCESS_REQUEST_ID, accessStatusRequestDTO);
		});
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	void getPendingAccessRequests() {
		// given
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		User user = getUser(TEST_USER_DEVELOPER_ID, TEST_USERNAME_DEVELOPER);
		Role role = getRole(TEST_ROLE_DEVELOPER_NAME);
		ProjectAccessRequest projectAccessRequest = getProjectAccessRequest(TEST_ACCESS_REQUEST_ID, project, user,
				role);

		// when
		when(projectAccessRequestRepository.findAll()).thenReturn(List.of(projectAccessRequest));

		// then
		List<ProjectAccessRequestDTO> projectAccessRequestDTOList = projectAccessService
				.getPendingProjectAccessRequests();

		assertThat(projectAccessRequestDTOList).hasSize(1).satisfies(projectAccessRequestDTO -> {
			assertEquals(TEST_USERNAME_DEVELOPER, projectAccessRequestDTO.get(0).getUsername());
			assertEquals(project.getId(), projectAccessRequestDTO.get(0).getProjectId());
		});
	}

	private Project getProject(String name, String description) {
		Project project = Project.builder().name(name).description(description).build();
		project.setId(UUID.randomUUID());
		project.setCreatedDate(new Date());
		project.setModifiedDate(new Date());
		return project;
	}

	private User getUser(UUID userId, String username) {
		User user = User.builder().username(username).firstName(username).lastName(username)
				.email(String.format("%s@parodos.dev", username)).build();
		user.setId(userId);
		return user;
	}

	private Role getRole(String roleName) {
		return Role.builder().name(roleName).build();
	}

	private ProjectAccessRequest getProjectAccessRequest(UUID accessRequestId, Project project, User user, Role role) {
		return ProjectAccessRequest.builder().id(accessRequestId).project(project).user(user).role(role)
				.status(ProjectAccessStatus.PENDING).build();
	}

}
