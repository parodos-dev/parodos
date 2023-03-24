package com.redhat.parodos.workflow.task.version;

import org.springframework.beans.factory.annotation.Value;

public class GithubVersionManager implements VersionManager {

	@Value("${gitPropertiesFileLocation}")
	String gitPropertiesFileLocation;

	@Override
	public String getVersion() {
		// we using this gitPropertiesFileLocation to find the git props and find the
		// version
		return null;
	}

}
