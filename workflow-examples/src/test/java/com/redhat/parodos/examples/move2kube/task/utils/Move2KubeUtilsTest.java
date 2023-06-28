package com.redhat.parodos.examples.move2kube.task.utils;

import java.net.URISyntaxException;

import com.redhat.parodos.examples.move2kube.utils.Move2KubeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Move2KubeUtilsTest {

	private static final String WORKSPACE_ID = "workspace-id";

	private static final String PROJECT_ID = "project-id";

	private static final String OUTPUT_ID = "output-id";

	@Test
	void testGetPath() throws URISyntaxException {
		String expectedResponse = "http://test.com/workspaces/workspace-id/projects/project-id/outputs/output-id";
		assertEquals(expectedResponse, Move2KubeUtils.getPath("http://test.com", WORKSPACE_ID, PROJECT_ID, OUTPUT_ID));
	}

}
