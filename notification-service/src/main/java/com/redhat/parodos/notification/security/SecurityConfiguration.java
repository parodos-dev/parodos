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
package com.redhat.parodos.notification.security;

import com.redhat.parodos.notification.config.SecurityProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * Security configuration for the application to ensure the main endpoints are locked down
 * and an OAuth2 server is enable. The OAuth2 server details can be found in the
 * application.yml file
 *
 * @author Luke Shannon (Github: lshannon)
 */

@Component
@Configuration
@Profile("!local")
@DependsOn("org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor")
public class SecurityConfiguration {

	@Autowired
	private LdapConnectionProperties ldapConnectionProperties;

	@Autowired
	private SecurityProperties securityProperties;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).cors(AbstractHttpConfigurer::disable);

		if (!this.securityProperties.isAuthentication()) {
			return http.build();
		}

		// @formatter:off
		http
				.authorizeHttpRequests(auth ->auth
						.requestMatchers(HttpMethod.OPTIONS, "/**")
						.permitAll()
						.requestMatchers("/api/**")
						.fullyAuthenticated()
						.anyRequest().permitAll())
				.httpBasic(Customizer.withDefaults())
				.headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.formLogin(form -> form.loginProcessingUrl("/login"))
				.logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
						.logoutSuccessUrl("/login").permitAll());
		// @formatter:on
		return http.build();
	}

	@Bean
	public InetOrgPersonContextMapper userContextMapper() {
		return new InetOrgPersonContextMapper();
	}

	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		if (!securityProperties.isAuthentication()) {
			return;
		}
		// @formatter:off
		auth.ldapAuthentication()
			.userDnPatterns(this.ldapConnectionProperties.getUserDNPatterns())
			.groupSearchBase(this.ldapConnectionProperties.getGroupSearchBase()).contextSource()
			.url(this.ldapConnectionProperties.getUrl())
			.managerDn(this.ldapConnectionProperties.getManagerDN())
			.managerPassword(this.ldapConnectionProperties.getManagerPassword())
			.and()
			.passwordCompare()
			.passwordEncoder(new BCryptPasswordEncoder())
			.passwordAttribute(this.ldapConnectionProperties.getPasswordAttribute());
		// @formatter:on
	}

}
