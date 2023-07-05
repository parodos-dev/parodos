package com.redhat.parodos.vmmigration;

import java.util.List;

import com.redhat.parodos.vmmigration.assessment.VmMigrationWorkFlowTask;
import com.redhat.parodos.vmmigration.checker.MigrationStatusWorkFlowChecker;
import com.redhat.parodos.vmmigration.checker.PlanStatusWorkFlowChecker;
import com.redhat.parodos.vmmigration.constants.Constants;
import com.redhat.parodos.vmmigration.task.CreateMigrationWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VmMigrationWorkFlowConfiguration {

	// Assessment workflow
	@Bean
	WorkFlowOption vmMigrationOption() {
		return new WorkFlowOption.Builder("vmMigrationOption",
				"vmMigrationWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
						.addToDetails("this is the flow to migrate the VM to OCP Virtualization")
						.displayName("VM Migration to OCP Virtualization")
						.setDescription("this is the flow to migrate the VM to OCP Virtualization").build();
	}

	// An AssessmentTask returns one or more WorkFlowOption wrapped in a WorkflowOptions
	@Bean
	VmMigrationWorkFlowTask vmMigrationWorkFlowTask(@Qualifier("vmMigrationOption") WorkFlowOption vmMigrationOption,
			@Qualifier("planStatusCheckerWorkflow") WorkFlow planStatusCheckerWorkflow) {
		VmMigrationWorkFlowTask assessment = new VmMigrationWorkFlowTask(List.of(vmMigrationOption));
		assessment.setWorkFlowCheckers(List.of(planStatusCheckerWorkflow));
		return assessment;
	}

	// A Workflow designed to execute and return WorkflowOption(s) that can be executed
	// next. In this case there is only one.
	@Bean(name = "vmMigration" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment(parameters = {
			@Parameter(key = Constants.VM_NAME_PARAMETER_NAME,
					description = "The name of the VM to migrate from vmware to OCP Virtualization",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME,
					description = "The URL of the kubernetes API server", type = WorkParameterType.TEXT,
					optional = false),
			@Parameter(key = Constants.KUBERNETES_TOKEN_PARAMETER_NAME,
					description = "The token to authenticate against the kubernetes API server",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.KUBERNETES_CA_CERT_PARAMETER_NAME,
					description = "The Certificate Authority Cert to validate the encrypted connection with the API server",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.NAMESPACE_NAME_PARAMETER_NAME,
					description = "Enter the target namespace where the VM will be migrated to inside the OCP cluster. This namespace will also need to contain the storage and network CRs required to perform the migration",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.STORAGE_NAME_PARAMETER_NAME,
					description = "Enter the name of the migration storage instance in the target namespace that will be used to migrate the VM",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.NETWORK_NAME_PARAMETER_NAME,
					description = "Enter the name of the migration network instance in the target namespace that will be used to migrate the VM",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME,
					description = "Enter the name of the VM provider where the VM is being hosted",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME,
					description = "Enter the name of the VM provider where the VM will be migrated to",
					type = WorkParameterType.TEXT, optional = false) })
	WorkFlow VmMigrationAssessmentTask(
			@Qualifier("vmMigrationWorkFlowTask") VmMigrationWorkFlowTask vmMigrationWorkFlowTask) {
		// @formatter:off
        return SequentialFlow.Builder.aNewSequentialFlow()
                .named("vmMigration" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
				.execute(vmMigrationWorkFlowTask)
                .build();
        // @formatter:on
	}

	@Bean
	CreateMigrationWorkFlowTask createMigrationFlow(
			@Qualifier("migrationStatusCheckerWorkflow") WorkFlow migrationStatusCheckerWorkflow) {
		CreateMigrationWorkFlowTask migrationWorkflow = new CreateMigrationWorkFlowTask();
		migrationWorkflow.setWorkFlowCheckers(List.of(migrationStatusCheckerWorkflow));
		return migrationWorkflow;
	}

	@Bean(name = "planStatusWorkFlowChecker")
	PlanStatusWorkFlowChecker planStatusWorkFlowChecker() {
		return new PlanStatusWorkFlowChecker();
	}

	@Bean(name = "migrationStatusWorkFlowChecker")
	MigrationStatusWorkFlowChecker migrationStatusWorkFlowChecker() {
		return new MigrationStatusWorkFlowChecker();
	}

	@Bean
	@Checker(cronExpression = "*/5 * * * * ?")
	public WorkFlow planStatusCheckerWorkflow(
			@Qualifier("planStatusWorkFlowChecker") PlanStatusWorkFlowChecker planStatusWorkFlowChecker) {
		// @formatter:off
		return SequentialFlow.Builder
				.aNewSequentialFlow()
				.named("planStatusCheckerWorkflow")
				.execute(planStatusWorkFlowChecker)
				.build();
		// @formatter:on
	}

	@Bean
	@Checker(cronExpression = "0 */1 * * * ?")
	public WorkFlow migrationStatusCheckerWorkflow(
			@Qualifier("migrationStatusWorkFlowChecker") MigrationStatusWorkFlowChecker migrationStatusWorkFlowChecker) {
		// @formatter:off
		return SequentialFlow.Builder
				.aNewSequentialFlow()
				.named("migrationStatusCheckerWorkflow")
				.execute(migrationStatusWorkFlowChecker)
				.build();
		// @formatter:on
	}

	@Bean(name = "vmMigrationWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure(parameters = {
			@Parameter(key = Constants.VM_NAME_PARAMETER_NAME,
					description = "The name of the VM to migrate from vmware to OCP Virtualization",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME,
					description = "The URL of the kubernetes API server", type = WorkParameterType.TEXT,
					optional = false),
			@Parameter(key = Constants.KUBERNETES_TOKEN_PARAMETER_NAME,
					description = "The token to authenticate against the kubernetes API server",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.KUBERNETES_CA_CERT_PARAMETER_NAME,
					description = "The Certificate Authority Cert to validate the encrypted connection with the API server",
					type = WorkParameterType.TEXT, optional = true),
			@Parameter(key = Constants.NAMESPACE_NAME_PARAMETER_NAME,
					description = "Enter the target namespace where the VM will be migrated to inside the OCP cluster. This namespace will also need to contain the storage and network CRs required to perform the migration",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.STORAGE_NAME_PARAMETER_NAME,
					description = "Enter the name of the migration storage instance in the target namespace that will be used to migrate the VM",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.NETWORK_NAME_PARAMETER_NAME,
					description = "Enter the name of the migration network instance in the target namespace that will be used to migrate the VM",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME,
					description = "Enter the name of the VM provider where the VM is being hosted",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME,
					description = "Enter the name of the VM provider where the VM will be migrated to",
					type = WorkParameterType.TEXT, optional = false) })
	WorkFlow vmMigrationWorkFlow(
			@Qualifier("createMigrationFlow") CreateMigrationWorkFlowTask createMigrationWorkFlow) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("vmMigrationWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
				.execute(createMigrationWorkFlow).build();
	}

}
