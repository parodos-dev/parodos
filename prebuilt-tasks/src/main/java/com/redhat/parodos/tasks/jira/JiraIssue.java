package com.redhat.parodos.tasks.jira;

import java.util.List;

/**
 * JiraIssue is a DTO representing a subset of an issue, and used as an input and output
 * to/from a {@link JiraTask}
 *
 */
public class JiraIssue {

	String id, project, description, summary, status;

	List<String> comments;

	public JiraIssue(String id, String project, String summary, String description, String status,
			List<String> comments) {
		this.id = id;
		this.project = project;
		this.summary = summary;
		this.description = description;
		this.status = status;
		this.comments = comments;
	}

}
