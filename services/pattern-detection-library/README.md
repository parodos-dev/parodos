# Patter Detection

This is a Java Library that can be used to detect different 'Patterns' in code/configuration that can be used during the Assessment phase of the Parodos infrastructure-service to help determine if a specific Infrastructure Option should be assigned (ie: .NET applications, Java and J2EE Java all have different options)

## Introduction

Can be embedded into any Service to detect a desired 'Pattern'.

## Domain Model

**Pattern**

A combination of Clues that might be found in code base.

A Pattern has a collection of needOneOfClues, and a collection of needAllOfClues. Provided the appropriate Clues are detected in these collection, the Pattern is considered to be detected.

A pattern can be programmatically configured like this:

```java

BasicPatternImpl controllerMavenPattern = BasicPatternImpl.Builder
				.aNewPattern()
				.addThisToAllAreRequiredClues(mavenConfig)
				.addThisToAllAreRequiredClues(reactConfig)
				.build();

```

It can also be externally configured:

```json

name = "fullStackJavascriptFrameworkState"
needAllOfTheseClues = [
	"java-clue",
	"mavenFrontEndPlugin-clue",
	"node-clue"
]
needOneOfTheseClue = [
	"javascript-react-clue",
	"javascript-angular-clue"
]


```

**Clue**

A Clue is something specific that is being looked for in a file or collection of files(s).

Implementations of Clues are:

**FileContentsCondition** Looks for specific strings in a file. Support for Regex. Example: (Java file containing @RestController)

**FileExtensionCondition** Looks for files that contain a specific extension. Support for Regex. Example: (.js file)

**FileNameExtension** Looks for files containing a name, or Regex pattern. Example: package.js

**FolderNameExtension** Similar to FileName extension, but looks at the sames of folders. Example: 'client'

A clue can be configured in Java like this:

```java

ContentsClueImpl.Builder.builder().name("restcontroller").targetFileExtensionPatternString(".java").targetContentPatternString("@RestController").build();

NameClueImpl.Builder.builder().name("maven").targetFileNamePatternString("pom.xml").build();

```

Or externally like this:

```json

name = "isJavaProject"
targetFileExtensionPatternString = ".java"

```


## Detecting Patterns

Once the Clues and Patterns have been defined, a detection can run on code or folders for one or more patterns like this:

```java

WorkContext context = WorkContextDelegate.WorkContextBuilder.builder()
				.startDirectory(new File(SRC_TEST_RESOURCES_JAVA_WEB_CONTROLLER_CLUE).getAbsolutePath())
				.addThisToDesiredPatterns(controllerMavenPattern)
				.build();
		DetectionResults results = PatternDetector.detect(context);

```

The DectionResults object will contain the detected Patterns, start time (or the detection run), end time (of the detection run) and all discovered Clue (regardless of if they were part of a detected Pattern).

