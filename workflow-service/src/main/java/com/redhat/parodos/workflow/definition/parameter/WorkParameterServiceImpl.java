package com.redhat.parodos.workflow.definition.parameter;

import com.redhat.parodos.workflow.definition.dto.WorkParameterValueRequestDTO;
import com.redhat.parodos.workflow.definition.dto.WorkParameterValueResponseDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionService;
import com.redhat.parodos.workflow.parameter.WorkParameterValueRequest;
import com.redhat.parodos.workflow.parameter.WorkParameterValueProvider;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkParameterServiceImpl implements WorkParameterService {

	private final Map<String, WorkParameterValueProvider> workFlowValueProviderMap;

	public WorkParameterServiceImpl(Map<String, WorkParameterValueProvider> workFlowValueProviderMap) {
		this.workFlowValueProviderMap = workFlowValueProviderMap;
	}

	@Override
	public List<WorkParameterValueResponseDTO> getValues(String workflowDefinitionName, String valueProviderName,
			List<WorkParameterValueRequestDTO> workParameterValueRequestDTOs) {
		return Optional
				.ofNullable(workFlowValueProviderMap.get(valueProviderName)).map(
						valueProvider -> valueProvider
								.getValuesForWorkflow(workflowDefinitionName,
										workParameterValueRequestDTOs.stream()
												.map(workParameterValueRequestDTO -> new ModelMapper().map(
														workParameterValueRequestDTO, WorkParameterValueRequest.class))
												.toList())
								.stream().map(parameterValue -> new ModelMapper().map(parameterValue,
										WorkParameterValueResponseDTO.class))
								.toList())
				.orElse(List.of());
	}

}
