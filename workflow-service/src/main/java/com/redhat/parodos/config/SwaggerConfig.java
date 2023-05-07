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
package com.redhat.parodos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Swagger Documentation
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 */

@Configuration
public class SwaggerConfig {

	@Bean
	OpenAPI parodosWorkFlowServiceOpenAPI() {
		return new OpenAPI().info(new Info().title("Parodos Workflow Service API")
				.description("This is the API documentation for the Parodos Workflow Service. "
						+ "It provides operations to execute assessments to determine infrastructure options (tooling + environments). "
						+ "Also executes infrastructure task workflows to call downstream systems to stand-up an infrastructure option.")
				.version("v1.0.0"));
	}

}
