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

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;

/**
 * 
 * Basic Contract for a WorkFlow Checker. This is tasks that checks if a Workflow that is in waiting status, perhaps due to an external process like an approval, have completed and the next Workflow can begin
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public interface WorkFlowChecker extends WorkFlowTask  {
	
	/**
	 * Method to check if a WorkFlow that is in a holding status, i.e: waiting for an external process to occur, has achieved its status and can trigger the next WorkFlow
	 * 
	 * @param context
	 * @return
	 */
	 WorkReport checkWorkFlowStatus(WorkContext context);
	
	 /**
	  * By default, if no execute method is defined, the checkWorkFlowStatus method will be executed by the WorkFlow engine
	  */
	public default WorkReport execute(WorkContext workContext) {
		return checkWorkFlowStatus(workContext);
	}

	 
	 

}
