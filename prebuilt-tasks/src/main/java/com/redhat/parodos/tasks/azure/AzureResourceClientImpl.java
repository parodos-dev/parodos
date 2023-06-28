package com.redhat.parodos.tasks.azure;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
class AzureResourceClientImpl implements AzureResourceClient {

	private AzureResourceManager azureResourceManager;

	@Override
	public void init(String azureTenantId, String azureClientId, String azureClientSecret, String azureSubscriptionId) {
		TokenCredential credential = new ClientSecretCredentialBuilder().tenantId(azureTenantId).clientId(azureClientId)
				.clientSecret(azureClientSecret).build();
		AzureProfile profile = new AzureProfile(azureTenantId, azureSubscriptionId, AzureEnvironment.AZURE);
		azureResourceManager = AzureResourceManager.configure().withLogLevel(HttpLogDetailLevel.BASIC)
				.authenticate(credential, profile).withDefaultSubscription();
	}

	@Override
	public ResourceGroup createResourceGroup(String resourcesPrefix) {
		log.info("Creating resource group...");
		return azureResourceManager.resourceGroups().define(resourcesPrefix + "ResourceGroup")
				.withRegion(Region.US_EAST).create();
	}

	@Override
	public AvailabilitySet createAvailabilitySet(String resourcesPrefix) {
		log.info("Creating availability set...");
		return azureResourceManager.availabilitySets().define(resourcesPrefix + "AvailabilitySet")
				.withRegion(Region.US_EAST).withExistingResourceGroup(resourcesPrefix + "ResourceGroup").create();
	}

	@Override
	public PublicIpAddress createPublicIpAddress(String resourcesPrefix) {
		log.info("Creating public IP address...");
		return azureResourceManager.publicIpAddresses().define(resourcesPrefix + "PublicIP").withRegion(Region.US_EAST)
				.withExistingResourceGroup(resourcesPrefix + "ResourceGroup").withDynamicIP().create();
	}

	@Override
	public Network createNetwork(String resourcesPrefix) {
		log.info("Creating virtual network...");
		return azureResourceManager.networks().define(resourcesPrefix + "VN").withRegion(Region.US_EAST)
				.withExistingResourceGroup(resourcesPrefix + "ResourceGroup").withAddressSpace("10.0.0.0/16")
				.withSubnet(resourcesPrefix + "Subnet", "10.0.0.0/24").create();
	}

	@Override
	public NetworkInterface createNetworkInterface(String resourcesPrefix, Network network,
			PublicIpAddress publicIPAddress) {
		log.info("Creating network interface...");
		return azureResourceManager.networkInterfaces().define(resourcesPrefix + "NIC").withRegion(Region.US_EAST)
				.withExistingResourceGroup(resourcesPrefix + "ResourceGroup").withExistingPrimaryNetwork(network)
				.withSubnet(resourcesPrefix + "Subnet").withPrimaryPrivateIPAddressDynamic()
				.withExistingPrimaryPublicIPAddress(publicIPAddress).create();
	}

	@Override
	public VirtualMachine createVirtualMachine(String resourcesPrefix, NetworkInterface networkInterface,
			AvailabilitySet availabilitySet, String userName, String sshKey) {
		log.info("Creating virtual machine...");
		return azureResourceManager.virtualMachines().define(resourcesPrefix + "VM").withRegion(Region.US_EAST)
				.withExistingResourceGroup(resourcesPrefix + "ResourceGroup")
				.withExistingPrimaryNetworkInterface(networkInterface)
				.withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS).withRootUsername(userName)
				.withSsh(sshKey).withComputerName(resourcesPrefix + "VM").withExistingAvailabilitySet(availabilitySet)
				.withSize(VirtualMachineSizeTypes.STANDARD_D3_V2).create();
	}

}
