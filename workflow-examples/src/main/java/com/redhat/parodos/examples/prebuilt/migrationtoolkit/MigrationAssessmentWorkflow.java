package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import java.net.URI;
import java.util.List;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.tasks.migrationtoolkit.CreateApplicationTask;
import com.redhat.parodos.tasks.migrationtoolkit.GetAnalysisTask;
import com.redhat.parodos.tasks.migrationtoolkit.GetApplicationTask;
import com.redhat.parodos.tasks.migrationtoolkit.SubmitAnalysisTask;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.redhat.parodos.workflows.workflow.SequentialFlow.Builder.aNewSequentialFlow;

/**
 * An assessment workflow to analyze applications using Migration Toolkit for Applications
 * and return a move2kube option when needed.
 * <p>
 * This workflow will: - create an application with a name, and git repo URL - submit an
 * analysis report for it - wait till the report is back, analyze it, and return a
 * workflow option to move2kube based on the findings
 * <p>
 */
@Configuration
public class MigrationAssessmentWorkflow {

	// the url of the MTA (migration toolkit for application)
	@Value("${workflows.mta.url}")
	private String mtaUrl;

	@Bean
	public CreateApplicationTask createApplicationTask() {
		return new CreateApplicationTask(URI.create(mtaUrl), "");
	}

	@Bean
	public GetApplicationTask getApplicationTask() {
		return new GetApplicationTask(URI.create(mtaUrl), "");
	}

	@Bean
	public SubmitAnalysisTask submitAnalysisTask(WorkFlow fetchReportURL) {
		SubmitAnalysisTask t = new SubmitAnalysisTask(URI.create(mtaUrl), "");
		t.setWorkFlowCheckers(List.of(fetchReportURL));
		return t;
	}

	@Bean
	public GetAnalysisTask getAnalysisTask(Notifier notifier) {
		return new GetAnalysisTask(URI.create(mtaUrl), "", notifier);
	}

	@Bean
	ProcessAnalysisTask processAnalysisTask(WorkFlowOption move2kube, WorkFlowOption defaultOption, Notifier notifier) {
		return new ProcessAnalysisTask(move2kube, defaultOption,
				// suggest the move2kube option if this predicate is true
				analysisIncidents -> analysisIncidents.mandatory() == 0 && analysisIncidents.cloudMandatory() == 0,
				notifier);
	}

	@Bean
	WorkFlowOption move2kube() {
		return new WorkFlowOption.Builder("move2kube", "move2KubeWorkFlow_INFRASTRUCTURE_WORKFLOW")
				.addToDetails("Migration to OCP").displayName("Migration to OCP")
				.setDescription("This app is ready to be migrated to OCP. Click to migrate.").build();
	}

	@Bean
	WorkFlowOption defaultOption() {
		return new WorkFlowOption.Builder("defaultOption", "AnalyzeApplicationAssessment")
				.addToDetails("Rerun Analysis").displayName("Run Migration Analysis")
				.setDescription(
						"This application didn't meet the expected analysis score. Update or apply the relevant fixes and re-run the analysis")
				.build();
	}

	@Bean(name = "AnalyzeApplicationAssessment")
	@Assessment(parameters = {
			@Parameter(key = "repositoryURL", description = "The repository with the code to analyze",
					type = WorkParameterType.URI, optional = false),
			@Parameter(key = "applicationName", description = "The name of the application to analyze",
					type = WorkParameterType.TEXT, optional = false),
			@Parameter(key = "branch", description = "The repository branch to analyze", type = WorkParameterType.TEXT,
					optional = true),
			@Parameter(key = "identity", description = "The identity of ssh key in MTA", type = WorkParameterType.TEXT,
					optional = true) })
	public WorkFlow AnalyzeApplicationAssessment(CreateApplicationTask createApplicationTask,
			GetApplicationTask getAppTask, SubmitAnalysisTask submitAnalysisTask) {
		return aNewSequentialFlow().named("AnalyzeApplicationAssessment").execute(createApplicationTask)
				.then(getAppTask).then(submitAnalysisTask).build();
	}

	@Bean("fetchReportURL")
	@Checker(cronExpression = "*/5 * * * * ?")
	public WorkFlow fetchReportURL(GetAnalysisTask getAnalysisTask, ProcessAnalysisTask processAnalysisTask) {
		return aNewSequentialFlow().named("fetchReportURL").execute(getAnalysisTask).then(processAnalysisTask).build();
	}

}
