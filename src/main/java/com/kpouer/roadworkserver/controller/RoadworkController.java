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

import com.kpouer.roadwork.model.sync.SyncData;
import com.kpouer.roadworkserver.config.SecurityConfig;
import com.kpouer.roadworkserver.service.DataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Matthieu Casanova
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class RoadworkController {
    private final DataService dataService;
    private final SecurityConfig securityConfig;

    @PostMapping("/setData/{team}/{opendataService}")
    public ResponseEntity<Map<String, SyncData>> setData(HttpServletRequest request,
                                                         @PathVariable String team,
                                                         @PathVariable String opendataService,
                                                         @RequestBody Map<String, SyncData> syncDataList) {
        if (opendataService.endsWith(".json")) {
            opendataService = opendataService.substring(0, opendataService.length() - ".json".length());
        }
        var username = request.getUserPrincipal().getName();
        var userDetails = securityConfig.getUser(username);
        MDC.put("team", team);
        MDC.put("user", username);
        MDC.put("service", opendataService);
        if (!userDetails.hasTeam(team)) {
            logger.warn("User do not belong to that team");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        var stringSyncDataMap = dataService.setData(team, opendataService, syncDataList);
        return new ResponseEntity<>(stringSyncDataMap, HttpStatus.OK);
    }
}
