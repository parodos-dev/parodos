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
package com.redhat.parodos.patterndetection.clue.content;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.redhat.parodos.patterndetection.clue.content.stream.ContentInputStreamClientConfiguration;
import com.redhat.parodos.patterndetection.clue.content.stream.InputStreamWrapper;

/**
 * Helper methods for working with different content that might be used for
 * detection.
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class ContentsDelegate {

	private static ContentsDelegate instance = null;

	private ContentsDelegate() {
	}

	public static synchronized ContentsDelegate getInstance() {
		if (instance == null) {
			instance = new ContentsDelegate();
		}
		return instance;
	}

	/**
	 * Converts an InputStream to a list of strings. This is useful when the
	 * contents of a file has been converted into a list of strings (ie: getting the
	 * contents of a file from github)
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public List<String> inputStreamToList(InputStream content) throws IOException {
		return IOUtils.readLines(content, StandardCharsets.UTF_8);
	}

	/**
	 * Iterates through the supplied paths and uses the supplied client to generate
	 * a list of InputstreamWrappers. This gives an opportunity for the client to
	 * make custom decisions around how expensive InputStreams can be obtained
	 * 
	 * @param client
	 * @param paths
	 * @return
	 */
	public List<InputStreamWrapper> getContentUsingClient(ContentInputStreamClientConfiguration client) {
		return client.getPathsToProcessForContent().stream().map(path -> {
			InputStream stream = client.getContentClient().getContentIfRequired(path, client.getParametersForClient());
			if (stream != null) {
				return InputStreamWrapper.builder().inputStream(stream).fileName(path).build();
			} else {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

}
