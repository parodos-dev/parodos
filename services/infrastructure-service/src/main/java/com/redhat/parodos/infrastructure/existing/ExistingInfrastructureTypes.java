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
package com.redhat.parodos.infrastructure.existing;

/**
 * 
 * * These are the infrastructure tasks:
 *
 * - UPDATE_EXSISTING: This is when an application is currently running on Infrastructure and there is an Update available for the runtime, or a part of the tooling leading up to it. This option is intended for smaller changes to existing configuration/environments
 * - CREATE_NEW: This is when the application is not running anywhere, it will need new tooling and environments
 * - MIGRATE_NEW: This is when the application needs to be moved to a new environment (or tool chain) and that new environment/tool chain does not exist yet. This is different that creating new due to the fact that the workload will end up with two Infrastructure stacks (current and the new). This is state the enterprise might wish to keep an eye on
 * - MIGRATE_EXISTING: This is when the applications needs to be moved to a new environment (or tool chain) and these assets already exist
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public enum ExistingInfrastructureTypes {
	
	UPGRADE_EXISTING,
	CREATE_NEW,
	MIGRATE_NEW,
	MIGRATE_EXISTING

}
