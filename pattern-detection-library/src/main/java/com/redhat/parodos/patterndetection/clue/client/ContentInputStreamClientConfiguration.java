package com.redhat.parodos.patterndetection.clue.client;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentInputStreamClientConfiguration {

	private String name;

	private List<String> pathsToProcessForContent;

	private Map<String, Object> parametersForClient;

	private ContentInputStreamClient contentClient;

}
