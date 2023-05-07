package com.redhat.parodos.config.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "spring.security")
@ConfigurationPropertiesScan
@Data
public class SecurityProperties {

	Boolean authentication;

}