package com.redhat.parodos.examples.assessment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.github.GitHub;

import com.redhat.parodos.patterndetection.PatternDetector;
import com.redhat.parodos.patterndetection.clue.ContentsClueImpl;
import com.redhat.parodos.patterndetection.clue.NameClueImpl;
import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClientConfiguration;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.pattern.BasicPatternImpl;
import com.redhat.parodos.patterndetection.results.DetectionResults;
import com.redhat.parodos.tasks.github.GithubPatternDetectionTask;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.option.WorkFlowOptions;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatternDetectionAssessmentTask extends GithubPatternDetectionTask {

	// these are the keys to parameters that come in from the UI
	private static final String REPO = "REPO";

	private static final String ORG = "ORG";

	private static final String BRANCH = "BRANCH";

	// these are configured in the Github config
	List<WorkFlowOption> workFlowOptions;

	// client
	GitFileInputStreamClient client;

	PatternDetectionAssessmentTask(GitHub github, List<WorkFlowOption> workFlowOptions) {
		super(github, workFlowOptions);
		this.workFlowOptions = workFlowOptions;
		this.client = new GitFileInputStreamClient(github);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		// Get the Params from the UI
		Map<String, Object> myMap;
		try {
			// @formatter:off
			myMap = Map.of(
					REPO, getRequiredParameterValue(workContext, REPO), 
					ORG, getRequiredParameterValue(workContext, ORG), 
					BRANCH,getRequiredParameterValue(workContext, BRANCH)
				);
			// @formatter:on
			// Get all the file names
			var data = super.getDirectoriesAndFiles(getRequiredParameterValue(workContext, ORG), getRequiredParameterValue(workContext, REPO));
			List<String> fileNames = new ArrayList<>();
			for (Entry<String, List<String>> directory : data.entrySet()) {
				fileNames.addAll(directory.getValue());
			}
			
			// Clues to use during test cases
			BasicPatternImpl ocpTargetApp = configureCluesAndPatterns();
			WorkContext context =
					// @formatter:off
					PatternDetectionWorkContextDelegate
					.WorkContextBuilder.builder()
					.addThisToDesiredPatterns(ocpTargetApp)
					.addContentInputStreamClientAndPaths(
							ContentInputStreamClientConfiguration.builder()
							.contentClient(client)
							.name("githubPatternDetection")
							.pathsToProcessForContent(fileNames)
							.parametersForClient(myMap)
							.build())
					.build();
					// @formatter:on
			DetectionResults results = PatternDetector.detect(context);
			if (results.isAllPatternsWhereDetected()) {
				// put workflow option for ocp onboarding in the context
				WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				         WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
				         // @formatter:off
				         new WorkFlowOptions.Builder()
				         .addNewOption(workFlowOptions.stream().filter(option -> option.getDisplayName().contains("OCP Onboarding")).findFirst().get())
				         .build());
				         // @formatter:on
			}
			else {
				WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				         WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
				         // @formatter:off
				         new WorkFlowOptions.Builder()
				         .addNewOption(workFlowOptions.stream().filter(option -> option.getDisplayName().contains("Not Supported Application")).findFirst().get())
				         .build());
				         // @formatter:on
			}

			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		catch (MissingParameterException | IOException e) {
			log.error("Issues performing this assessment: ", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
	}

	private BasicPatternImpl configureCluesAndPatterns() {
		return
		// @formatter:off
			BasicPatternImpl
			.Builder
			.aNewPattern()
			.addThisToAllAreRequiredClues(NameClueImpl.Builder.builder().name("maven").targetFileNamePatternString("pom.xml").build())
			.addThisToAllAreRequiredClues(ContentsClueImpl.Builder.builder().name("tomcatConfig").targetFileExtensionPatternString(".java").targetContentPatternString(".*TomcatServletWebServerFactory.*").build())
			.build();
			// @formatter:on
	}

	@Override
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return
		// @formatter:off
			List.of(
					WorkFlowTaskParameter
					.builder()
					.key(REPO)
					.description("Please enter the repository you wish to assess")
					.optional(false)
					.type(WorkFlowTaskParameterType.TEXT)
					.build(),
					WorkFlowTaskParameter
					.builder()
					.key(ORG)
					.description("Please enter the organization with the repository you wish to assess")
					.optional(false)
					.type(WorkFlowTaskParameterType.TEXT)
					.build(),
					WorkFlowTaskParameter
					.builder()
					.key(BRANCH)
					.description("Please enter the branch of the the repository you wish to assess")
					.optional(false)
					.type(WorkFlowTaskParameterType.TEXT)
					.build()
			);
			// @formatter:on

	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return Collections.emptyList();
	}

}
