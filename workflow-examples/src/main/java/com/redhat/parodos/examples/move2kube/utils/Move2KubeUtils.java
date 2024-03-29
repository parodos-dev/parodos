package com.redhat.parodos.examples.move2kube.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public abstract class Move2KubeUtils {

	private Move2KubeUtils() {
	}

	public static String getPath(String server, String workspaceID, String projectID, String outputID)
			throws URISyntaxException {
		String path = "/workspaces/%s/projects/%s/outputs/%s".formatted(workspaceID, projectID, outputID);
		URI baseUri = new URI(server);
		return new URI(baseUri.getScheme(), baseUri.getAuthority(), path, null, null).toString();
	}

}
