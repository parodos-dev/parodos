package com.redhat.parodos.workflow.execution.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowOptionsResponseDTO {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private WorkFlowOption currentVersion;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkFlowOption> upgradeOptions;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkFlowOption> migrationOptions;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkFlowOption> newOptions;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkFlowOption> continuationOptions;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkFlowOption> otherOptions;

}
