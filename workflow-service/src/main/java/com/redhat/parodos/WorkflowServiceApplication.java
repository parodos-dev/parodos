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
package com.redhat.parodos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Main entry point into the application
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardW98)
 *
 */
@SpringBootApplication(scanBasePackages = { "com.redhat.parodos", "${scan.packages}" },
		exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@EnableWebSecurity
@EnableAspectJAutoProxy
@EnableConfigurationProperties
@ConfigurationPropertiesScan("com.redhat.parodos")
public class WorkflowServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowServiceApplication.class, args);
	}

}
