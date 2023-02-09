/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.patterndetection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.redhat.parodos.patterndetection.clue.ContentsClueImpl;
import com.redhat.parodos.patterndetection.clue.NameClueImpl;
import com.redhat.parodos.patterndetection.context.WorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.patterndetection.pattern.BasicPatternImpl;
import com.redhat.parodos.patterndetection.pattern.Pattern;
import com.redhat.parodos.patterndetection.results.DetectionResults;
import com.redhat.parodos.workflows.work.WorkContext;

/*
 *
 * End to end testing of the Pattern Detection flow
 *
 */
class PatternDetectorTest {

	private static final String SRC_TEST_RESOURCES_JAVA_WEB_CONTROLLER_MULTIPLE_CLUE = "src/test/resources/javaWebControllerMultipleClue";

	private static final String SRC_TEST_RESOURCES_JAVA_WEB_CONTROLLER_CLUE = "src/test/resources/javaWebControllerClue";

	private static final String SRC_TEST_RESOURCES_MAVEN_CLUE = "src/test/resources/mavenClue";

	// Clues to use during test cases
	NameClueImpl mavenConfig = NameClueImpl.Builder.builder().name("maven").targetFileNamePatternString("pom.xml")
			.build();

	NameClueImpl reactConfig = NameClueImpl.Builder.builder().name("react").targetFileNamePatternString("package.json")
			.build();

	ContentsClueImpl restControllerMultiple = ContentsClueImpl.Builder.builder().name("restcontrollers")
			.continueToRunIfDetected(true).targetFileExtensionPatternString(".java")
			.targetContentPatternString("@RestController").build();

	ContentsClueImpl restControllerStopWhenDetected = ContentsClueImpl.Builder.builder().name("restcontroller")
			.targetFileExtensionPatternString(".java").targetContentPatternString("@RestController").build();

	ContentsClueImpl restControllerContinueRunning = ContentsClueImpl.Builder.builder().name("restcontroller")
			.continueToRunIfDetected(true).targetContentPatternString("@RestController").build();

	/*
	 * Ensure a scan can't proceed without configuration
	 */
	@Test
	void testDetect_failNoConfig_missingConfigurationError() {
		WorkContext context = new WorkContext();
		Assertions.assertThrows(PatternDetectionConfigurationException.class, () -> {
			PatternDetector.detect(context, null);
		});
	}

	/*
	 * Ensure the PatternDetector can handle null inputs by providing exceptions that are
	 * intuitive to the users
	 */
	@Test
	void testDetect_failNull_missingConfigurationError() {
		Assertions.assertThrows(PatternDetectionConfigurationException.class, () -> {
			PatternDetector.detect(null, null);
		});
	}

	/*
	 * Ensure a positive test succeeds and contains the appropriate results
	 */
	@Test
	void testDetect_success_contextContainResults() {
		BasicPatternImpl javaMavenPattern = BasicPatternImpl.Builder.aNewPattern()
				.addThisToAllAreRequiredClues(mavenConfig).build();
		WorkContext context = WorkContextDelegate.WorkContextBuilder.builder()
				.startDirectory(new File(SRC_TEST_RESOURCES_MAVEN_CLUE).getAbsolutePath())
				.addThisToDesiredPatterns(javaMavenPattern).build();
		DetectionResults report = PatternDetector.detect(context, new Pattern[] { javaMavenPattern });
		assertTrue(report.getDetectedPatterns().contains(javaMavenPattern));
	}

	/*
	 * Test to ensure false positives do not occur (ie: find a pattern that is not
	 * actually there)
	 */
	@Test
	void testDetect_success_contextDoesNotContainResults() {
		BasicPatternImpl javaMavenPattern = BasicPatternImpl.Builder.aNewPattern()
				.addThisToAllAreRequiredClues(reactConfig).build();
		WorkContext context = WorkContextDelegate.WorkContextBuilder.builder()
				.startDirectory(new File(SRC_TEST_RESOURCES_MAVEN_CLUE).getAbsolutePath())
				.addThisToDesiredPatterns(javaMavenPattern).build();
		DetectionResults results = PatternDetector.detect(context, new Pattern[] { javaMavenPattern });
		assertFalse(results.getDetectedPatterns().contains(javaMavenPattern));
	}

	/*
	 * Ensure all detected clues are in the results
	 */
	@Test
	void testDetect_allClueSuccess_contextContainsResults() {
		BasicPatternImpl controllerMavenPattern = BasicPatternImpl.Builder.aNewPattern()
				.addThisToAllAreRequiredClues(mavenConfig).addThisToAllAreRequiredClues(reactConfig).build();
		WorkContext context = WorkContextDelegate.WorkContextBuilder.builder()
				.startDirectory(new File(SRC_TEST_RESOURCES_JAVA_WEB_CONTROLLER_CLUE).getAbsolutePath())
				.addThisToDesiredPatterns(controllerMavenPattern).build();
		DetectionResults results = PatternDetector.detect(context, new Pattern[] { controllerMavenPattern });
		assertFalse(results.getDetectedPatterns().contains(controllerMavenPattern));
		assertTrue(results.getDetectedClues().containsKey(mavenConfig));
		assertFalse(results.getDetectedClues().containsKey(reactConfig));

	}

	/*
	 * Ensure 'oneOfClue' works as expected, meaning the first one detected should be
	 * there and only that one
	 */
	@Test
	void testDetect_oneOfClueSuccess_contextContainsResults() {

		BasicPatternImpl controllerMavenPattern = BasicPatternImpl.Builder.aNewPattern()
				.addThiToOneIsRequiredClues(mavenConfig).addThiToOneIsRequiredClues(reactConfig).build();
		WorkContext context = WorkContextDelegate.WorkContextBuilder.builder()
				.startDirectory(new File(SRC_TEST_RESOURCES_JAVA_WEB_CONTROLLER_CLUE).getAbsolutePath())
				.addThisToDesiredPatterns(controllerMavenPattern).build();
		DetectionResults results = PatternDetector.detect(context, new Pattern[] { controllerMavenPattern });
		assertTrue(results.getDetectedPatterns().contains(controllerMavenPattern));
		assertTrue(results.getDetectedClues().containsKey(mavenConfig));
		assertFalse(results.getDetectedClues().containsKey(reactConfig));

	}

	/*
	 * Ensure that AllAreRequired works as expected (ie: if one is not detected the
	 * Pattern should not be found)
	 *
	 */
	@Test
	void testDetect_missingOneAllClue_contextDoesNotContainResults() {
		BasicPatternImpl controllerMavenPattern = BasicPatternImpl.Builder.aNewPattern()
				.addThisToAllAreRequiredClues(mavenConfig).addThisToAllAreRequiredClues(restControllerStopWhenDetected)
				.build();
		WorkContext context = WorkContextDelegate.WorkContextBuilder.builder()
				.startDirectory(new File(SRC_TEST_RESOURCES_MAVEN_CLUE).getAbsolutePath())
				.addThisToDesiredPatterns(controllerMavenPattern).build();
		DetectionResults results = PatternDetector.detect(context, new Pattern[] { controllerMavenPattern });
		assertFalse(results.getDetectedPatterns().contains(controllerMavenPattern));
		assertTrue(results.getDetectedClues().containsKey(mavenConfig));
		assertFalse(results.getDetectedClues().containsKey(reactConfig));

	}

	/*
	 * Test to ensure continued matching works. This means a Clue should continue to be
	 * detected after its first detection
	 */
	@Test
	void testDetectOnlyJava_successContinuedMatching_contextContainsMultipleResults() {
		BasicPatternImpl multipleRestControllers = BasicPatternImpl.Builder.aNewPattern()
				.addThisToAllAreRequiredClues(restControllerMultiple).build();
		WorkContext context = createContextForContinuedMatchTests(multipleRestControllers);
		DetectionResults results = PatternDetector.detect(context, new Pattern[] { multipleRestControllers });
		assertTrue(results.getDetectedPatterns().contains(multipleRestControllers));
		assertTrue(results.getDetectedClues().containsKey(restControllerMultiple));
		assertEquals(4, results.getDetectedClues().get(restControllerMultiple).size());
	}

	/*
	 * Ensure that the Clue continues to get detected even after the Pattern is detected
	 *
	 */
	@Test
	void testDetectAllFiles_successContinuedMatching_contextContainsMultipleResults() {
		BasicPatternImpl multipleRestControllers = BasicPatternImpl.Builder.aNewPattern()
				.addThisToAllAreRequiredClues(restControllerContinueRunning).build();
		WorkContext context = createContextForContinuedMatchTests(multipleRestControllers);
		DetectionResults results = PatternDetector.detect(context, new Pattern[] { multipleRestControllers });
		assertTrue(results.getDetectedPatterns().contains(multipleRestControllers));
		assertTrue(results.getDetectedClues().containsKey(restControllerContinueRunning));
		assertEquals(5, results.getDetectedClues().get(restControllerContinueRunning).size());
	}

	private WorkContext createContextForContinuedMatchTests(BasicPatternImpl multipleRestControllers) {
		WorkContext context = WorkContextDelegate.WorkContextBuilder.builder()
				.startDirectory(new File(SRC_TEST_RESOURCES_JAVA_WEB_CONTROLLER_MULTIPLE_CLUE).getAbsolutePath())
				.addThisToDesiredPatterns(multipleRestControllers).build();
		return context;
	}

}
