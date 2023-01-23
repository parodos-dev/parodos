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
package com.redhat.parodos.workflows.common.context;

import com.redhat.parodos.workflows.common.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;

/**
 * 
 * Contains useful logic that is valuable for any WorkFlowTask implementation
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class WorkContextDelegate {
	
	private WorkContextDelegate() {
	}
	
	/**
	 * 
	 * Gets a String value from the WorkContext
	 * 
	 * @param workContext reference from the workflow-engine that is shared across WorkFlowTasks. It wraps a Map
	 * @param key used to put/get values from the WorkContext
	 * @return String value from the Map
	 * 
	 * @throws MissingParameterException if the value not found
	 */
	public static String getRequiredValueFromRequestParams(WorkContext workContext, String key) throws MissingParameterException {
        if (workContext.get(key) == null) {
            throw new MissingParameterException("For this task the WorkContext required key: " + key + " and a corresponding value");
        }
        return (String) workContext.get(key);
    }

	/**
	 * Gets a String value from the WorkContext
	 * 
	 * @param workContext reference from the workflow-engine that is shared across WorkFlowTasks. It wraps a Map
	 * @param key used to put/get values from the WorkContext
	 * @return String value from the Map or a null if that key does not exist
	 * 
	 */
	public static String getOptionalValueFromRequestParams(WorkContext workContext, String key, String defaultValue) {
        if (workContext.get(key) == null) {
            return defaultValue;
        }
        return (String) workContext.get(key);
    }
}
