package com.redhat.parodos.project.controller;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.service.ProjectServiceImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DirtiesContext
public class ProjectControllerTest extends ControllerMockClient {

	private static final String PROJECT_NAME_1 = "project1";

	private static final String PROJECT_NAME_2 = "project2";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProjectServiceImpl projectService;

	@Test
	public void testCreateValidProject() throws Exception {
		// given
		ProjectRequestDTO project1DTO = new ProjectRequestDTO();
		project1DTO.setName(PROJECT_NAME_1);

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(project1DTO);

		ProjectResponseDTO response = createSampleProject(PROJECT_NAME_1);

		Mockito.when(projectService.save(Mockito.eq(project1DTO))).thenReturn(response);

		// When
		mockMvc.perform(this.postRequestWithValidCredentials("/api/v1/projects/").content(json)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(response.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(response.getName())));

		// Then
		Mockito.verify(projectService, Mockito.times(1)).save(Mockito.any());
	}

	@Test
	public void testCreateValidProjectWithInvalidAuth() throws Exception {
		// given
		ProjectRequestDTO project1DTO = new ProjectRequestDTO();
		project1DTO.setName(PROJECT_NAME_1);
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(project1DTO);
		// When
		mockMvc.perform(this.postRequestWithInValidCredentials("/api/v1/projects/").content(json)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// Then
		Mockito.verify(projectService, Mockito.never()).save(Mockito.any());
	}

	@Test
	public void testListProjects() throws Exception {
		ProjectResponseDTO project1DTO = createSampleProject(PROJECT_NAME_1);
		ProjectResponseDTO project2DTO = createSampleProject(PROJECT_NAME_2);
		Mockito.when(projectService.getProjects()).thenReturn(List.of(project1DTO, project2DTO));

		// When
		mockMvc.perform(this.getRequestWithValidCredentials("/api/v1/projects/"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(project1DTO.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(project1DTO.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(project2DTO.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is(project2DTO.getName())));

		// Then
		Mockito.verify(projectService, Mockito.times(1)).getProjects();
	}

	@Test
	public void testGetProjectsWithInvalidCredentials() throws Exception {
		// When
		mockMvc.perform(this.getRequestWithInValidCredentials("/api/v1/projects/"))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// Then
		Mockito.verify(projectService, Mockito.never()).getProjects();
	}

	@Test
	public void testGetProjectByIdWithValidID() throws Exception {
		ProjectResponseDTO project1DTO = createSampleProject(PROJECT_NAME_1);
		Mockito.when(projectService.getProjectById(project1DTO.getId())).thenReturn(project1DTO);

		// When
		mockMvc.perform(this.getRequestWithValidCredentials(String.format("/api/v1/projects/%s", project1DTO.getId())))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(project1DTO.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(project1DTO.getName())));

		// Then
		Mockito.verify(projectService, Mockito.times(1)).getProjectById(Mockito.any());
	}

	@Test
	public void testGetProjectByIdWithInvalidID() throws Exception {
		Mockito.when(projectService.getProjectById(Mockito.any())).thenReturn(null);

		// When
		mockMvc.perform(this.getRequestWithValidCredentials(String.format("/api/v1/projects/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
		// Then
		Mockito.verify(projectService, Mockito.times(1)).getProjectById(Mockito.any());
	}

	@Test
	public void testGetProjectbyIdWithValidIDWithoutRightCredentials() throws Exception {
		// When
		mockMvc.perform(this.getRequestWithInValidCredentials(String.format("/api/v1/projects/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// Then
		Mockito.verify(projectService, Mockito.never()).getProjectById(Mockito.any());

	}

	ProjectResponseDTO createSampleProject(String name) {
		ProjectResponseDTO responseDTO = new ProjectResponseDTO();
		responseDTO.setId(UUID.randomUUID());
		responseDTO.setName(name);
		return responseDTO;
	}

}
