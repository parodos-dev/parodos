package com.redhat.parodos.workflow.definition.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.redhat.parodos.workflow.definition.entity.WorkFlowPropertiesDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = WorkFlowPropertiesDefinitionDTO.WorkFlowPropertiesDefinitionDTOBuilder.class)
@Data
public class WorkFlowPropertiesDefinitionDTO {

	private String version;

	public static WorkFlowPropertiesDefinitionDTO fromEntity(WorkFlowPropertiesDefinition properties) {
		WorkFlowPropertiesDefinitionDTOBuilder builder = WorkFlowPropertiesDefinitionDTO.builder();
		if (properties == null) {
			return builder.build();
		}
		return builder.version(properties.getVersion()).build();
	}

}
