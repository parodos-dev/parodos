package com.redhat.parodos.workflow.definition.dto;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkDefinitionResponseDTOTest {

	@Test
	public void testFromWorkFlowTaskDefinition() {
		// given
		UUID id = UUID.randomUUID();
		WorkFlowTaskDefinition wdt = new WorkFlowTaskDefinition();
		wdt.setId(id);
		wdt.setName("foo");
		wdt.setParameters(getWorkFlowParameter());
		wdt.setOutputs(WorkFlowDTOUtil.writeObjectValueAsString(getWorkFlowTaskOutput()));

		// when
		WorkDefinitionResponseDTO result = WorkDefinitionResponseDTO.fromWorkFlowTaskDefinition(wdt);

		// then
		assertEquals(result.getId(), id.toString());
		assertEquals(result.getWorkType(), WorkType.TASK.name());
		assertEquals(result.getName(), "foo");

		assertEquals(result.getParameters().size(), 1);
		assertEquals(result.getParameters().get("key").get("format"), "text");
		assertEquals(result.getParameters().get("key").get("type"), "string");
		assertEquals(result.getParameters().get("key").get("required"), true);

		assertNotNull(result.getOutputs());
		assertEquals(result.getOutputs().size(), 1);
		assertEquals(result.getOutputs().get(0), WorkFlowTaskOutput.HTTP2XX);
	}

	@Test
	public void testFromWorkFlowTaskDefinitionWithParameters() {
		// given
		UUID id = UUID.randomUUID();
		WorkFlowDefinition wd = new WorkFlowDefinition();
		wd.setId(id);
		wd.setName("foo");
		wd.setParameters(getWorkFlowParameter());
		wd.setProcessingType("BATCH");

		List<WorkFlowWorkDefinition> dependencies = List.of(new WorkFlowWorkDefinition());
		// when
		WorkDefinitionResponseDTO result = WorkDefinitionResponseDTO.fromWorkFlowDefinitionEntity(wd, dependencies);

		// then
		assertEquals(result.getId(), id.toString());
		assertEquals(result.getWorkType(), WorkType.WORKFLOW.name());
		assertEquals(result.getName(), "foo");

		assertEquals(result.getParameters().size(), 1);
		assertEquals(result.getParameters().get("key").get("format"), "text");
		assertEquals(result.getParameters().get("key").get("type"), "string");
		assertEquals(result.getParameters().get("key").get("required"), true);
		assertEquals(result.getNumberOfWorkUnits(), dependencies.size());
		assertEquals(result.getProcessingType(), "BATCH");
		assertEquals(result.getWorks().size(), 0);
	}

	private String getWorkFlowParameter() {
		return "{\n" + "    \"key\" : {\n" + "        \"format\" : \"text\",\n"
				+ "        \"description\" : \"The app id\",\n" + "        \"type\" : \"string\",\n"
				+ "        \"required\" : true\n" + "    }\n" + "}";
	}

	private List<WorkFlowTaskOutput> getWorkFlowTaskOutput() {
		return List.of(WorkFlowTaskOutput.HTTP2XX);
	}

}