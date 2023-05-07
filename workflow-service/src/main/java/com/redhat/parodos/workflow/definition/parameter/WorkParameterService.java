package com.redhat.parodos.workflow.definition.parameter;

import java.util.List;

import com.redhat.parodos.workflow.definition.dto.WorkParameterValueRequestDTO;
import com.redhat.parodos.workflow.definition.dto.WorkParameterValueResponseDTO;

public interface WorkParameterService {

	List<WorkParameterValueResponseDTO> getValues(String workflowDefinitionName, String valueProviderName,
			List<WorkParameterValueRequestDTO> workParameterValueRequestDTOs);

}
