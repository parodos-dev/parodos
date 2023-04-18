package com.redhat.parodos.workflow.definition.parameter;

import com.redhat.parodos.workflow.definition.dto.WorkParameterValueRequestDTO;
import com.redhat.parodos.workflow.definition.dto.WorkParameterValueResponseDTO;

import java.util.List;
import java.util.UUID;

public interface WorkParameterService {

	List<WorkParameterValueResponseDTO> getValues(String workflowDefinitionName, String valueProviderName,
			List<WorkParameterValueRequestDTO> workParameterValueRequestDTOs);

}
