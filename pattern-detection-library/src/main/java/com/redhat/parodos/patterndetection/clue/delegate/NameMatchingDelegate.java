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
package com.redhat.parodos.patterndetection.clue.delegate;

import java.io.File;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Wraps the logic around file/folder name matching
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
@Data
public class NameMatchingDelegate {

	Pattern targetFileNameRegexPattern;
	Pattern targetFileExtensionPattern;
	Pattern ignoreFileNamePattern;
	boolean isFolder;
	
	//For the usage of the Builder
	private NameMatchingDelegate() {
		
	}
	
	/**
	 * Returns a true is a file/folder name matches. Regex is applied if 'targetFileNamePatternString' was set 
	 * when this object was created
	 * 
	 * @param name of the file/folder
	 * @return true if its a match, false is not
	 */
	public boolean doesNameMatch(String name) {
		return targetFileNameRegexPattern.matcher(name).matches();
	}
	
	/**
	 * Determines if a Target File Extension is a match. If 'targetFileExtensionPatternString' is specified at creation,
	 * then Regex will be applied when matching
	 * 
	 * @param file the file that the extension check will be applied too
	 * @return true if the file extension is a match, false if not. Regex is applied if 'targetFileExtensionPatternString' was specified
	 */
	public boolean isThisATargetFileExtension(File file) {
		if (getExtensionByStringHandling(file.getName()).isPresent() && targetFileExtensionPattern != null) {
			return targetFileExtensionPattern.matcher(getExtensionByStringHandling(file.getName()).get()).matches();
		}
		log.warn("Trying to check for a file extension with a file the extension could not be obtained for: {}", file.getName());
		return true;
	}
	
	/**
	 * Checks if a File is a target file for processing by the Clue.
	 * This check is based on the ignoreFileNamePattern and the targetFileExtensionPattern
	 * 
	 * @param currentFile file to be processed
	 * @return true if it should be processed, false if it should be ignored
	 */
	public boolean shouldProcessFile(File currentFile) {
		//Check if this is a file name pattern we should ignore
		if (isIgnoreFileNameDefined()) {
			return !ignoreFileNamePattern.matcher(currentFile.getName()).matches();
		}
		//check if this is an extension we are interested in
		if (targetFileExtensionPattern != null) {
			// don't need optional - String or Null
			Optional<String> extension = getExtensionByStringHandling(currentFile.getAbsolutePath());
			return extension.isPresent() && !targetFileExtensionPattern.matcher(extension.get()).matches();
		}
		//if we got to here, this is a file/folder we are interested in. Apply the name match
		if (targetFileNameRegexPattern != null) {
			// the file name is not the one specified - skip reading this file
			return targetFileNameRegexPattern.matcher(currentFile.getName()).matches();
		}
		return true;
	}

	
	boolean isIgnoreFileNameDefined() {
		return ignoreFileNamePattern != null;
	}

	Optional<String> getExtensionByStringHandling(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf('.')));
	}
	
	public static class Builder {
		
		private Builder() {
		}
		
		public static NameMatchingDelegateBuilder aNewNameMatchingDelegate() {
			return new NameMatchingDelegateBuilder();
		}
		
		public static class NameMatchingDelegateBuilder {
			
			private String targetFileNamePatternString;
			private String targetFileExtensionPatternString;
			private String ignoreFileNamePatternString;
			private boolean isFolder;
			
			NameMatchingDelegateBuilder() {
			}
			
			public NameMatchingDelegateBuilder targetFileNamePattern(String targetFileNamePatternString) {
				this.targetFileNamePatternString = targetFileNamePatternString;
				return this;
			}
			
			public NameMatchingDelegateBuilder isFolder(boolean isFolder) {
				this.isFolder = isFolder;
				return this;
			}
			
			public NameMatchingDelegateBuilder targetFileExtensionPattern(String targetFileExtensionPatternString) {
				this.targetFileExtensionPatternString = targetFileExtensionPatternString;
				return this;
			}
			
			public NameMatchingDelegateBuilder ignoreFileNamePattern(String ignoreFileNamePatternString) {
				this.ignoreFileNamePatternString = ignoreFileNamePatternString;
				return this;
			}
			
			public NameMatchingDelegate build() {
				NameMatchingDelegate instance = new NameMatchingDelegate();
				if (ignoreFileNamePatternString != null) {
					instance.setIgnoreFileNamePattern(Pattern.compile(ignoreFileNamePatternString));
				}
				if (targetFileExtensionPatternString != null) {
					instance.setTargetFileExtensionPattern(Pattern.compile(targetFileExtensionPatternString));
				}
				if (targetFileNamePatternString != null) {
					instance.setTargetFileNameRegexPattern(Pattern.compile(targetFileNamePatternString));
				}
				instance.setFolder(isFolder);
				return instance;
			}
		}
	}

	
}
