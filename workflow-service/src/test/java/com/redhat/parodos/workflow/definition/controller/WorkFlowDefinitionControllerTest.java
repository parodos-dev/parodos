package com.redhat.parodos.workflow.definition.controller;

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WorkFlowDefinitionControllerTest extends ControllerMockClient {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	@Test
	public void ListWorkFlowDefinitions() throws Exception {
		// given
		WorkFlowDefinitionResponseDTO WFDefFoo = createSampleWorkFlowDefinition("workflow-foo");
		WorkFlowDefinitionResponseDTO WFDefBar = createSampleWorkFlowDefinition("workflow-bar");
		when(workFlowDefinitionService.getWorkFlowDefinitions()).thenReturn(List.of(WFDefFoo, WFDefBar));

		// when
		this.mockMvc.perform(this.getRequestWithValidCredentials("/api/v1/workflowdefinitions"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(WFDefFoo.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(WFDefFoo.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].works", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].works[0].name", Matchers.is("task1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].works[0].parameters.param1.description",
						Matchers.is("param1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(WFDefBar.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is(WFDefBar.getName())));

		// then
		verify(this.workFlowDefinitionService, times(1)).getWorkFlowDefinitions();
	}

	@Test
	public void ListWorkfFlowDefinitionsWithinvalidCredentials() throws Exception {
		// when
		this.mockMvc.perform(this.getRequestWithInValidCredentials("/api/v1/workflowdefinitions"))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// then
		verify(this.workFlowDefinitionService, never()).getWorkFlowDefinitions();
	}

	@Test
	public void getWorkFlowDefinitionByIdWithValidData() throws Exception {
		// given
		WorkFlowDefinitionResponseDTO WFDef = createSampleWorkFlowDefinition("workflow-foo");
		when(workFlowDefinitionService.getWorkFlowDefinitionById(WFDef.getId())).thenReturn(WFDef);

		// when
		this.mockMvc
				.perform(this
						.getRequestWithValidCredentials(String.format("/api/v1/workflowdefinitions/%s", WFDef.getId())))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(WFDef.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(WFDef.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].name", Matchers.is("task1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].parameters.param1.description",
						Matchers.is("param1")));

		// then
		verify(this.workFlowDefinitionService, times(1)).getWorkFlowDefinitionById(WFDef.getId());
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInValidData() throws Exception {
		// given
		when(workFlowDefinitionService.getWorkFlowDefinitionById(any())).thenReturn(null);

		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials(
						String.format("/api/v1/workflowdefinitions/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isNotFound());

		// then
		verify(this.workFlowDefinitionService, times(1)).getWorkFlowDefinitionById(any());
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInValalidCredentials() throws Exception {
		// when
		this.mockMvc
				.perform(this.getRequestWithInValidCredentials(
						String.format("/api/v1/workflowdefinitions/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// then
		verify(this.workFlowDefinitionService, never()).getWorkFlowDefinitionById(any());
	}

	private WorkFlowDefinitionResponseDTO createSampleWorkFlowDefinition(String name) {
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = new WorkFlowDefinitionResponseDTO();
		workFlowDefinitionResponseDTO.setName(name);
		workFlowDefinitionResponseDTO.setId(UUID.randomUUID());
		workFlowDefinitionResponseDTO.setWorks(List.of(createSampleWorkFlowTaskDefinition("task1")));
		return workFlowDefinitionResponseDTO;
	}

	private WorkDefinitionResponseDTO createSampleWorkFlowTaskDefinition(String name) {
		String parameters = "{\"param1\": {\"description\": \"param1\"}}";
		return WorkDefinitionResponseDTO.builder().id(UUID.randomUUID()).name(name).parameterFromString(parameters)
				.build();
	}

}