package com.redhat.parodos.tasks.github;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
@Slf4j
@Configuration
public class GitHubConfiguration {
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
