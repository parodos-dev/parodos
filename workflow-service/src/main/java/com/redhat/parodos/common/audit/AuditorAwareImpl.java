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
package com.redhat.parodos.common.audit;

import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.user.service.UserService;
import org.jetbrains.annotations.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

/**
 * Audit aware implementation
 *
 * @author Annel Ketcha (Github: anludke)
 */
public class AuditorAwareImpl implements AuditorAware<UUID> {

	@Autowired
	private UserService userService;

	@NotNull
	@Override
	public Optional<UUID> getCurrentAuditor() {
		return Optional.of(userService.getUserEntityByUsername(SecurityUtils.getUsername()).getId());
	}

}
