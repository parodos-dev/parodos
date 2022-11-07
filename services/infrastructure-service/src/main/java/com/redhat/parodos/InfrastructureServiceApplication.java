package com.redhat.parodos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Main entry point into the application
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@SpringBootApplication
@EnableWebSecurity
public class InfrastructureServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(InfrastructureServiceApplication.class, args);
	}
}
