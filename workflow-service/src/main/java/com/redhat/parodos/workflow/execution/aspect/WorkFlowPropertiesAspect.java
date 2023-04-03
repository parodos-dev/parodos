package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.annotation.WorkFlowProperties;
import com.redhat.parodos.workflow.version.WorkFlowVersionServiceImpl;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import com.redhat.parodos.workflows.workflow.WorkFlowPropertiesMetadata;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Aspect
@Component
@Slf4j
@Order(-1)
public class WorkFlowPropertiesAspect {

	@Autowired
	public Environment env;

	@Around("@annotation(com.redhat.parodos.workflow.annotation.WorkFlowProperties) && args(..)")
	public Object WorkFlowPropertiesAround(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		WorkFlowProperties properties = signature.getMethod().getAnnotation(WorkFlowProperties.class);

		Object result = joinPoint.proceed();
		if (!(result instanceof WorkFlow)) {
			return result;
		}

		WorkFlow workFlow = (WorkFlow) result;
		return setProperitesForWorkflow(workFlow, properties);
	}

	public Object setProperitesForWorkflow(WorkFlow workFlow, WorkFlowProperties properties) throws IOException {
		if (properties == null) {
			return workFlow;
		}
		String version = env.resolvePlaceholders(properties.version());
		if (version.isEmpty()) {
			version = WorkFlowVersionServiceImpl.GetVersionHashForObject(workFlow);
		}
		WorkFlowPropertiesMetadata propertiesMetadata = WorkFlowPropertiesMetadata.builder().version(version).build();
		workFlow.setProperties(propertiesMetadata);
		return workFlow;
	}

}