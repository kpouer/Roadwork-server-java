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
import com.kpouer.roadworkserver.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Matthieu Casanova
 */
@Controller
public class RestController {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);

    private final DataService dataService;

    public RestController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/setData/{team}/{opendataService}")
    public ResponseEntity<Map<String, SyncData>> setData(@PathVariable String team, @PathVariable String opendataService, @RequestBody Map<String, SyncData> syncDataList) {
        MDC.put("team", team);
        MDC.put("service", opendataService);
        logger.info("setData");

        Map<String, SyncData> stringSyncDataMap = dataService.setData(team, opendataService, syncDataList);
        return new ResponseEntity<>(stringSyncDataMap, HttpStatus.OK);
    }
}
