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
package com.kpouer.roadworkserver.controller;

import com.kpouer.roadwork.model.sync.SyncData;
import com.kpouer.roadworkserver.config.SecurityConfig;
import com.kpouer.roadworkserver.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author Matthieu Casanova
 */
@org.springframework.web.bind.annotation.RestController
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    public static final String ADMIN = "admin";

    private final SecurityConfig securityConfig;

    public AdminController(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @GetMapping(value = "/admin/teams", produces = "application/json")
    public ResponseEntity<Collection<String>> listTeams(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        User userDetails = securityConfig.getUser(username);
        MDC.put("team", null);
        MDC.put("user", username);
        MDC.put("service", null);
        if (!userDetails.hasTeam(ADMIN)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        return new ResponseEntity<>(securityConfig.getTeams(), HttpStatus.OK);
    }
}
