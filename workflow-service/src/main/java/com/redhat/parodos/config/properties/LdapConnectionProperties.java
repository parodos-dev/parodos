package com.redhat.parodos.config.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "spring.ldap.connection")
@ConfigurationPropertiesScan
@Data
public class LdapConnectionProperties {

	String userDNPatterns;

	String groupSearchBase;

	String url;

	String managerDN;

	String managerPassword;

	String passwordAttribute;

}
