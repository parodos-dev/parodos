package com.redhat.parodos.project.controller;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.common.exceptions.OperationDeniedException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.project.dto.request.AccessRequestDTO;
import com.redhat.parodos.project.dto.request.ProjectRequestDTO;
import com.redhat.parodos.project.dto.request.UserRoleRequestDTO;
import com.redhat.parodos.project.dto.response.AccessResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectUserRoleResponseDTO;
import com.redhat.parodos.project.enums.Role;
import com.redhat.parodos.project.service.ProjectServiceImpl;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DirtiesContext
public class ProjectControllerTest extends ControllerMockClient {

	private static final String TEST_ROLE_DEVELOPER = "DEVELOPER";

	private static final String TEST_USERNAME = "test-username";

	private static final String TEST_PROJECT_NAME_1 = "test-project-1";

	private static final String TEST_PROJECT_NAME_2 = "test-project-2";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProjectServiceImpl projectService;

	@Test
	public void testCreateValidProject() throws Exception {
		// given
		ProjectRequestDTO project1DTO = new ProjectRequestDTO();
		project1DTO.setName(TEST_PROJECT_NAME_1);

		String jsonPayload = objectMapper.writeValueAsString(project1DTO);

		ProjectResponseDTO response = createSampleProject(TEST_PROJECT_NAME_1);

		when(projectService.createProject(eq(project1DTO))).thenReturn(response);

		// When
		mockMvc.perform(this.postRequestWithValidCredentials("/api/v1/projects").content(jsonPayload)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(response.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(response.getName())));

		// Then
		verify(projectService, times(1)).createProject(any());
	}

	@Test
	public void testCreateValidProjectWithInvalidAuth() throws Exception {
		// given
		ProjectRequestDTO project1DTO = new ProjectRequestDTO();
		project1DTO.setName(TEST_PROJECT_NAME_1);
		String jsonPayload = objectMapper.writeValueAsString(project1DTO);
		// When
		mockMvc.perform(this.postRequestWithInValidCredentials("/api/v1/projects/").content(jsonPayload)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// Then
		verify(projectService, never()).createProject(any());
	}

	@Test
	public void testListProjects() throws Exception {
		ProjectResponseDTO project1DTO = createSampleProject(TEST_PROJECT_NAME_1);
		ProjectResponseDTO project2DTO = createSampleProject(TEST_PROJECT_NAME_2);
		when(projectService.getProjects()).thenReturn(List.of(project1DTO, project2DTO));

		// When
		mockMvc.perform(this.getRequestWithValidCredentials("/api/v1/projects"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(project1DTO.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(project1DTO.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(project2DTO.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is(project2DTO.getName())));

		// Then
		verify(projectService, times(1)).getProjects();
	}

	@Test
	public void testListProjects_when_etagEquals_then_return_notModified() throws Exception {
		ProjectResponseDTO project1DTO = createSampleProject(TEST_PROJECT_NAME_1);
		ProjectResponseDTO project2DTO = createSampleProject(TEST_PROJECT_NAME_2);
		when(projectService.getProjects()).thenReturn(List.of(project1DTO, project2DTO));
		String etagValue = String.valueOf(List.of(project1DTO, project2DTO).hashCode());
		// When
		mockMvc.perform(this.getRequestWithValidCredentials("/api/v1/projects").header("If-None-Match",
				"\"" + etagValue + "\"")).andExpect(MockMvcResultMatchers.status().isNotModified());

		// Then
		verify(projectService, times(1)).getProjects();
	}

	@Test
	public void testGetProjectsWithInvalidCredentials() throws Exception {
		// When
		mockMvc.perform(this.getRequestWithInValidCredentials("/api/v1/projects/"))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// Then
		verify(projectService, never()).getProjects();
	}

	@Test
	public void testGetProjectByIdWithValidID() throws Exception {
		ProjectResponseDTO project1DTO = createSampleProject(TEST_PROJECT_NAME_1);
		when(projectService.getProjectById(project1DTO.getId())).thenReturn(project1DTO);

		// When
		mockMvc.perform(this.getRequestWithValidCredentials(String.format("/api/v1/projects/%s", project1DTO.getId())))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(project1DTO.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(project1DTO.getName())));

		// Then
		verify(projectService, times(1)).getProjectById(any());
	}

	@Test
	public void testGetProjectByIdWithInvalidID() throws Exception {
		when(projectService.getProjectById(any())).thenThrow(new ResourceNotFoundException("Project not found"));

		// When
		mockMvc.perform(this.getRequestWithValidCredentials(String.format("/api/v1/projects/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
		// Then
		verify(projectService, times(1)).getProjectById(any());
	}

	@Test
	public void testGetProjectbyIdWithValidIDWithoutRightCredentials() throws Exception {
		// When
		mockMvc.perform(this.getRequestWithInValidCredentials(String.format("/api/v1/projects/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// Then
		verify(projectService, never()).getProjectById(any());

	}

	@Test
	public void testUpdateUserRolesToProject() throws Exception {
		List<UserRoleRequestDTO> userRoleRequestDTOList = List
				.of(UserRoleRequestDTO.builder().username("test-user").build());
		ProjectUserRoleResponseDTO projectUserRoleResponseDTO = ProjectUserRoleResponseDTO.builder()
				.projectName("test-project").build();
		when(projectService.updateUserRolesToProject(any(), any())).thenReturn(projectUserRoleResponseDTO);

		// When
		mockMvc.perform(
				this.postRequestWithValidCredentials(String.format("/api/v1/projects/%s/users", UUID.randomUUID()))
						.content(JSONObject.valueToString(userRoleRequestDTOList)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$").value(projectUserRoleResponseDTO));
		// Then
		verify(projectService, times(1)).updateUserRolesToProject(any(), any());
	}

	@Test
	public void testRemoveUsersFromProject() throws Exception {
		List<String> users = List.of("test-user");
		ProjectUserRoleResponseDTO projectUserRoleResponseDTO = ProjectUserRoleResponseDTO.builder()
				.projectName("test-project").build();
		when(projectService.removeUsersFromProject(any(), any())).thenReturn(projectUserRoleResponseDTO);

		// When
		mockMvc.perform(
				this.deleteRequestWithValidCredentials(String.format("/api/v1/projects/%s/users", UUID.randomUUID()))
						.content(JSONObject.valueToString(users)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$").value(projectUserRoleResponseDTO));
		// Then
		verify(projectService, times(1)).removeUsersFromProject(any(), any());
	}

	@Test
	public void testRemoveUsersFromProject_WithEmptyUsersList() throws Exception {
		// Given
		List<String> users = List.of();
		ProjectUserRoleResponseDTO projectUserRoleResponseDTO = ProjectUserRoleResponseDTO.builder()
				.projectName("test-project").build();

		// When
		mockMvc.perform(
				this.deleteRequestWithValidCredentials(String.format("/api/v1/projects/%s/users", UUID.randomUUID()))
						.content(JSONObject.valueToString(users)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
		// Then
		verify(projectService, times(0)).removeUsersFromProject(any(), any());
	}

	@Test
	public void testCreateAccessRequestToProject() throws Exception {
		// given
		UUID projectId = UUID.randomUUID();
		AccessRequestDTO accessRequestDTO = AccessRequestDTO.builder().username(TEST_USERNAME)
				.role(Role.valueOf(TEST_ROLE_DEVELOPER)).build();
		AccessResponseDTO accessResponseDTO = AccessResponseDTO.builder().accessRequestId(UUID.randomUUID())
				.project(AccessResponseDTO.ProjectDTO.builder().id(projectId).build()).build();

		// When
		when(projectService.createAccessRequestToProject(eq(projectId), eq(accessRequestDTO)))
				.thenReturn(accessResponseDTO);

		// Then
		String jsonPayload = objectMapper.writeValueAsString(accessRequestDTO);
		mockMvc.perform(this.postRequestWithValidCredentials(String.format("/api/v1/projects/%s/access", projectId))
				.content(jsonPayload)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessRequestId",
						Matchers.is(accessResponseDTO.getAccessRequestId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.project.id",
						Matchers.is(accessResponseDTO.getProject().getId().toString())));

		verify(projectService, times(1)).createAccessRequestToProject(eq(projectId), eq(accessRequestDTO));
	}

	@Test
	public void testCreateAccessRequestToProjectWhenNotFoundProjectOrRoleOrUser() throws Exception {
		// given
		UUID projectId = UUID.randomUUID();
		AccessRequestDTO accessRequestDTO = AccessRequestDTO.builder().username(TEST_USERNAME)
				.role(Role.valueOf(TEST_ROLE_DEVELOPER)).build();

		// When
		when(projectService.createAccessRequestToProject(eq(projectId), eq(accessRequestDTO)))
				.thenThrow(ResourceNotFoundException.class);

		String jsonPayload = objectMapper.writeValueAsString(accessRequestDTO);
		mockMvc.perform(this.postRequestWithValidCredentials(String.format("/api/v1/projects/%s/access", projectId))
				.content(jsonPayload)).andExpect(MockMvcResultMatchers.status().isNotFound());

		// Then
		verify(projectService, times(1)).createAccessRequestToProject(eq(projectId), eq(accessRequestDTO));
	}

	@Test
	public void testCreateAccessRequestToProjectWhenUserAlreadyAssignedToProject() throws Exception {
		// given
		UUID projectId = UUID.randomUUID();
		AccessRequestDTO accessRequestDTO = AccessRequestDTO.builder().username(TEST_USERNAME)
				.role(Role.valueOf(TEST_ROLE_DEVELOPER)).build();

		// When
		when(projectService.createAccessRequestToProject(eq(projectId), eq(accessRequestDTO)))
				.thenThrow(OperationDeniedException.class);

		String jsonPayload = objectMapper.writeValueAsString(accessRequestDTO);
		mockMvc.perform(this.postRequestWithValidCredentials(String.format("/api/v1/projects/%s/access", projectId))
				.content(jsonPayload)).andExpect(MockMvcResultMatchers.status().isBadRequest());

		// Then
		verify(projectService, times(1)).createAccessRequestToProject(eq(projectId), eq(accessRequestDTO));
	}

	ProjectResponseDTO createSampleProject(String name) {
		ProjectResponseDTO responseDTO = new ProjectResponseDTO();
		responseDTO.setId(UUID.randomUUID());
		responseDTO.setName(name);
		return responseDTO;
	}

}
