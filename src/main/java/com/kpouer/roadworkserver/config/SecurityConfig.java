/*
 * Copyright 2022 Matthieu Casanova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kpouer.roadworkserver.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security config.
 * @author Matthieu Casanova
 */
@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {
    private final BasicAuthenticationEntryPoint authenticationEntryPoint =  new BasicAuthenticationEntryPoint();
    private final InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
    private final UserConfig userConfig;

    @PostConstruct
    public void postConstruct() {
        authenticationEntryPoint.setRealmName("Roadwork");
        userConfig.getUsers().values().forEach(userDetailsService::createUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher("/admin/*")).hasAuthority("Admin")
                .requestMatchers(new AntPathRequestMatcher("/setData/*")).hasAuthority("Closure")
                .requestMatchers(new AntPathRequestMatcher("/salt/*")).permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint);
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        return userDetailsService;
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .userDetailsPasswordManager(userDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    public void removeAllUsers() {
        logger.info("Removing all users");
        userConfig.getUsers().keySet().forEach(userDetailsService::deleteUser);
        userConfig.loadUsers();
        userConfig.getUsers().values().forEach(userDetailsService::createUser);
    }
}