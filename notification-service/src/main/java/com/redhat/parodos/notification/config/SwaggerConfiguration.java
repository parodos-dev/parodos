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
package com.redhat.parodos.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
@Configuration
public class SwaggerConfiguration {

	@Bean
	OpenAPI parodosNotificationServiceOpenAPI() {
		return new OpenAPI().info(new Info().title("Parodos Notification Service API")
				.description("This is the API documentation for the Parodos Notification Service. "
						+ "It provides operations to send out and check notification. "
						+ "The endpoints are secured with oAuth2/OpenID and cannot be accessed without a valid token.")
				.version("v1.0.0"));
	}

}