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
package com.redhat.parodos.patterndetection.clue;

import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;

/**
 * 
 * A Clue is a aspect of a file/folder that can be detected. Clue(s) are grouped together to establish a @see Pattern.
 * 
 * Clue is based on the @see Work, which comes from on j-easy (https://github.com/j-easy/easy-flows) Work class. This provides a contract for a @see WorkFlow to execute
 * 
 * If 'isContinueToRunIfDetected' is true, the scanner will keep matching for this Clue (this is enforced in @see WorkContextDelegate). If its false, the scanner will make the
 * Clue detected, save the file it was detected in and no longer scan for that particular Clue.
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public interface Clue extends Work {

	WorkReport execute(WorkContext workContext);

}
