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
package com.redhat.parodos.notification.util;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Component;

/**
 * Utility method to get the Username from the Authenticated Session
 *
 * @author Richard Wang (Github: RichardW98)
 */
@Component
@Slf4j
public class SecurityUtil {

	/**
	 * Extract preferred username from security context.
	 * @return username.
	 */
	public String getUsername() {
		LdapUserDetailsImpl ldapDetails = (LdapUserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		if (ldapDetails != null) {
			return ldapDetails.getUsername();
		}
		else
			log.error("Unable to get the LdapDetails to get the username");
		return null;
	}

}
