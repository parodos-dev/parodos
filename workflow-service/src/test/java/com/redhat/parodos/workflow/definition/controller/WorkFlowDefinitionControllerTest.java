package com.redhat.parodos.workflow.definition.controller;

import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
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

import java.util.UUID;
import java.util.List;

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
		Mockito.when(workFlowDefinitionService.getWorkFlowDefinitions()).thenReturn(List.of(WFDefFoo, WFDefBar));

		// when
		this.mockMvc.perform(this.getRequestWithValidCredentials("/api/v1/workflowdefinitions"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(WFDefFoo.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(WFDefFoo.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].tasks", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].tasks[0].name", Matchers.is("task1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].tasks[0].parameters[0].key", Matchers.is("param1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].tasks[0].parameters[0].description",
						Matchers.is("param1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(WFDefBar.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is(WFDefBar.getName())));

		// then
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).getWorkFlowDefinitions();
	}

	@Test
	public void ListWorkfFlowDefinitionsWithinvalidCredentials() throws Exception {
		// when
		this.mockMvc.perform(this.getRequestWithInValidCredentials("/api/v1/workflowdefinitions"))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// then
		Mockito.verify(this.workFlowDefinitionService, Mockito.never()).getWorkFlowDefinitions();
	}

	@Test
	public void getWorkFlowDefinitionByIdWithValidData() throws Exception {
		// given
		WorkFlowDefinitionResponseDTO WFDef = createSampleWorkFlowDefinition("workflow-foo");
		Mockito.when(workFlowDefinitionService.getWorkFlowDefinitionById(WFDef.getId())).thenReturn(WFDef);

		// when
		this.mockMvc
				.perform(this
						.getRequestWithValidCredentials(String.format("/api/v1/workflowdefinitions/%s", WFDef.getId())))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(WFDef.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(WFDef.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.tasks", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.tasks[0].name", Matchers.is("task1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.tasks[0].parameters[0].key", Matchers.is("param1")))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.tasks[0].parameters[0].description", Matchers.is("param1")));

		// then
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).getWorkFlowDefinitionById(WFDef.getId());
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInValidData() throws Exception {
		// given
		Mockito.when(workFlowDefinitionService.getWorkFlowDefinitionById(Mockito.any())).thenReturn(null);

		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials(
						String.format("/api/v1/workflowdefinitions/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isNotFound());

		// then
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).getWorkFlowDefinitionById(Mockito.any());
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInValalidCredentials() throws Exception {
		// when
		this.mockMvc
				.perform(this.getRequestWithInValidCredentials(
						String.format("/api/v1/workflowdefinitions/%s", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());

		// then
		Mockito.verify(this.workFlowDefinitionService, Mockito.never()).getWorkFlowDefinitionById(Mockito.any());
	}

	private WorkFlowDefinitionResponseDTO createSampleWorkFlowDefinition(String name) {
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = new WorkFlowDefinitionResponseDTO();
		workFlowDefinitionResponseDTO.setName(name);
		workFlowDefinitionResponseDTO.setId(UUID.randomUUID());
		workFlowDefinitionResponseDTO.setWorks(List.of(createSampleWorkFlowTaskDefinition("task1")));
		return workFlowDefinitionResponseDTO;
	}

	private WorkDefinitionResponseDTO createSampleWorkFlowTaskDefinition(String name) {
		return WorkDefinitionResponseDTO.builder().id(UUID.randomUUID().toString()).name(name)
				.parameters(List.of(WorkFlowTaskParameter.builder().key("param1").description("param1").build()))
				.build();
	}

}