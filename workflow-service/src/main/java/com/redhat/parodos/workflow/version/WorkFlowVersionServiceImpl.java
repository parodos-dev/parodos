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
package com.redhat.parodos.workflow.version;

import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Provides functionality implementation to get hash of a workflow related class
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Service
@Slf4j
public class WorkFlowVersionServiceImpl implements WorkFlowVersionService {

	public static String GetVersionHashForObject(Object workflowObject) throws IOException {
		WorkFlowVersionServiceImpl versionService = new WorkFlowVersionServiceImpl();
		return versionService.getHash(workflowObject);
	}

	@Override
	public String getHash(Object workFlowRef) throws IOException {
		String md5 = "";
		try (InputStream is = getClassInputStream(workFlowRef)) {
			md5 = DigestUtils.md5Hex(is);
		}
		log.info("md5 checksum version of {} is : {}", workFlowRef.getClass(), md5);
		return md5;
	}

	private InputStream getClassInputStream(Object workFlowRef) throws IOException {
		return new ClassPathResource(workFlowRef.getClass().getName().replace(".", "/") + ".class",
				workFlowRef.getClass().getClassLoader()).getInputStream();
	}

}
