package com.redhat.parodos.workflow.definition.dto;

import com.redhat.parodos.workflow.definition.entity.WorkFlowPropertiesDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkFlowPropertiesDefinitionDTOTest {

	@Test
	public void testFromEntity() {
		// given
		WorkFlowPropertiesDefinition entity = WorkFlowPropertiesDefinition.builder().version("1.0").build();

		// when
		WorkFlowPropertiesDefinitionDTO dto = WorkFlowPropertiesDefinitionDTO.fromEntity(entity);

		// then
		assertNotNull(dto);
		assertEquals(dto.getVersion(), "1.0");
	}

	@Test
	public void testFromNull() {

		// when
		WorkFlowPropertiesDefinitionDTO dto = WorkFlowPropertiesDefinitionDTO.fromEntity(null);

		// then
		assertNotNull(dto);
		assertNull(dto.getVersion());
	}

}