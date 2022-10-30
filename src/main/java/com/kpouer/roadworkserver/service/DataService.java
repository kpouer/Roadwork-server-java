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
package com.kpouer.roadworkserver.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpouer.roadwork.model.sync.SyncData;
import com.kpouer.roadworkserver.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthieu Casanova
 */
@Service
public class DataService {
    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    private final Config config;
    private final ObjectMapper objectMapper;

    public DataService(Config config) {
        this.config = config;
        objectMapper = new ObjectMapper();
    }

    public Map<String, SyncData> setData(String team, String opendataService, Map<String, SyncData> syncDataList) {
        logger.info("setData");
        Map<String, SyncData> existingSyncDataList = getData(team, opendataService);
        return merge(team, opendataService, existingSyncDataList, syncDataList);
    }

    private Path getPath(String team, String opendataService) {
        return Path.of(config.getDataPath(), team, opendataService + ".json");
    }

    private Map<String, SyncData> getData(String team, String opendataService) {
        Path dataPath = getPath(team, opendataService);
        logger.info("getData path={}", dataPath);
        if (Files.exists(dataPath)) {
            try {
                JavaType listType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, SyncData.class);
                return objectMapper.readValue(dataPath.toFile(), listType);
            } catch (IOException e) {
                logger.error("Unable read data", e);
            }
        }
        logger.info("Nothing to return for {}", opendataService);
        return Collections.emptyMap();
    }

    /**
     * Merge existing data with new data.
     *
     * @param team            the team name
     * @param opendataService the service name
     * @param existingSyncDataList        the existing data
     * @param newSyncDataList the new data
     * @return the merged data.
     */
    private Map<String, SyncData> merge(String team, String opendataService, Map<String, SyncData> existingSyncDataList, Map<String, SyncData> newSyncDataList) {
        logger.info("merge");
        long serverUpdateTime = System.currentTimeMillis();
        for (Entry<String, SyncData> entry : existingSyncDataList.entrySet()) {
            SyncData existingSyncData = entry.getValue();
            String id = entry.getKey();
            SyncData newSyncData = newSyncDataList.get(id);
            if (newSyncData != null) {
                if (newSyncData.isDirty()) {
                    if (newSyncData.getServerUpdateTime() == existingSyncData.getServerUpdateTime()) {
                        logger.info("{} dirty=true server time is identical, update time", id);
                        newSyncData.setServerUpdateTime(serverUpdateTime);
                        newSyncData.setLocalUpdateTime(serverUpdateTime);
                    } else {
                        // server version is more up to date but it is also modified by the client, use the greatest status
                        if (newSyncData.getStatus().compareTo(existingSyncData.getStatus()) < 0) {
                            logger.info("{} dirty=true server time is modified but server version is better ({} > {})", id, existingSyncData.getStatus(), newSyncData.getStatus());
                            // server version is better
                            newSyncData.setStatus(existingSyncData.getStatus());
                            newSyncData.setServerUpdateTime(existingSyncData.getServerUpdateTime());
                            newSyncData.setLocalUpdateTime(existingSyncData.getServerUpdateTime());
                        } else {
                            logger.info("{} dirty=true server time is modified but server version is lower ({} < {})", id, existingSyncData.getStatus(), newSyncData.getStatus());
                            newSyncData.setServerUpdateTime(serverUpdateTime);
                            newSyncData.setLocalUpdateTime(serverUpdateTime);
                        }
                    }
                    newSyncData.setDirty(false);
                } else {
                    if (newSyncData.getServerUpdateTime() != existingSyncData.getServerUpdateTime()) {
                        logger.info("{} dirty=false server time is different, copying server version", id);
                        newSyncData.setServerUpdateTime(existingSyncData.getServerUpdateTime());
                        newSyncData.setLocalUpdateTime(existingSyncData.getServerUpdateTime());
                        newSyncData.setStatus(existingSyncData.getStatus());
                    }
                }
            }
        }
        newSyncDataList.values().forEach(syncData -> syncData.setDirty(false));
        save(team, opendataService, newSyncDataList);
        return newSyncDataList;
    }

    private void save(String team, String opendataService, Map<String, SyncData> roadworkData) {
        Path path = getPath(team, opendataService);
        logger.info("save to {}", path);
        try {
            Files.createDirectories(path.getParent());
            objectMapper.writeValue(path.toFile(), roadworkData);
        } catch (IOException e) {
            logger.error("Unable to save", e);
        }
    }
}
