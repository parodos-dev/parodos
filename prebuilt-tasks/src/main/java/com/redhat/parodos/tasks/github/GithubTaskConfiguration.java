/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.tasks.github;

import java.io.IOException;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

/***
 * If there is no GitHub bean configured, this one will be returned. All a client needs to
 * do is set githubPersonalToken (which is a github personal access token) as an
 * environment variable
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
@Configuration
public class GithubTaskConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public GitHub gitHub(@Value("${githubPersonalToken:defaultValue}") String personalToken) {
		try {
			// @formatter:off
			return new GitHubBuilder()
					.withOAuthToken(personalToken)
					.build();
			// @formatter:on
		}
		catch (IOException e) {
			log.error("Unable to make the Github Connection using the personalToken provided: {}", personalToken);
			return null;
		}

	}

}
