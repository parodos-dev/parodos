package com.redhat.parodos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import lombok.Data;

@ConfigurationProperties(prefix = "spring.security")
@ConfigurationPropertiesScan
@Data
public class SecurityProperties {

	Boolean authentication;

}