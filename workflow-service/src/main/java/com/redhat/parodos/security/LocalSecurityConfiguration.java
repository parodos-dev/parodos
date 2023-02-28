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

import static org.springframework.security.config.Customizer.withDefaults;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Turn off security for Local testing only. Do not enable this profile in production
 *
 * @author Luke Shannon (Github: lshannon)
 */

@Profile("local")
@Configuration
@Slf4j
public class LocalSecurityConfiguration extends SecurityConfiguration {

	@Override
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		HttpSecurity httpSec = this.setHttpSecurity(http);
		httpSec.cors().disable();
		httpSec.csrf().disable();
		return httpSec.build();
	}

}