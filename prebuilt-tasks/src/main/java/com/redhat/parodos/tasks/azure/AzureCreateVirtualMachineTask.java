package com.redhat.parodos.tasks.azure;

import java.util.List;

import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.sun.jdi.InternalException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AzureCreateVirtualMachineTask extends BaseInfrastructureWorkFlowTask {

	static final String VM_USER_NAME_KEY = "vm-user-name";
	static final String VM_SSH_PUBLIC_KEY_KEY = "vm-ssh-public-key";
	static final String AZURE_TENANT_ID_KEY = "azure-tenant-id";
	static final String AZURE_SUBSCRIPTION_ID_KEY = "azure-subscription-id";
	static final String AZURE_CLIENT_ID_KEY = "azure-client-id";
	static final String AZURE_CLIENT_SECRET_KEY = "azure-client-secret";
	static final String AZURE_RESOURCES_PREFIX_KEY = "azure-resources-prefix";

	private AzureResourceClient azureResourceClient;

	public AzureCreateVirtualMachineTask() {
		this(new AzureResourceClientImpl());
	}

	AzureCreateVirtualMachineTask(AzureResourceClient azureResourceClient) {
		this.azureResourceClient = azureResourceClient;
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(VM_USER_NAME_KEY).description("The user name for the Virtual Machine login")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key(VM_SSH_PUBLIC_KEY_KEY)
						.description("The SSH public key for the Virtual Machine login").type(WorkParameterType.TEXT)
						.optional(false).build(),
				WorkParameter.builder().key(AZURE_TENANT_ID_KEY)
						.description("The unique identifier of the Azure Active Directory instance")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key(AZURE_SUBSCRIPTION_ID_KEY)
						.description("The GUID that uniquely identifies your subscription to use Azure services")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key(AZURE_CLIENT_ID_KEY).description(
						"The unique Application (client) ID assigned to your app by Azure AD when the app was registered")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key(AZURE_CLIENT_SECRET_KEY)
						.description("The password of the service principal").type(WorkParameterType.TEXT)
						.optional(false).build(),
				WorkParameter.builder().key(AZURE_RESOURCES_PREFIX_KEY)
						.description("A designated prefix for naming all Azure resources").type(WorkParameterType.TEXT)
						.optional(false).build());
	}

	@Override
	/**
	 * Execute task, creates Virtual Machine in Azure Results with Virtual Machine created
	 * with public IP. This VM can be accessed using ssh (with provided username and ssh
	 * key) the public IP address is added to the context returned when the task is
	 * completed. Key: public-ip-address
	 */
	public WorkReport execute(WorkContext context) {
		try {
			final String userName = getRequiredParameterValue(context, VM_USER_NAME_KEY);
			final String sshKey = getRequiredParameterValue(context, VM_SSH_PUBLIC_KEY_KEY);

			final String azureTenantId = getRequiredParameterValue(context, AZURE_TENANT_ID_KEY);
			final String azureSubscriptionId = getRequiredParameterValue(context, AZURE_SUBSCRIPTION_ID_KEY);
			final String azureClientId = getRequiredParameterValue(context, AZURE_CLIENT_ID_KEY);
			final String azureClientSecret = getRequiredParameterValue(context, AZURE_CLIENT_SECRET_KEY);
			final String resourcesPrefix = getRequiredParameterValue(context, AZURE_RESOURCES_PREFIX_KEY);

			this.azureResourceClient.init(azureTenantId, azureClientId, azureClientSecret, azureSubscriptionId);

			ResourceGroup resourceGroup = azureResourceClient.createResourceGroup(resourcesPrefix);

			AvailabilitySet availabilitySet = azureResourceClient.createAvailabilitySet(resourcesPrefix);

			PublicIpAddress publicIPAddress = azureResourceClient.createPublicIpAddress(resourcesPrefix);

			Network network = azureResourceClient.createNetwork(resourcesPrefix);

			NetworkInterface networkInterface = azureResourceClient.createNetworkInterface(resourcesPrefix, network,
					publicIPAddress);

			VirtualMachine virtualMachine = azureResourceClient.createVirtualMachine(resourcesPrefix, networkInterface,
					availabilitySet, userName, sshKey);

			PublicIpAddress publicIpAddress = virtualMachine.getPrimaryPublicIPAddress();
			if (publicIpAddress == null) {
				log.error("VirtualMachine was created but without public IP");
				throw new InternalException("The new created VirtualMachine missing public IP address");
			}

			String ipAddress = publicIpAddress.ipAddress();
			log.info("VirtualMachine was created with public IP {}", ipAddress);
			context.put("public-ip-address", ipAddress);
		}
		catch (MissingParameterException e) {
			log.error("Task {} failed: missing required parameter, error: {}", getName(), e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, context, e);
		}
		catch (Exception e) {
			log.error("Task {} failed, with error: {}", getName(), e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, context, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, context);
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

}
