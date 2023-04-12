package com.redhat.parodos.examples.assessment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
			myMap = Map.of(REPO, WorkContextDelegate.getRequiredValueFromRequestParams(workContext, REPO), ORG,
					WorkContextDelegate.getRequiredValueFromRequestParams(workContext, ORG), BRANCH,
					WorkContextDelegate.getRequiredValueFromRequestParams(workContext, BRANCH));
			// Get all the file names
			var data = super.getDirectoriesAndFiles(ORG, REPO, BRANCH);
			List<String> fileNames = new ArrayList<>();
			for (Entry<String, List<String>> directory : data.entrySet()) {
				fileNames.addAll(directory.getValue());
			}
			// Configure the client
			ContentInputStreamClientConfiguration.builder().contentClient(client).name("githubPatternDetection")
					.parametersForClient(myMap).build();
		}
		catch (MissingParameterException | IOException e) {
			log.error("Issues performing this assessment: ", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		// Clues to use during test cases
		BasicPatternImpl ocpTargetApp = configureCluesAndPatterns();
		WorkContext context = PatternDetectionWorkContextDelegate.WorkContextBuilder.builder()
				.addThisToDesiredPatterns(ocpTargetApp).build();
		DetectionResults results = PatternDetector.detect(context);
		if (results.isAllPatternsWhereDetected()) {
			// put workflow option for ocp onboarding in the context
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
					WorkContextDelegate.Resource.WORKFLOW_OPTIONS, workFlowOptions.stream()
							.filter(option -> option.getDisplayName().contains("ocp")).collect(Collectors.toList()));
		}
		else {
			// put the workflow option for 'not supported' in the context
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private BasicPatternImpl configureCluesAndPatterns() {
		return
		// @formatter:off
			BasicPatternImpl
			.Builder
			.aNewPattern()
			.addThisToAllAreRequiredClues(NameClueImpl.Builder.builder().name("maven").targetFileNamePatternString("pom.xml").build())
			.addThisToAllAreRequiredClues(ContentsClueImpl.Builder.builder().name("restcontroller").targetFileExtensionPatternString(".java").targetContentPatternString("@RestController").build())
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
