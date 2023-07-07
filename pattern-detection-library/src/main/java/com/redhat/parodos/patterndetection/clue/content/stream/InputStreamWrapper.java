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
package com.redhat.parodos.patterndetection.clue.content.stream;

import java.io.InputStream;
import lombok.Builder;
import lombok.Getter;

/**
 * A simple object to contain an InputStream and it's associated File Name. This is for
 * use when creating a File reference on the file system is not optimal or ideal
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Builder
@Getter
public class InputStreamWrapper {

	private InputStream inputStream;

	private String fileName;

}
