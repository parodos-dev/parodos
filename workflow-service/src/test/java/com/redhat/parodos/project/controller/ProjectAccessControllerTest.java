package com.redhat.parodos.project.controller;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.project.dto.request.AccessStatusRequestDTO;
import com.redhat.parodos.project.dto.response.AccessStatusResponseDTO;
import com.redhat.parodos.project.enums.ProjectAccessStatus;
import com.redhat.parodos.project.service.ProjectAccessServiceImpl;
import org.hamcrest.Matchers;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DirtiesContext
public class ProjectAccessControllerTest extends ControllerMockClient {

	private static final UUID TEST_ACCESS_REQUEST_ID = UUID.randomUUID();

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProjectAccessServiceImpl projectAccessService;

	@Test
	public void testGetProjectAccessStatus() throws Exception {
		// given
		AccessStatusResponseDTO accessStatusResponseDTO = AccessStatusResponseDTO.builder()
				.accessRequestId(TEST_ACCESS_REQUEST_ID).status(ProjectAccessStatus.PENDING).build();

		when(projectAccessService.getProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID)))
				.thenReturn(accessStatusResponseDTO);

		// when
		mockMvc.perform(this
				.getRequestWithValidCredentials(
						String.format("/api/v1/projects/access/%s/status", TEST_ACCESS_REQUEST_ID))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessRequestId",
						Matchers.is(TEST_ACCESS_REQUEST_ID.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ProjectAccessStatus.PENDING.name())));

		// then
		verify(projectAccessService, times(1)).getProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID));
	}

	@Test
	public void testGetProjectAccessStatusWithInvalidAuth() throws Exception {
		// when
		mockMvc.perform(this
				.getRequestWithInValidCredentials(
						String.format("/api/v1/projects/access/%s/status", TEST_ACCESS_REQUEST_ID))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// then
		verify(projectAccessService, never()).getProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID));
	}

	@Test
	public void testGetProjectAccessStatusWithNotFoundAccessRequestId() throws Exception {
		// when
		when(projectAccessService.getProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID)))
				.thenThrow(new ResourceNotFoundException(""));

		// then
		mockMvc.perform(this
				.getRequestWithValidCredentials(
						String.format("/api/v1/projects/access/%s/status", TEST_ACCESS_REQUEST_ID))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());

		verify(projectAccessService, times(1)).getProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID));
	}

	@Test
	public void testUpdateProjectAccessStatusWithNotFoundAccessRequestId() throws Exception {
		// given
		AccessStatusRequestDTO accessStatusRequestDTO = AccessStatusRequestDTO.builder()
				.status(ProjectAccessStatus.APPROVED).build();

		// when
		doThrow(new ResourceNotFoundException("")).when(projectAccessService)
				.updateProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID), any());

		String jsonPayload = objectMapper.writeValueAsString(accessStatusRequestDTO);
		mockMvc.perform(this
				.postRequestWithValidCredentials(
						String.format("/api/v1/projects/access/%s/status", TEST_ACCESS_REQUEST_ID))
				.content(jsonPayload)).andExpect(MockMvcResultMatchers.status().isNotFound());

		// Then
		verify(projectAccessService, times(1)).updateProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID),
				eq(accessStatusRequestDTO));
	}

	@Test
	public void testUpdateProjectAccessStatusWithValidAccessRequestId() throws Exception {
		// given
		AccessStatusRequestDTO accessStatusRequestDTO = AccessStatusRequestDTO.builder()
				.status(ProjectAccessStatus.APPROVED).build();

		// when
		doNothing().when(projectAccessService).updateProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID), any());

		String jsonPayload = objectMapper.writeValueAsString(accessStatusRequestDTO);
		mockMvc.perform(this
				.postRequestWithValidCredentials(
						String.format("/api/v1/projects/access/%s/status", TEST_ACCESS_REQUEST_ID))
				.content(jsonPayload)).andExpect(MockMvcResultMatchers.status().isNoContent());

		// Then
		verify(projectAccessService, times(1)).updateProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID),
				eq(accessStatusRequestDTO));
	}

	@Test
	public void testUpdateProjectAccessStatusWithInvalidCredentials() throws Exception {
		// given
		AccessStatusRequestDTO accessStatusRequestDTO = AccessStatusRequestDTO.builder()
				.status(ProjectAccessStatus.APPROVED).build();

		// When
		String jsonPayload = objectMapper.writeValueAsString(accessStatusRequestDTO);
		mockMvc.perform(this
				.postRequestWithInValidCredentials(
						String.format("/api/v1/projects/access/%s/status", TEST_ACCESS_REQUEST_ID))
				.content(jsonPayload)).andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// Then
		verify(projectAccessService, never()).updateProjectAccessStatusById(eq(TEST_ACCESS_REQUEST_ID), any());
	}

}
