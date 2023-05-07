package com.redhat.parodos.workflow.definition.parameter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.redhat.parodos.workflow.definition.dto.WorkParameterValueRequestDTO;
import com.redhat.parodos.workflow.definition.dto.WorkParameterValueResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionService;
import com.redhat.parodos.workflow.parameter.WorkParameterValueProvider;
import com.redhat.parodos.workflow.parameter.WorkParameterValueRequest;
import com.redhat.parodos.workflow.parameter.WorkParameterValueResponse;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;

@Service
public class WorkParameterServiceImpl implements WorkParameterService {

	private final Map<String, WorkParameterValueProvider> workFlowValueProviderMap;

	private final WorkFlowDefinitionService workFlowDefinitionService;

	public WorkParameterServiceImpl(Map<String, WorkParameterValueProvider> workFlowValueProviderMap,
			WorkFlowDefinitionService workFlowDefinitionService) {
		this.workFlowValueProviderMap = workFlowValueProviderMap;
		this.workFlowDefinitionService = workFlowDefinitionService;
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
								.stream()
								.map(parameterValue -> mappingParameterResponse(workflowDefinitionName, parameterValue))
								.filter(Objects::nonNull).toList())
				.orElse(List.of());
	}

	private WorkParameterValueResponseDTO mappingParameterResponse(String workflowDefinitionName,
			WorkParameterValueResponse workParameterValueResponse) {
		String workName = Optional.ofNullable(workParameterValueResponse.getWorkName()).orElse(workflowDefinitionName);
		Map<String, Object> parameters = workFlowDefinitionService.getWorkParametersByWorkName(workName);
		if (parameters == null || !parameters.containsKey(workParameterValueResponse.getKey()))
			return null;

		String propertyPath = workName;
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionService.getParentWorkFlowByWorkName(workName);

		while (workFlowDefinition != null) {
			propertyPath = String.join(".", workFlowDefinition.getName(), propertyPath);
			workFlowDefinition = workFlowDefinitionService.getParentWorkFlowByWorkName(workFlowDefinition.getName());
		}

		return WorkParameterValueResponseDTO.convertToDto(workParameterValueResponse, propertyPath);
	}

}
