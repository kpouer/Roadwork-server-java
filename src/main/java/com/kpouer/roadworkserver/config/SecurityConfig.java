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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpouer.roadworkserver.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Security config.
 * @author Matthieu Casanova
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final BasicAuthenticationEntryPoint authenticationEntryPoint;
    private final InMemoryUserDetailsManager userDetailsService;
    private final Config config;
    private Map<String, User> users = Collections.emptyMap();

    public SecurityConfig(Config config) {
        this.config = config;
        authenticationEntryPoint = new BasicAuthenticationEntryPoint();
        authenticationEntryPoint.setRealmName("Roadwork");
        userDetailsService = new InMemoryUserDetailsManager();
        loadUsers();
    }

    public User getUser(String name) {
        return users.get(name);
    }

    public void loadUsers() {
        logger.info("loadUsers");
        Path path = Path.of(config.getDataPath(), "users.json");
        users.keySet().forEach(userDetailsService::deleteUser);
        if (Files.exists(path)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                User[] users = objectMapper.readValue(path.toFile(), User[].class);
                Arrays.stream(users).forEach(userDetailsService::createUser);
                this.users = new HashMap<>();
                Arrays.stream(users).forEach(user -> this.users.put(user.getUsername(), user));
            } catch (IOException e) {
                logger.error("Unable read data", e);
            }
        } else {
            logger.warn("No {} file", path);
            users.keySet().forEach(userDetailsService::deleteUser);
            users = Collections.emptyMap();
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .userDetailsPasswordManager(userDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().disable();
        http
                .authorizeRequests()
                .antMatchers("/setData/*").hasAuthority("Closure")
                .antMatchers("/salt/*").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint);
    }
}