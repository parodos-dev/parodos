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
package com.redhat.parodos.assessment;

import com.redhat.parodos.workflows.work.Work;

/**
 * 
 * An AssessmentTask is a unit of work that is performed on an input (i.e: a code base of an application) to determine what InfrastructureOption(s) might be available.
 * The Work construct comes from the easy-flows Workflow engine (https://github.com/j-easy/easy-flows/wiki) 
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public interface AssessmentTask extends Work {
	
	static final String WORK_FLOW_TYPE = "ASSESSMENT";
}
