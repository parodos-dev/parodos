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
package com.redhat.parodos.security;

import com.redhat.parodos.config.properties.LdapConnectionProperties;
import com.redhat.parodos.config.properties.SecurityProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.stereotype.Component;

/**
 * Security configuration for the application to ensure the main endpoints are locked down
 * and an OAuth2 server is enabled. The OAuth2 server details can be found in the
 * application.yml file
 *
 * @author Luke Shannon (Github: lshannon)
 */

@Component
@Configuration
@Profile("!local")
@DependsOn("org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private LdapConnectionProperties ldapConnectionProperties;

	@Autowired
	private SecurityProperties securityProperties;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().cors().disable();

		if (!this.securityProperties.getAuthentication()) {
			return;
		}

		// @formatter:off
        http
                .authorizeRequests()
                .mvcMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .mvcMatchers("/api/**", "/actuator/shutdown")
                .fullyAuthenticated()
                .and()
                .httpBasic(Customizer.withDefaults())
                .headers().frameOptions().disable()
                .and()
                .formLogin(form -> form.loginProcessingUrl("/login"))
                .logout()
                .logoutSuccessUrl("/login").permitAll();
        // @formatter:on
	}

	@Bean
	public InetOrgPersonContextMapper userContextMapper() {
		return new InetOrgPersonContextMapper();
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		if (!this.securityProperties.getAuthentication()) {
			return;
		}
		// @formatter:off
		auth
				.ldapAuthentication()
                .userDetailsContextMapper(userContextMapper())
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
