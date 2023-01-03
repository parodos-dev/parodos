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
package com.redhat.parodos.workflows;

import lombok.Getter;

import java.util.Map;

/**
 * A base class to build WorkFlowChecker references with
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public abstract class BaseWorkFlowChecker implements WorkFlowChecker {
	
	String nextWorkFlowId;
	Map<String,String> nextWorkFlowArguments;

	@Getter
	private final String name;

	public BaseWorkFlowChecker(String nextWorkFlowId, Map<String, String> nextWorkFlowArguments, String name) {
		super();
		this.name = name;
		this.nextWorkFlowId = nextWorkFlowId;
		this.nextWorkFlowArguments = nextWorkFlowArguments;
	}
	
	public BaseWorkFlowChecker(String nextWorkFlowId, String name) {
		super();
		this.nextWorkFlowId = nextWorkFlowId;
		this.name = name;
	}

	public String getNextWorkFlowId() {
		return nextWorkFlowId;
	}

	public Map<String, String> getNextWorkFlowArguments() {
		return nextWorkFlowArguments;
	}
	
	

}
