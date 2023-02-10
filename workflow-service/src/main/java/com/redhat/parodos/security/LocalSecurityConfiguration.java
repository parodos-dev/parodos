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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Turn off security for Local testing only. Do not enable this profile in production
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */

@Profile("local")
@Configuration
public class LocalSecurityConfiguration {

	private static final String REQUIRED_ROLE = "USER";

	private static final String USER_FIELD_SEPERATOR = ":";

	private static final String USER_SEPERATOR = ",";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors().disable().csrf().disable().authorizeRequests()
				.antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/login**").permitAll().antMatchers("/**")
				.authenticated().and().httpBasic(withDefaults())
				.formLogin(form -> form.loginProcessingUrl("/perform_login")).logout().logoutSuccessUrl("/login")
				.permitAll();
		// @formatter:on
		return http.build();
	}

	@Bean
	UserDetailsService users(@Value("${users}") String userDetailsString) {
		return new InMemoryUserDetailsManager(getUsers(userDetailsString));
	}

	/*
	 * Get the users from the users string in memory
	 */
	private List<UserDetails> getUsers(String userDetailsString) {
		if (!userDetailsString.contains(USER_FIELD_SEPERATOR)) {
			throw new RuntimeException(
					"The User Details string must have a ':' seperating username and password (ie: testuser:testpassword) and commas seperating the User entries (ie: testuser1:testpassword1,testuser2:testpassword2");
		}
		List<UserDetails> users = new ArrayList<UserDetails>();
		for (String upa : userDetailsString.split(USER_SEPERATOR)) {
			String[] up = upa.replace(USER_SEPERATOR, "").split(USER_FIELD_SEPERATOR);
			users.add(User.builder().username(up[0]).password("{noop}" + decodePassword(up[1])).roles(REQUIRED_ROLE)
					.build());
		}
		return users;
	}

	/*
	 * The password value will be Base64 encoded - this will decode it
	 */
	private String decodePassword(String encodedPassword) {
		byte[] decodedBytes = Base64.getDecoder().decode(encodedPassword);
		return new String(decodedBytes);
	}

}