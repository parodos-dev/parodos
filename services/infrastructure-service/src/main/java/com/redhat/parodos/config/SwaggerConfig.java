package com.redhat.parodos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuration for the Swagger Documentation
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Configuration
public class SwaggerConfig {
	@Bean
	OpenAPI springShopOpenAPI() {
		return new OpenAPI().info(new Info().title("Parodos Infrastructure Service")
			.description("Executes Assessments To Determine InfrastructureOptions (tooling + environments). Also executes InfrastructureTask Workflows to call downstream systems to stand-up an InfrastructureOption")
			.version("v.0.0.1"));
	}
}
