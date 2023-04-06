package com.redhat.parodos.examples.assessment;

import java.io.File;

import org.kohsuke.github.GitHub;

import com.redhat.parodos.patterndetection.PatternDetector;
import com.redhat.parodos.patterndetection.clue.ContentsClueImpl;
import com.redhat.parodos.patterndetection.clue.NameClueImpl;
import com.redhat.parodos.patterndetection.context.WorkContextDelegate;
import com.redhat.parodos.patterndetection.pattern.BasicPatternImpl;
import com.redhat.parodos.patterndetection.pattern.Pattern;
import com.redhat.parodos.patterndetection.results.DetectionResults;
import com.redhat.parodos.tasks.github.GithubAwareTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;

public class PatternDetectionAssessmentTask extends GithubAwareTask {

	PatternDetectionAssessmentTask(GitHub github) {
		super(github);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		// Clues to use during test cases

		// @formatter:off
		NameClueImpl mavenConfig =
				NameClueImpl
					.Builder
					.builder()
					.name("maven")
					.targetFileNamePatternString("pom.xml")
				.build();
		ContentsClueImpl restControllerStopWhenDetected =
				ContentsClueImpl
					.Builder
					.builder()
					.name("restcontroller")
					.targetFileExtensionPatternString(".java")
					.targetContentPatternString("@RestController")
				.build();
		BasicPatternImpl ocpTargetApp =
				BasicPatternImpl
				.Builder.aNewPattern()
				.addThisToAllAreRequiredClues(mavenConfig)
				.addThisToAllAreRequiredClues(restControllerStopWhenDetected)
				.build();
		WorkContext context =
				WorkContextDelegate
				.WorkContextBuilder
				.builder()
				.addThisToDesiredPatterns(ocpTargetApp)
				.build();
		DetectionResults results = PatternDetector.detect(context, new Pattern[] { ocpTargetApp });
		// @formatter:on		
		return null;
	}

}
