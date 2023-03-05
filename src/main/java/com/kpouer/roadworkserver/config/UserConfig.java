/*
 * Copyright 2023 Matthieu Casanova
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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class UserConfig {
    private final Config config;
    private Map<String, User> users;

    @PostConstruct
    public void postConstruct() {
        loadUsers();
    }

    /**
     * Load the users from the users.json file.
     * The users must be removed from the userDetailsService before calling this method.
     */
    public void loadUsers() {
        logger.info("loadUsers");
        var path = Path.of(config.getDataPath(), "users.json");
        if (Files.exists(path)) {
            try {
                var objectMapper = new ObjectMapper();
                var users = objectMapper.readValue(path.toFile(), User[].class);
                this.users = Arrays.stream(users).collect(Collectors.toMap(User::getUsername, Function.identity()));
            } catch (IOException e) {
                logger.error("Unable read data", e);
                users = Collections.emptyMap();
            }
        } else {
            logger.warn("No {} file", path);
            users = Collections.emptyMap();
        }
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public User getUser(String name) {
        return users.get(name);
    }


    /**
     * Returns a list of all teams.
     *
     * @return a list of all teams
     */
    public Collection<String> getTeams() {
        var teams = new HashSet<String>();
        users
                .values()
                .stream()
                .map(User::getTeams)
                .map(List::of)
                .forEach(teams::addAll);
        return teams;
    }
}
