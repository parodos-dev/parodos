package com.redhat.parodos.vmmigration.constants;

import java.util.List;

import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;

public class Constants {

	public static final String PLAN_NAME_PARAMETER_NAME = "planName";

	public static final String MIGRATION_NAME_PARAMETER_NAME = "migrationName";

	public static final String VM_NAME_PARAMETER_NAME = "VM_NAME";

	public static final String KUBERNETES_API_SERVER_URL_PARAMETER_NAME = "API_SERVER";

	public static final String KUBERNETES_TOKEN_PARAMETER_NAME = "TOKEN";

	public static final String KUBERNETES_CA_CERT_PARAMETER_NAME = "CA_CERT";

	public static final String STORAGE_NAME_PARAMETER_NAME = "STORAGE_NAME";

	public static final String NETWORK_NAME_PARAMETER_NAME = "NETWORK_NAME";

	public static final String NAMESPACE_NAME_PARAMETER_NAME = "NAMESPACE_NAME";

	public static final String DESTINATION_PROVIDER_TYPE_PARAMETER_NAME = "DESTINATION_TYPE";

	public static final String SOURCE_PROVIDER_TYPE_PARAMETER_NAME = "SOURCE_TYPE";

	public static WorkParameter vmNameParameter = WorkParameter.builder().key(Constants.VM_NAME_PARAMETER_NAME)
			.description("Enter the name of the Vmware VM to determine if it can be migrated to OCP Virtualization")
			.optional(false).type(WorkParameterType.TEXT).selectOptions(List.of("mtv-rhel8-sanity")).build();

	public static WorkParameter apiUrlParameter = WorkParameter.builder()
			.key(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME)
			.description("Enter the URL of the OpenShift API server where the VM will be migrated to").optional(false)
			.type(WorkParameterType.TEXT).build();

	public static WorkParameter tokenParameter = WorkParameter.builder().key(Constants.KUBERNETES_TOKEN_PARAMETER_NAME)
			.description("Enter the authentication token to the Openshift API server").type(WorkParameterType.TEXT)
			.optional(false).build();

	public static WorkParameter caCertParameter = WorkParameter.builder()
			.key(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME)
			.description(
					"Enter the CA certificate to the API server to verify the SSL certificate with the API server. If not available, the connection will not validate the certificate from the server")
			.optional(true).build();

	public static WorkParameter planParameter = WorkParameter.builder().key(Constants.PLAN_NAME_PARAMETER_NAME)
			.description("Enter the URL of the OpenShift API server where the VM will be migrated to").optional(false)
			.type(WorkParameterType.TEXT).build();

	public static WorkParameter namespaceNameParameter = WorkParameter.builder()
			.key(Constants.NAMESPACE_NAME_PARAMETER_NAME)
			.description(
					"Enter the target namespace where the VM will be migrated to inside the OCP cluster. This namespace will also need to contain the storage and network CRs required to perform the migration")
			.optional(false).build();

	public static WorkParameter storageNameParameter = WorkParameter.builder()
			.key(Constants.STORAGE_NAME_PARAMETER_NAME)
			.description(
					"Enter the name of the migration storage instance in the target namespace that will be used to migrate the VM")
			.optional(false).build();

	public static WorkParameter networkNameParameter = WorkParameter.builder()
			.key(Constants.NETWORK_NAME_PARAMETER_NAME)
			.description(
					"Enter the name of the migration network instance in the target namespace that will be used to migrate the VM")
			.optional(false).build();

	public static WorkParameter sourceProviderTypeParameter = WorkParameter.builder()
			.key(Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME)
			.description("Enter the name of the VM provider where the VM is being hosted").optional(false).build();

	public static WorkParameter destinationProviderTypeParameter = WorkParameter.builder()
			.key(Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME)
			.description("Enter the name of the VM provider where the VM will be migrated to").optional(false).build();

}
