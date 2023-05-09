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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WorkFlowOptionsResponseDTO {

	private WorkFlowOption currentVersion;

	private List<WorkFlowOption> upgradeOptions;

	private List<WorkFlowOption> migrationOptions;

	private List<WorkFlowOption> newOptions;

	private List<WorkFlowOption> continuationOptions;

	private List<WorkFlowOption> otherOptions;

}
