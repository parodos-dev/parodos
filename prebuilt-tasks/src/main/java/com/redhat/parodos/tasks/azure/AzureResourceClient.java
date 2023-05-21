package com.redhat.parodos.tasks.azure;

import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;

interface AzureResourceClient {

	void init(String azureTenantId, String azureClientId, String azureClientSecret, String azureSubscriptionId);

	ResourceGroup createResourceGroup(String resourcesPrefix);

	AvailabilitySet createAvailabilitySet(String resourcesPrefix);

	PublicIpAddress createPublicIpAddress(String resourcesPrefix);

	Network createNetwork(String resourcesPrefix);

	NetworkInterface createNetworkInterface(String resourcesPrefix, Network network, PublicIpAddress publicIPAddress);

	VirtualMachine createVirtualMachine(String resourcesPrefix, NetworkInterface networkInterface,
			AvailabilitySet availabilitySet, String userName, String sshKey);

}
