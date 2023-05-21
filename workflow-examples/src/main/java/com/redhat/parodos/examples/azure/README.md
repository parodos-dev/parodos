# Running AzureVirtualMachine WorkFlow Configuration instructions

AzureVirtualMachineWorkFlow Configuration is an illustration of configuration utilizing the AzureVirtualMachineCreateVM Task.
Below are guidelines on executing this example workflow configuration using curl.

## 1. Prerequisites

* Have account in azure, at least contributor user is required (In this case, second step should be requested from Admin).
* You updated workflow-service running and accessible from your machine.

## 2. Create App Registration in Azure account

The workflow-service java application needs read and create permissions in your Azure subscription to run the AzureVirtualMachineCreateVM Task. Create a service principal, which its credentials would be used by the application. Service principals provide a way to create a noninteractive account associated with your identity to which you grant only the privileges your app needs to run.

This step must be done by Admin user in Azure.
It can be done only ones, make sure it has the required expiration date.

### Configure the environment variable

- **`AZURE_SUBSCRIPTION_ID`**: Use the id value from `az account show` in the Azure CLI 2.0.

### Create a service principal by using the Azure CLI 2.0, and capture the output

```shell
az ad sp create-for-rbac \
--name AzureJavaTest \
--role Contributor \
--scopes /subscriptions/${AZURE_SUBSCRIPTION_ID}
```

## 3. Execute the workflow using curl

### Configure the environment variables

- **`AZURE_SUBSCRIPTION_ID`**: Use the id value from `az account show` in the Azure CLI 2.0.
- **`AZURE_CLIENT_ID`**: Use the appId value from the output taken from a service principal output.
- **`AZURE_CLIENT_SECRET`**: Use the password value from the service principal output.
- **`AZURE_TENANT_ID`**: Use the tenant value from the service principal output.
- **`AZURE_RESOURCES_PREFIX`**: A designated prefix for naming all Azure resources
- **`VM_USER_NAME`**: The username for the Virtual Machine login
- **`VM_SSH_PUBLIC_KEY`**: The SSH public key for the Virtual Machine login
- **`PARODOS_PROJECT_ID`**: Parodos project ID

### Execute the workflow

```shell
curl -X 'POST' -u test:test \
  'http://localhost:8080/api/v1/workflows' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "arguments": [],
  "projectId": "'"${PARODOS_PROJECT_ID}"'",
  "workFlowName": "azureVirtualMachineWorkFlow_INFRASTRUCTURE_WORKFLOW",
  "works": [
    {
      "arguments": [
        {
          "key": "azure-tenant-id",
          "value": "'"${AZURE_TENANT_ID}"'"
        },
        {
          "key": "azure-subscription-id",
          "value": "'"${AZURE_SUBSCRIPTION_ID}"'"
        },
        {
          "key": "azure-client-id",
          "value": "'"${AZURE_CLIENT_ID}"'"
        },
        {
          "key": "azure-client-secret",
          "value": "'"${AZURE_CLIENT_SECRET}"'"
        },
        {
          "key": "azure-resources-prefix",
          "value": "'"${AZURE_RESOURCES_PREFIX}"'"
        },
        {
          "key": "vm-user-name",
          "value": "'"${VM_USER_NAME}"'"
        },
        {
          "key": "vm-ssh-public-key",
          "value": "'"${VM_SSH_PUBLIC_KEY}"'"
        }
      ],
      "type": "TASK",
      "workName": "azureCreateVirtualMachineTask"
    }
  ]
}'
```
## 4. Validate access to the VM

### Configure the environment variables

- **`VM_PUBLIC_IP`**: The public IP was assigned to the VM, you can find it in the workflow-service logs
- **`VM_USER_NAME`**: The username for the Virtual Machine login
- **`PRIVATE_SSH_KEY_PATH`**: The private ssh key which its public key was used for the VM creation in previous step

### Access the VM using ssh

```shell
ssh -i ${PRIVATE_SSH_KEY_PATH} ${VM_USER_NAME}@${VM_PUBLIC_IP}
```
## 5. Delete all resources in Azure

### Configure the environment variables

- **`AZURE_RESOURCES_PREFIX`**: A designated prefix for naming all Azure resources

### delete the VM

```shell
az group delete -n "${AZURE_RESOURCES_PREFIX}ResourceGroup"
```

