package com.redhat.parodos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
/**
 * Turn off security for Local testing only. Do not enable this profile in production
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Profile("local")
@Configuration
public class LocalSecurityConfiguration {

   @Bean
   SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.mvcMatcher("/").anonymous();
          return http.build();
   }
   
}