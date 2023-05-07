package com.redhat.parodos.workflow.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import lombok.NonNull;

import org.springframework.core.annotation.AnnotationAttributes;

public class WorkFlowRegistryDelegate {

	private WorkFlowRegistryDelegate() {
	}

	@NonNull
	static List<WorkParameter> getWorkParameters(AnnotationAttributes[] annotationAttributes) {
		List<WorkParameter> workParameters = new ArrayList<>();
		if (annotationAttributes != null && annotationAttributes.length > 0) {
			workParameters = Arrays.stream(annotationAttributes)
					.map(annotationAttribute -> WorkParameter.builder().key(annotationAttribute.getString("key"))
							.description(annotationAttribute.getString("description"))
							.type((WorkParameterType) annotationAttribute.get("type"))
							.optional(annotationAttribute.getBoolean("optional"))
							.selectOptions(Arrays.stream(annotationAttribute.getStringArray("selectOptions")).toList())
							.valueProviderName(annotationAttribute.getString("valueProviderName")).build())
					.toList();
		}
		return workParameters;
	}

}