/*
 * Copyright 2022-2023 Matthieu Casanova
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

import com.kpouer.roadworkserver.config.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author Matthieu Casanova
 */
@RestController
@RequiredArgsConstructor
public class AdminController {
    private final SecurityConfig securityConfig;

    @GetMapping(value = "/admin/teams", produces = "application/json")
    public ResponseEntity<Collection<String>> listTeams(HttpServletRequest request) {
        var username = request.getUserPrincipal().getName();
        var userDetails = securityConfig.getUser(username);
        MDC.clear();
        MDC.put("user", username);
        return new ResponseEntity<>(securityConfig.getTeams(), HttpStatus.OK);
    }

    @GetMapping("/salt/{password}")
    public String salt(@PathVariable("password") String password) {
        return new BCryptPasswordEncoder().encode(password);
    }
}
