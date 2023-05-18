package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import java.util.Iterator;
import java.util.Scanner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MTAAnalysisReport {

	/**
	 * migrationIssuesReport is assumed to be valid html. MTA 6.1 should supply a CSV as
	 * well. Throw and exception when the parsing fails or if it can't extract data
	 */
	public record AnalysisIncidents(int mandatory, int optional, int potential, int cloudMandatory, int cloudOptional,
			int information) {
	}

	/**
	 * Take the data/issue_summaries.js that contains a variable with a map of severity ->
	 * list of issues. Then extract the size of the list by the severity name. Parsing is
	 * straight forward - identify the assignment to the var and extract the value - it is
	 * a valid json map.
	 * @param jsIssuesData
	 * @return AnalysisIncidents
	 * @throws Exception - for any failure in json parsing or when there's nothing to
	 * parse
	 */
	public static AnalysisIncidents extractIncidents(String jsIssuesData) throws Exception {
		Scanner scanner = new Scanner(jsIssuesData);
		while (scanner.hasNextLine()) {
			String s = scanner.nextLine();
			// find the variable assignment WINDUP...['123'] = {$severity: [{}]}
			if (s.startsWith("WINDUP_ISSUE_SUMMARIES[")) {
				// extract only the json map data
				s = s.replaceFirst("WINDUP_ISSUE_SUMMARIES\\[.*\\] =", "");
				var mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
						.setSerializationInclusion(JsonInclude.Include.NON_NULL);
				try {
					int mandatory = 0, optional = 0, potential = 0, cloudMandatory = 0, cloudOptional = 0,
							information = 0;
					JsonNode jsonNode = mapper.readTree(s);
					for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext();) {
						String severity = it.next();
						switch (severity) {
							case "mandatory" -> mandatory = jsonNode.get(severity).size();
							case "optional" -> optional = jsonNode.get(severity).size();
							case "potential" -> potential = jsonNode.get(severity).size();
							case "cloud-mandatory" -> cloudMandatory = jsonNode.get(severity).size();
							case "cloud-optional" -> cloudOptional = jsonNode.get(severity).size();
							case "information" -> information = jsonNode.get(severity).size();
						}
					}
					return new MTAAnalysisReport.AnalysisIncidents(mandatory, optional, potential, cloudMandatory,
							cloudOptional, information);
				}
				catch (JsonProcessingException e) {
					throw new Exception("failed extracting the incidents summary from issue_summaries.js " + e);
				}
			}
		}
		throw new Exception(
				"failed extracting the incidents summary from issue_summaries.js - didn't match any line. Possibly the format changed"
						+ "by MTA or wrong page is scanned. Data parsed: " + jsIssuesData);
	}

}
