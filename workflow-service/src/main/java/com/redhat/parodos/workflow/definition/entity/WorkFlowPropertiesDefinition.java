package com.redhat.parodos.workflow.definition.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WorkFlowPropertiesDefinition implements Serializable {

	@Getter
	@Setter
	private String version;

}
