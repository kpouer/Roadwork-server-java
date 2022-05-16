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
import com.kpouer.roadworkserver.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Matthieu Casanova
 */
@org.springframework.web.bind.annotation.RestController
public class RestController {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);

    private final DataService dataService;
    private final SecurityConfig securityConfig;

    public RestController(DataService dataService, SecurityConfig securityConfig) {
        this.dataService = dataService;
        this.securityConfig = securityConfig;
    }

    @PostMapping("/setData/{team}/{opendataService}")
    public ResponseEntity<Map<String, SyncData>> setData(HttpServletRequest request, @PathVariable String team, @PathVariable String opendataService, @RequestBody Map<String, SyncData> syncDataList) {
        String username = request.getUserPrincipal().getName();
        User userDetails = securityConfig.getUser(username);
        MDC.put("team", team);
        MDC.put("user", username);
        MDC.put("service", opendataService);
        if (!userDetails.hasTeam(team)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        logger.info("setData");

        Map<String, SyncData> stringSyncDataMap = dataService.setData(team, opendataService, syncDataList);
        return new ResponseEntity<>(stringSyncDataMap, HttpStatus.OK);
    }
}
