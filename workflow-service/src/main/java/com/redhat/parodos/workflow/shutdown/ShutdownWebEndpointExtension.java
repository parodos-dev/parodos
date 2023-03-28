package com.redhat.parodos.workflow.shutdown;

import java.util.Map;

import com.redhat.parodos.workflow.execution.service.WorkFlowService;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Component
public class ShutdownWebEndpointExtension extends ShutdownEndpoint {

	private final WorkFlowService workFlowService;

	public ShutdownWebEndpointExtension(WorkFlowService workFlowService) {
		this.workFlowService = workFlowService;
	}

	@WriteOperation
	public Map<String, String> shutdown() {
		workFlowService.enableGracefulShutdown();
		return super.shutdown();
	}
}
