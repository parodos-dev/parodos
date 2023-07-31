package com.redhat.parodos.tasks.azure;

import java.util.HashMap;
import java.util.UUID;

import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.sun.jdi.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AzureCreateVirtualMachineTaskTest {

	private static WorkContext ctx;

	private static AzureResourceClient azureResourceClient;

	// Define all Azure resources mocks
	private static AzureCreateVirtualMachineTask underTest;

	private static ResourceGroup resourceGroup;

	private static AvailabilitySet availabilitySet;

	private static PublicIpAddress publicIPAddress;

	private static Network network;

	private static NetworkInterface networkInterface;

	private static VirtualMachine virtualMachine;

	private static final String[] requiredParamsKeys = { AzureCreateVirtualMachineTask.VM_USER_NAME_KEY,
			AzureCreateVirtualMachineTask.VM_SSH_PUBLIC_KEY_KEY, AzureCreateVirtualMachineTask.AZURE_TENANT_ID_KEY,
			AzureCreateVirtualMachineTask.AZURE_SUBSCRIPTION_ID_KEY, AzureCreateVirtualMachineTask.AZURE_CLIENT_ID_KEY,
			AzureCreateVirtualMachineTask.AZURE_CLIENT_SECRET_KEY,
			AzureCreateVirtualMachineTask.AZURE_RESOURCES_PREFIX_KEY };

	private static final String resourcesPrefix = AzureCreateVirtualMachineTask.AZURE_RESOURCES_PREFIX_KEY
			+ "-testValue";

	private static final String userName = AzureCreateVirtualMachineTask.VM_USER_NAME_KEY + "-testValue";

	private static final String sshKey = AzureCreateVirtualMachineTask.VM_SSH_PUBLIC_KEY_KEY + "-testValue";

	@BeforeEach
	public void setUp() throws Exception {
		// Initiate all Azure resources mocks
		azureResourceClient = mock(AzureResourceClient.class);
		resourceGroup = mock(ResourceGroup.class);
		availabilitySet = mock(AvailabilitySet.class);
		publicIPAddress = mock(PublicIpAddress.class);
		network = mock(Network.class);
		networkInterface = mock(NetworkInterface.class);
		virtualMachine = mock(VirtualMachine.class);

		underTest = new AzureCreateVirtualMachineTask(azureResourceClient);
		underTest.setBeanName("AzureCreateVirtualMachineTask");
		ctx = new WorkContext();

		HashMap<String, String> map = new HashMap<>();
		for (String paramKey : requiredParamsKeys) {
			map.put(paramKey, paramKey + "-testValue");
			WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, underTest.getName(),
					WorkContextDelegate.Resource.ARGUMENTS, map);
		}

		when(azureResourceClient.createResourceGroup(resourcesPrefix)).thenReturn(resourceGroup);
		when(azureResourceClient.createAvailabilitySet(resourcesPrefix)).thenReturn(availabilitySet);
		when(azureResourceClient.createPublicIpAddress(resourcesPrefix)).thenReturn(publicIPAddress);
		when(azureResourceClient.createNetwork(resourcesPrefix)).thenReturn(network);
		when(azureResourceClient.createNetworkInterface(resourcesPrefix, network, publicIPAddress))
				.thenReturn(networkInterface);
		when(azureResourceClient.createVirtualMachine(resourcesPrefix, networkInterface, availabilitySet, userName,
				sshKey)).thenReturn(virtualMachine);
	}

	@Test
	public void testHappyFlow() {
		// given
		String publicIP = "11.11.11.11";
		when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(publicIPAddress);
		when(publicIPAddress.ipAddress()).thenReturn(publicIP);
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());

		underTest.preExecute(ctx);
		// when
		WorkReport result = underTest.execute(ctx);

		// then
		verify(azureResourceClient, times(1)).createResourceGroup(resourcesPrefix);
		verify(azureResourceClient, times(1)).createAvailabilitySet(resourcesPrefix);
		verify(azureResourceClient, times(1)).createPublicIpAddress(resourcesPrefix);
		verify(azureResourceClient, times(1)).createNetwork(resourcesPrefix);
		verify(azureResourceClient, times(1)).createNetworkInterface(resourcesPrefix, network, publicIPAddress);
		verify(azureResourceClient, times(1)).createVirtualMachine(resourcesPrefix, networkInterface, availabilitySet,
				userName, sshKey);

		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		assertEquals(publicIP, ctx.get("public-ip-address"));

	}

	@Test
	public void testVmMissingPublicIPErr() {
		// given
		String publicIP = "11.11.11.11";
		when(virtualMachine.getPrimaryPublicIPAddress()).thenReturn(null);
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		underTest.preExecute(ctx);
		// when
		WorkReport result = underTest.execute(ctx);

		// then
		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(InternalException.class, result.getError().getClass());
		assertEquals("The new created VirtualMachine missing public IP address", result.getError().getMessage());

	}

	@Test
	public void testMissingRequiredParamErr() {
		// given
		WorkContext ctx = new WorkContext();
		HashMap<String, String> map = new HashMap<>();

		for (String paramKey : requiredParamsKeys) {
			WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
					WorkContextDelegate.Resource.ID, UUID.randomUUID());
			underTest.preExecute(ctx);
			// when
			WorkReport result = underTest.execute(ctx);

			// then
			assertEquals(WorkStatus.FAILED, result.getStatus());
			assertEquals(MissingParameterException.class, result.getError().getClass());
			assertEquals("missing parameter(s) for ParameterName: " + paramKey, result.getError().getMessage());

			// Adding this key to the context for next for loop
			map.put(paramKey, paramKey + "-testValue");
			WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, underTest.getName(),
					WorkContextDelegate.Resource.ARGUMENTS, map);
		}
	}

}
