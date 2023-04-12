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
package com.redhat.parodos.patterndetection.clue.client;

import java.io.InputStream;
import java.util.Map;

/**
 *
 * It might be an expensive operation to obtain an inputstream for Detection. An
 * implementation of this can provide an oppertunity during the Detection phase to do
 * something extra (or even skip) obtaining content for a resource
 *
 * @author Luke Shannon (Github: lshannon)
 */

public interface ContentInputStreamClient {

	InputStream getContentIfRequired(String path, Map<String, Object> map);

	String getName();

}
