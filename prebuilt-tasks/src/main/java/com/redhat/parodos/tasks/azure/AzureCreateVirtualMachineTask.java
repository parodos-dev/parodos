package com.redhat.parodos.tasks.azure;

import java.util.List;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;

@Slf4j
public class AzureCreateVirtualMachineTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key("vm-user-name").description("The user name for the Virtual Machine login")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key("vm-ssh-public-key")
						.description("The SSH public key for the Virtual Machine login").type(WorkParameterType.TEXT)
						.optional(false).build(),
				WorkParameter.builder().key("azure-tenant-id")
						.description("The unique identifier of the Azure Active Directory instance")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key("azure-subscription-id")
						.description("The GUID that uniquely identifies your subscription to use Azure services")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key("azure-client-id").description(
						"The unique Application (client) ID assigned to your app by Azure AD when the app was registered")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key("azure-client-secret").description("The password of the service principal")
						.type(WorkParameterType.TEXT).optional(false).build(),
				WorkParameter.builder().key("azure-resources-prefix")
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
			final String userName = getRequiredParameterValue(context, "vm-user-name");
			final String sshKey = getRequiredParameterValue(context, "vm-ssh-public-key");

			final String azureTenantId = getRequiredParameterValue(context, "azure-tenant-id");
			final String azureSubscriptionId = getRequiredParameterValue(context, "azure-subscription-id");
			final String azureClientId = getRequiredParameterValue(context, "azure-client-id");
			final String azureClientSecret = getRequiredParameterValue(context, "azure-client-secret");
			final String resourcesPrefix = getRequiredParameterValue(context, "azure-resources-prefix");

			TokenCredential credential = new ClientSecretCredentialBuilder().tenantId(azureTenantId)
					.clientId(azureClientId).clientSecret(azureClientSecret).build();

			AzureProfile profile;
			if (StringUtils.hasLength(azureTenantId) && StringUtils.hasLength(azureSubscriptionId)) {
				profile = new AzureProfile(azureTenantId, azureSubscriptionId, AzureEnvironment.AZURE);
			}
			else {
				profile = new AzureProfile(AzureEnvironment.AZURE);
			}

			AzureResourceManager azureResourceManager = AzureResourceManager.configure()
					.withLogLevel(HttpLogDetailLevel.BASIC).authenticate(credential, profile).withDefaultSubscription();

			log.info("Creating resource group...");
			ResourceGroup resourceGroup = azureResourceManager.resourceGroups()
					.define(resourcesPrefix + "ResourceGroup").withRegion(Region.US_EAST).create();

			log.info("Creating availability set...");
			AvailabilitySet availabilitySet = azureResourceManager.availabilitySets()
					.define(resourcesPrefix + "AvailabilitySet").withRegion(Region.US_EAST)
					.withExistingResourceGroup(resourcesPrefix + "ResourceGroup").create();

			log.info("Creating public IP address...");
			PublicIpAddress publicIPAddress = azureResourceManager.publicIpAddresses()
					.define(resourcesPrefix + "PublicIP").withRegion(Region.US_EAST)
					.withExistingResourceGroup(resourcesPrefix + "ResourceGroup").withDynamicIP().create();

			log.info("Creating virtual network...");
			Network network = azureResourceManager.networks().define(resourcesPrefix + "VN").withRegion(Region.US_EAST)
					.withExistingResourceGroup(resourcesPrefix + "ResourceGroup").withAddressSpace("10.0.0.0/16")
					.withSubnet(resourcesPrefix + "Subnet", "10.0.0.0/24").create();

			log.info("Creating network interface...");
			NetworkInterface networkInterface = azureResourceManager.networkInterfaces().define(resourcesPrefix + "NIC")
					.withRegion(Region.US_EAST).withExistingResourceGroup(resourcesPrefix + "ResourceGroup")
					.withExistingPrimaryNetwork(network).withSubnet(resourcesPrefix + "Subnet")
					.withPrimaryPrivateIPAddressDynamic().withExistingPrimaryPublicIPAddress(publicIPAddress).create();

			log.info("Creating virtual machine...");
			VirtualMachine virtualMachine = azureResourceManager.virtualMachines().define(resourcesPrefix + "VM")
					.withRegion(Region.US_EAST).withExistingResourceGroup(resourcesPrefix + "ResourceGroup")
					.withExistingPrimaryNetworkInterface(networkInterface)
					.withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
					.withRootUsername(userName).withSsh(sshKey).withComputerName(resourcesPrefix + "VM")
					.withExistingAvailabilitySet(availabilitySet).withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
					.create();

			PublicIpAddress publicIpAddress = virtualMachine.getPrimaryPublicIPAddress();
			if (publicIpAddress == null) {
				log.error("VirtualMachine was created but without public IP");
			}

			String ipAddress = publicIpAddress.ipAddress();
			log.info("VirtualMachine was created with public IP {}", ipAddress);
			context.put("public-ip-address", ipAddress);

			return new DefaultWorkReport(WorkStatus.COMPLETED, context, null);
		}
		catch (MissingParameterException e) {
			log.error("Task {} failed: missing required parameter, error: {}", getName(), e.getMessage());
		}
		catch (Exception e) {
			log.error("Task {} failed, with error: {}", getName(), e.getMessage());
		}

		return new DefaultWorkReport(WorkStatus.FAILED, context);
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

}
