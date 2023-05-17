package com.redhat.parodos.examples.azure;

import com.redhat.parodos.tasks.azure.AzureCreateVirtualMachineTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureVirtualMachineWorkFlowConfiguration {

	@Bean
	AzureCreateVirtualMachineTask azureCreateVirtualMachineTask() {
		return new AzureCreateVirtualMachineTask();
	}

	@Bean(name = "azureVirtualMachineWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow azureVirtualMachineWorkFlow(
			@Qualifier("azureCreateVirtualMachineTask") AzureCreateVirtualMachineTask azureCreateVirtualMachineTask) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("azureVirtualMachineWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(azureCreateVirtualMachineTask).build();
	}

}
