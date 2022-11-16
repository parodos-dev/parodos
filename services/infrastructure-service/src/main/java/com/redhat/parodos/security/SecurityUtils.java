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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility Method for assessing the Username and Accesstoken fron the authenticated session
 * 
 * @author Jennifer Ubah, Luke Shannon (Github: lshannon)
 *
 */
@Component
@Slf4j
public class SecurityUtils {
	
    private SecurityUtils() {
    }

    /**
     * Extract preferred username from security context.
     *
     * @return username.
     */
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getToken().getClaim("preferred_username");
        }
        log.error("Unable to find the username for the authenticated user - if this is being ran under the 'local' profile this behavior is expected");
        return null;
   }
}
