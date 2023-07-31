package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MTAAnalysisReportTest {

	public static String validJS = """
			var WINDUP_ISSUE_SUMMARIES = [];
			WINDUP_ISSUE_SUMMARIES['872448'] = {"mandatory": [{"id":"some incident id"}],"optional": [{"id": 123}], "cloud-mandatory": [{"id":1}, {"id": 2}], "cloud-optional": [{"id": 5}], "information": [{"id": 7}]};
			var effortToDescription = [];
			effortToDescription[0] = "Info";
			effortToDescription[1] = "Trivial";
			effortToDescription[3] = "Complex";
			effortToDescription[5] = "Redesign";
			effortToDescription[7] = "Architectural";
			effortToDescription[13] = "Unknown";
			var effortOrder = ["Info", "Trivial", "Complex", "Redesign", "Architectural", "Unknown"];
			var severityOrder = ['mandatory', 'optional', 'potential', 'cloud-mandatory', 'cloud-optional', 'information', ];
			""";

	@Test
	public void canExtractJS() throws Exception {
		MTAAnalysisReport.AnalysisIncidents incidents = MTAAnalysisReport.extractIncidents(validJS);
		assertThat(incidents, equalTo(new MTAAnalysisReport.AnalysisIncidents(1, 1, 0, 2, 1, 1)));
	}

}